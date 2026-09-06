package com.weeeol.activityapp

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// Apple-style colors
val MoveColor = Color(0xFFFA114F)
val ExerciseColor = Color(0xFF92E01D)
val StandColor = Color(0xFF1DDAE2)

@Composable
fun IosCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.975f else 1.0f,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "iosCardScale"
    )

    var boxModifier = modifier
        .graphicsLayer {
            scaleX = cardScale
            scaleY = cardScale
        }
        .clip(RoundedCornerShape(20.dp))
        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        .border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
            shape = RoundedCornerShape(20.dp)
        )

    if (onClick != null) {
        boxModifier = boxModifier.clickable(
            interactionSource = interactionSource,
            indication = null
        ) { onClick() }
    }

    Box(
        modifier = boxModifier.padding(16.dp),
        content = content
    )
}

@Composable
fun HealthScreen(
    waterGlasses: Int,
    steps: Int = 0,
    stepsGoal: Int = 10000,
    waterGoal: Int = 8,
    onUpdateSteps: (Int) -> Unit = {},
    onUpdateStepGoal: (Int) -> Unit = {},
    onAddWater: () -> Unit,
    onResetWater: () -> Unit,
    onGetLastSensorValue: () -> Float = { -1f },
    onSaveSensorValue: (Float) -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var isDashboardExpanded by remember { mutableStateOf(false) }

    var showGoalDialog by remember { mutableStateOf(false) }
    var goalInput by remember { mutableStateOf("") }
    var lastSensorValue by remember { mutableFloatStateOf(onGetLastSensorValue()) }

    var weatherLocation by remember { mutableStateOf("Mangaluru") }
    var weatherCondition by remember { mutableStateOf("Sunny") }
    var showWeatherDialog by remember { mutableStateOf(false) }
    var tempLocationInput by remember { mutableStateOf("") }

    // Celebration system for milestones
    var showCelebration by remember { mutableStateOf(false) }
    var celebrationTitle by remember { mutableStateOf("") }
    var celebrationSubtitle by remember { mutableStateOf("") }
    var celebrationColor by remember { mutableStateOf(StandColor) }

    var celebratedWaterToday by remember { mutableStateOf(waterGlasses >= waterGoal && waterGoal > 0) }
    var celebratedStepsToday by remember { mutableStateOf(steps >= stepsGoal && stepsGoal > 0) }

    LaunchedEffect(waterGlasses, waterGoal) {
        if (waterGlasses >= waterGoal && waterGoal > 0 && !celebratedWaterToday) {
            celebratedWaterToday = true
            celebrationTitle = "Hydration Goal Crushed! 🌊"
            celebrationSubtitle = "$waterGlasses of $waterGoal glasses logged today"
            celebrationColor = StandColor
            showCelebration = true
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } else if (waterGlasses < waterGoal) {
            celebratedWaterToday = false
        }
    }

    LaunchedEffect(steps, stepsGoal) {
        if (steps >= stepsGoal && stepsGoal > 0 && !celebratedStepsToday) {
            celebratedStepsToday = true
            celebrationTitle = "Step Goal Smashed! 🏃"
            celebrationSubtitle = "$steps of $stepsGoal steps completed today"
            celebrationColor = ExerciseColor
            showCelebration = true
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } else if (steps < stepsGoal) {
            celebratedStepsToday = false
        }
    }

    LaunchedEffect(showCelebration) {
        if (showCelebration) {
            kotlinx.coroutines.delay(4500L)
            showCelebration = false
        }
    }
    // Sensor logic
    DisposableEffect(Unit) {
        val sensorManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val attributionContext = context.createAttributionContext("StepCounterFeature")
            attributionContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        } else {
            context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        }

        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    val currentSensorValue = it.values[0]
                    if (lastSensorValue == -1f) {
                        lastSensorValue = currentSensorValue
                        onSaveSensorValue(lastSensorValue)
                    } else {
                        var delta = currentSensorValue - lastSensorValue
                        if (delta < 0) delta = currentSensorValue
                        if (delta > 0) {
                            val newSteps = steps + delta.toInt()
                            onUpdateSteps(newSteps)
                            lastSensorValue = currentSensorValue
                            onSaveSensorValue(lastSensorValue)
                        }
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (stepSensor != null) {
            sensorManager.registerListener(listener, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        onDispose { sensorManager.unregisterListener(listener) }
    }

    val sleepHours = 6.5f
    val sleepGoal = 8.0f
    val sleepProgress = (sleepHours / sleepGoal).coerceIn(0f, 1f)
    val stepsProgress = if (stepsGoal > 0) (steps.toFloat() / stepsGoal).coerceIn(0f, 1f) else 0f
    val waterProgress = (waterGlasses.toFloat() / waterGoal).coerceIn(0f, 1f)

    val todayFormatted = remember {
        java.text.SimpleDateFormat("EEEE, MMMM d", java.util.Locale.getDefault()).format(java.util.Date()).uppercase()
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 8.dp)
        ) {
            // Header (Aligned with Notes, Folders, and Timer screens)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { isDashboardExpanded = false },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = todayFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Summary",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Content Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                androidx.compose.animation.AnimatedContent(
                    targetState = isDashboardExpanded,
                    transitionSpec = {
                        (androidx.compose.animation.fadeIn(animationSpec = tween(400)) + androidx.compose.animation.scaleIn(initialScale = 0.8f, animationSpec = tween(400)))
                            .togetherWith(
                                androidx.compose.animation.fadeOut(animationSpec = tween(400)) + androidx.compose.animation.scaleOut(targetScale = 0.8f, animationSpec = tween(400)))
                    },
                    label = "dashboard_split"
                ) { expanded ->
                    if (!expanded) {
                        // Hero Rings Card
                        IosCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { isDashboardExpanded = true }
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Activity",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.align(Alignment.Start)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Box(modifier = Modifier.size(220.dp)) {
                                    ActivityRings(
                                        moveProgress = sleepProgress,
                                        exerciseProgress = stepsProgress,
                                        standProgress = waterProgress
                                    )
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    RingLegend("Sleep", MoveColor, "$sleepHours", "hr")
                                    RingLegend("Steps", ExerciseColor, "$steps", "")
                                    RingLegend("Water", StandColor, "$waterGlasses", "gls")
                                }
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Stats Grid - Row 1: Steps & Sleep side-by-side
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Steps Card
                                IosCard(
                                    modifier = Modifier.weight(1f).aspectRatio(1f),
                                    onClick = {
                                        goalInput = stepsGoal.toString()
                                        showGoalDialog = true
                                    }
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "Steps",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = ExerciseColor
                                        )
                                        Column {
                                            Text(
                                                "$steps",
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                "Goal: $stepsGoal",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }

                                // Sleep Card
                                IosCard(
                                    modifier = Modifier.weight(1f).aspectRatio(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "Sleep",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MoveColor
                                        )
                                        Column {
                                            Text(
                                                "${sleepHours}h",
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                "Goal: ${sleepGoal}h",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                            }

                            // Stats Grid - Row 2: Water Card (Wide, balanced layout)
                            IosCard(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                "Hydration",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = StandColor
                                            )
                                            if (waterGlasses > 0) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = "Reset",
                                                    tint = Color.Gray.copy(alpha = 0.6f),
                                                    modifier = Modifier
                                                        .size(18.dp)
                                                        .clickable {
                                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                            onResetWater()
                                                        }
                                                )
                                            }
                                        }
                                        Row(verticalAlignment = Alignment.Bottom) {
                                            Text(
                                                "$waterGlasses",
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                " / $waterGoal glasses",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.Gray,
                                                modifier = Modifier.padding(bottom = 3.dp, start = 4.dp)
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onAddWater()
                                        },
                                        modifier = Modifier
                                            .size(46.dp)
                                            .background(StandColor.copy(alpha = 0.2f), CircleShape)
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Add Water",
                                            tint = StandColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Weather Card (Always displayed, full width)
                IosCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        tempLocationInput = weatherLocation
                        showWeatherDialog = true
                    }
                ) {
                    val iconColor = if (weatherCondition == "Sunny") Color(0xFFFFD700) else Color.LightGray
                    val icon = if (weatherCondition == "Sunny") Icons.Default.WbSunny else Icons.Default.Cloud
                    val temp = if (weatherCondition == "Sunny") "32°" else "26°"

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "Weather",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                weatherLocation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                temp,
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = icon,
                                contentDescription = weatherCondition,
                                tint = iconColor,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }
                }
            }

            // Bottom clearance for the Floating Navigation Bar
            Spacer(modifier = Modifier.height(140.dp))
        }

        // Dialogs
        if (showGoalDialog) {
            AlertDialog(
                onDismissRequest = { showGoalDialog = false },
                title = { Text("Daily Step Goal") },
                text = {
                    OutlinedTextField(
                        value = goalInput,
                        onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) goalInput = it },
                        label = { Text("Steps") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        val newGoal = goalInput.toIntOrNull() ?: 10000
                        onUpdateStepGoal(newGoal)
                        showGoalDialog = false
                    }) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { showGoalDialog = false }) { Text("Cancel") }
                }
            )
        }

        if (showWeatherDialog) {
            AlertDialog(
                onDismissRequest = { showWeatherDialog = false },
                title = { Text("Weather") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = tempLocationInput,
                            onValueChange = { tempLocationInput = it },
                            label = { Text("Location") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Button(
                                onClick = { weatherCondition = "Sunny" },
                                colors = ButtonDefaults.buttonColors(containerColor = if (weatherCondition == "Sunny") MaterialTheme.colorScheme.primary else Color.Gray)
                            ) { Text("Sunny") }
                            Button(
                                onClick = { weatherCondition = "Cloudy" },
                                colors = ButtonDefaults.buttonColors(containerColor = if (weatherCondition == "Cloudy") MaterialTheme.colorScheme.primary else Color.Gray)
                            ) { Text("Cloudy") }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        weatherLocation = tempLocationInput.ifBlank { "Unknown" }
                        showWeatherDialog = false
                    }) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { showWeatherDialog = false }) { Text("Cancel") }
                }
            )
        }

        // Confetti Celebration Particle Cannon
        ConfettiCelebration(
            isTriggered = showCelebration,
            primaryColor = celebrationColor,
            onFinished = { }
        )

        // Celebration Banner
        AnimatedVisibility(
            visible = showCelebration,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 10.dp, start = 16.dp, end = 16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.5.dp, celebrationColor.copy(alpha = 0.55f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCelebration = false }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(celebrationColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🎉", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = celebrationTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = celebrationColor
                        )
                        Text(
                            text = celebrationSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    IconButton(
                        onClick = { showCelebration = false },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RingLegend(title: String, color: Color, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.labelMedium, color = color)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (unit.isNotEmpty()) {
                Text(unit, style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.padding(bottom = 2.dp, start = 2.dp))
            }
        }
    }
}

@Composable
fun ActivityRings(
    moveProgress: Float,
    exerciseProgress: Float,
    standProgress: Float,
    modifier: Modifier = Modifier
) {
    val animMove by animateFloatAsState(
        targetValue = moveProgress,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = Spring.StiffnessLow
        ),
        label = "moveAnim"
    )
    val animExercise by animateFloatAsState(
        targetValue = exerciseProgress,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = Spring.StiffnessLow
        ),
        label = "exAnim"
    )
    val animStand by animateFloatAsState(
        targetValue = standProgress,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = Spring.StiffnessLow
        ),
        label = "stAnim"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val strokeWidth = size.width * 0.12f
        val spacing = 2.dp.toPx()
        val center = Offset(size.width / 2, size.height / 2)

        val r1 = (size.width - strokeWidth) / 2
        val r2 = r1 - strokeWidth - spacing
        val r3 = r2 - strokeWidth - spacing

        fun drawRing(radius: Float, progress: Float, color: Color) {
            // Background track
            drawArc(
                color = color.copy(alpha = 0.2f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth)
            )
            // Progress
            if (progress > 0) {
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = (progress * 360f).coerceAtMost(360f),
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        drawRing(r1, animMove, MoveColor)
        drawRing(r2, animExercise, ExerciseColor)
        drawRing(r3, animStand, StandColor)
    }
}

data class ConfettiPiece(
    val startX: Float,
    val startY: Float,
    val velocityX: Float,
    val velocityY: Float,
    val rotationSpeed: Float,
    val initialRotation: Float,
    val color: Color,
    val width: Float,
    val height: Float,
    val isRibbon: Boolean
)

@Composable
fun ConfettiCelebration(
    isTriggered: Boolean,
    primaryColor: Color,
    onFinished: () -> Unit
) {
    if (!isTriggered) return

    val confettiColors = remember(primaryColor) {
        listOf(
            primaryColor,
            Color(0xFFFFD700), // Gold
            Color(0xFFFA114F), // Move pink
            Color(0xFF92E01D), // Exercise lime
            Color(0xFF1DDAE2), // Stand cyan
            Color(0xFFBF5AF2), // Purple
            Color(0xFFFF9F0A)  // Orange
        )
    }

    val particles = remember {
        List(110) {
            val angle = Random.nextFloat() * PI.toFloat() * 2f
            val speed = Random.nextFloat() * 650f + 250f
            ConfettiPiece(
                startX = 0.5f + (Random.nextFloat() - 0.5f) * 0.4f,
                startY = 0.35f + (Random.nextFloat() - 0.5f) * 0.2f,
                velocityX = cos(angle) * speed,
                velocityY = sin(angle) * speed - 180f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 720f,
                initialRotation = Random.nextFloat() * 360f,
                color = confettiColors[Random.nextInt(confettiColors.size)],
                width = Random.nextFloat() * 12f + 8f,
                height = Random.nextFloat() * 8f + 5f,
                isRibbon = Random.nextBoolean()
            )
        }
    }

    val progress = remember { Animatable(0f) }

    LaunchedEffect(isTriggered) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2400, easing = LinearEasing)
        )
        onFinished()
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val t = progress.value
        val gravity = 950f * t * t
        val alpha = if (t > 0.65f) (1f - (t - 0.65f) / 0.35f).coerceIn(0f, 1f) else 1f

        particles.forEach { p ->
            val curX = (p.startX * w) + p.velocityX * t * 0.7f + sin(t * 10f + p.initialRotation) * 28f
            val curY = (p.startY * h) + p.velocityY * t * 0.7f + gravity
            val curRotation = p.initialRotation + p.rotationSpeed * t

            rotate(curRotation, pivot = Offset(curX, curY)) {
                if (p.isRibbon) {
                    drawRect(
                        color = p.color.copy(alpha = alpha),
                        topLeft = Offset(curX - p.width / 2, curY - p.height / 2),
                        size = Size(p.width, p.height)
                    )
                } else {
                    drawCircle(
                        color = p.color.copy(alpha = alpha),
                        radius = p.width / 2.5f,
                        center = Offset(curX, curY)
                    )
                }
            }
        }
    }
}