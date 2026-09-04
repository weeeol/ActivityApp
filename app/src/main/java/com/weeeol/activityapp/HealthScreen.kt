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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
    var boxModifier = modifier
        .clip(RoundedCornerShape(20.dp))
        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        .border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
            shape = RoundedCornerShape(20.dp)
        )
        
    if (onClick != null) {
        boxModifier = boxModifier.clickable { onClick() }
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
    onResetWater: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var showWaterExplosion by remember { mutableStateOf(false) }
    var isDashboardExpanded by remember { mutableStateOf(false) }

    val dataManager = remember { DataManager(context) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var goalInput by remember { mutableStateOf("") }
    var lastSensorValue by remember { mutableFloatStateOf(dataManager.loadLastSensorValue()) }

    var weatherLocation by remember { mutableStateOf("Mangaluru") }
    var weatherCondition by remember { mutableStateOf("Sunny") }
    var showWeatherDialog by remember { mutableStateOf(false) }
    var tempLocationInput by remember { mutableStateOf("") }

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
                        dataManager.saveLastSensorValue(lastSensorValue)
                    } else {
                        var delta = currentSensorValue - lastSensorValue
                        if (delta < 0) delta = currentSensorValue
                        if (delta > 0) {
                            val newSteps = steps + delta.toInt()
                            onUpdateSteps(newSteps)
                            lastSensorValue = currentSensorValue
                            dataManager.saveLastSensorValue(lastSensorValue)
                        }
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (stepSensor != null) {
            sensorManager.registerListener(listener, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }
        onDispose { sensorManager.unregisterListener(listener) }
    }

    LaunchedEffect(waterGlasses) {
        if (waterGlasses == waterGoal) showWaterExplosion = true
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

        ParticleExplosion(isTriggered = showWaterExplosion, particleColor = StandColor, onFinished = { showWaterExplosion = false })
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
    val animMove by animateFloatAsState(targetValue = moveProgress, animationSpec = tween(1000), label = "moveAnim")
    val animExercise by animateFloatAsState(targetValue = exerciseProgress, animationSpec = tween(1000, delayMillis = 200), label = "exAnim")
    val animStand by animateFloatAsState(targetValue = standProgress, animationSpec = tween(1000, delayMillis = 400), label = "stAnim")

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

@Composable
fun ParticleExplosion(isTriggered: Boolean, particleColor: Color, onFinished: () -> Unit) {
    if (!isTriggered) return
    val particles = remember { List(60) { Particle(Random.nextFloat() * 2f * PI.toFloat(), Random.nextFloat() * 300f + 100f, Random.nextFloat() * 6f + 2f) } }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(true) {
        progress.snapTo(0f)
        progress.animateTo(targetValue = 1f, animationSpec = tween(durationMillis = 800, easing = LinearOutSlowInEasing))
        onFinished()
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        particles.forEach { particle ->
            val distance = particle.speed * progress.value
            val x = center.x + distance * cos(particle.angle)
            val y = center.y + distance * sin(particle.angle)
            val alpha = (1f - progress.value).coerceIn(0f, 1f)
            drawCircle(color = particleColor.copy(alpha = alpha), radius = particle.radius, center = Offset(x, y))
        }
    }
}