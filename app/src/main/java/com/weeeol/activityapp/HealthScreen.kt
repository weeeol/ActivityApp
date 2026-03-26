package com.weeeol.activityapp

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.ui.platform.LocalContext
import android.os.Build
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.ui.text.style.TextOverflow

// --- 1. THE AMBIENT ANIMATED BACKGROUND ---
@Composable
fun AmbientBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient")

    // Slow, drifting animation from 0 to 1 over 15 seconds
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "phase"
    )

    val color1 = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    val color2 = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Drifting orb 1
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color1, Color.Transparent),
                center = Offset(width * phase, height * 0.2f),
                radius = width * 0.8f
            ),
            radius = width * 0.8f,
            center = Offset(width * phase, height * 0.2f)
        )

        // Drifting orb 2
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color2, Color.Transparent),
                center = Offset(width * (1f - phase), height * 0.8f),
                radius = width * 0.9f
            ),
            radius = width * 0.9f,
            center = Offset(width * (1f - phase), height * 0.8f)
        )
    }
}

// --- 2. THE GLASSMORPHISM CARD COMPONENT ---
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    var boxModifier = modifier
        .clip(RoundedCornerShape(28.dp))
        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
        .border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            shape = RoundedCornerShape(28.dp)
        )

    if (onClick != null) {
        boxModifier = boxModifier.clickable { onClick() }
    }

    Box(
        modifier = boxModifier.padding(20.dp),
        contentAlignment = Alignment.Center,
        content = content
    )
}

// --- 3. THE FLOATING ARC STAT COMPONENT ---
@Composable
fun StatArc(
    progress: Float,
    color: Color,
    title: String,
    current: String,
    goal: String,
    modifier: Modifier = Modifier
) {
    val animProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 50f),
        label = "arcAnim"
    )

    Box(contentAlignment = Alignment.Center, modifier = modifier.fillMaxWidth()) {
        Canvas(modifier = Modifier.size(120.dp)) {
            val strokeWidth = 12.dp.toPx()

            drawArc(
                color = color.copy(alpha = 0.15f),
                startAngle = 140f,
                sweepAngle = 260f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            drawArc(
                color = color,
                startAngle = 140f,
                sweepAngle = 260f * animProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = current, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(text = goal, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

// --- THE MAIN SCREEN ---
@Composable
fun HealthScreen(waterGlasses: Int, onAddWater: () -> Unit, onResetWater: () -> Unit) {
    val context = LocalContext.current
    val waterGoal = 8
    var showWaterExplosion by remember { mutableStateOf(false) }
    var isDashboardExpanded by remember { mutableStateOf(false) }

    // --- SENSOR & GOAL STATES ---
    val dataManager = remember { DataManager(context) }
    var steps by remember { mutableIntStateOf(dataManager.loadSteps()) }
    var stepsGoal by remember { mutableIntStateOf(dataManager.loadStepGoal()) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var goalInput by remember { mutableStateOf("") }
    var lastSensorValue by remember { mutableFloatStateOf(dataManager.loadLastSensorValue()) }

    // --- WEATHER STATES ---
    var weatherLocation by remember { mutableStateOf("Mangaluru") }
    var weatherCondition by remember { mutableStateOf("Sunny") }
    var showWeatherDialog by remember { mutableStateOf(false) }
    var tempLocationInput by remember { mutableStateOf("") }

    // --- SENSOR INTEGRATION LOGIC ---
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
                            steps += delta.toInt()
                            dataManager.saveSteps(steps)
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

    val moveColor = Color(0xFFFA114F)
    val exerciseColor = Color(0xFF92E01D)
    val standColor = Color(0xFF1DDAE2)

    Box(modifier = Modifier.fillMaxSize()) {
        AmbientBackground()

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp).clickable { isDashboardExpanded = false },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isDashboardExpanded) "Dashboard" else "Activity",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
            }

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
                    // --- STATE 1: THE UNIFIED HERO RING & WIDE WEATHER WIDGET ---
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        GlassCard(
                            modifier = Modifier.size(280.dp),
                            onClick = { isDashboardExpanded = true }
                        ) {
                            ActivityRings(
                                moveProgress = sleepProgress,
                                exerciseProgress = stepsProgress,
                                standProgress = waterProgress,
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        GlassCard(
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            onClick = {
                                tempLocationInput = weatherLocation
                                showWeatherDialog = true
                            }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val iconColor = if (weatherCondition == "Sunny") Color(0xFFFFD700) else Color.LightGray
                                val icon = if (weatherCondition == "Sunny") Icons.Default.WbSunny else Icons.Default.Cloud
                                val temp = if (weatherCondition == "Sunny") "32°C" else "26°C"

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = weatherCondition,
                                        tint = iconColor,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Column {
                                        Text(
                                            text = weatherCondition,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Location", tint = Color.Gray, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = weatherLocation,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.Gray,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = temp,
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    // --- STATE 2: THE SPLIT GLASS CARDS ---
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            GlassCard(modifier = Modifier.weight(1f).aspectRatio(0.85f)) {
                                StatArc(progress = sleepProgress, color = moveColor, title = "Sleep", current = "$sleepHours", goal = "hrs")
                            }

                            GlassCard(
                                modifier = Modifier.weight(1f).aspectRatio(0.85f),
                                onClick = {
                                    goalInput = stepsGoal.toString()
                                    showGoalDialog = true
                                }
                            ) {
                                StatArc(progress = stepsProgress, color = exerciseColor, title = "Steps", current = "$steps", goal = "/ $stepsGoal")
                            }
                        }

                        Box {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    StatArc(progress = waterProgress, color = standColor, title = "Hydration", current = "$waterGlasses", goal = "/ $waterGoal gls", modifier = Modifier.weight(1f))

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                        modifier = Modifier.padding(start = 16.dp)
                                    ) {
                                        IconButton(
                                            onClick = onAddWater,
                                            modifier = Modifier.size(56.dp).background(standColor.copy(alpha = 0.2f), CircleShape).border(1.dp, standColor.copy(alpha = 0.5f), CircleShape)
                                        ) {
                                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Water", tint = standColor)
                                        }

                                        if (waterGlasses > 0) {
                                            IconButton(onClick = onResetWater, modifier = Modifier.size(40.dp)) {
                                                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset", tint = Color.Gray)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- EXISTING STEPS GOAL DIALOG ---
        if (showGoalDialog) {
            AlertDialog(
                onDismissRequest = { showGoalDialog = false },
                title = { Text("Set Daily Step Goal") },
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
                        stepsGoal = goalInput.toIntOrNull() ?: 10000
                        dataManager.saveStepGoal(stepsGoal)
                        showGoalDialog = false
                    }) {
                        Text("Save Goal")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showGoalDialog = false }) { Text("Cancel") }
                }
            )
        }

        // --- WEATHER CONFIG DIALOG ---
        if (showWeatherDialog) {
            AlertDialog(
                onDismissRequest = { showWeatherDialog = false },
                title = { Text("Weather Settings") },
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
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showWeatherDialog = false }) { Text("Cancel") }
                }
            )
        }

        ParticleExplosion(isTriggered = showWaterExplosion, particleColor = standColor, onFinished = { showWaterExplosion = false })
    }
}

// --- PARTICLE EXPLOSION  ---
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

@Composable
fun ActivityRings(
    moveProgress: Float,
    exerciseProgress: Float,
    standProgress: Float,
    modifier: Modifier = Modifier
) {
    val moveColor = Color(0xFFFA114F)     // Pink/Red
    val exerciseColor = Color(0xFF92E01D) // Neon Green
    val standColor = Color(0xFF1DDAE2)    // Cyan/Blue

    val animMove by animateFloatAsState(targetValue = moveProgress, animationSpec = tween(1000), label = "moveAnim")
    val animExercise by animateFloatAsState(targetValue = exerciseProgress, animationSpec = tween(1000, delayMillis = 200), label = "exAnim")
    val animStand by animateFloatAsState(targetValue = standProgress, animationSpec = tween(1000, delayMillis = 400), label = "stAnim")

    Canvas(modifier = modifier.fillMaxSize()) {
        val strokeWidth = size.width * 0.14f
        val spacing = strokeWidth * 0.25f
        val center = Offset(size.width / 2, size.height / 2)

        val r1 = (size.width - strokeWidth) / 2
        val r2 = r1 - strokeWidth - spacing
        val r3 = r2 - strokeWidth - spacing

        fun drawRing(radius: Float, progress: Float, color: Color) {
            drawArc(
                color = color.copy(alpha = 0.15f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth)
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = progress * 360f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        drawRing(r1, animMove, moveColor)
        drawRing(r2, animExercise, exerciseColor)
        drawRing(r3, animStand, standColor)
    }
}