package com.weeeol.activityapp

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI
import kotlin.random.Random

@Composable
fun ParticleExplosion(
    isTriggered: Boolean,
    particleColor: Color,
    onFinished: () -> Unit
) {
    if (!isTriggered) return

    // 1. Generate 60 random particles when triggered
    val particles = remember {
        List(60) {
            Particle(
                angle = Random.nextFloat() * 2f * PI.toFloat(), // Full 360 degrees
                speed = Random.nextFloat() * 300f + 100f,       // Random velocity
                radius = Random.nextFloat() * 6f + 2f           // Random sizes
            )
        }
    }

    // 2. The "Game Loop" timer (goes from 0.0 to 1.0)
    val progress = remember { Animatable(0f) }

    LaunchedEffect(isTriggered) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = LinearOutSlowInEasing)
        )
        onFinished() // Tell the parent the animation is done
    }

    // 3. Draw them to the screen
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)

        particles.forEach { particle ->
            // Calculate how far it has traveled
            val distance = particle.speed * progress.value

            // Standard Trig to get the X and Y based on angle
            val x = center.x + distance * cos(particle.angle)
            val y = center.y + distance * sin(particle.angle)

            // Fade out as progress approaches 1.0
            val alpha = (1f - progress.value).coerceIn(0f, 1f)

            drawCircle(
                color = particleColor.copy(alpha = alpha),
                radius = particle.radius,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
fun HealthScreen(waterGlasses: Int, onAddWater: () -> Unit, onResetWater: () -> Unit) {
    val waterGoal = 8

    var showWaterExplosion by remember { mutableStateOf(false) }

    LaunchedEffect(waterGlasses) {
        if (waterGlasses == waterGoal) {
            showWaterExplosion = true
        }
    }

    // --- 1. REPLACED CALORIES WITH SLEEP ---
    val sleepHours = 6.5f // You can change this to test the ring!
    val sleepGoal = 8.0f

    val steps = 6432
    val stepsGoal = 10000

    // --- 2. UPDATED PROGRESS MATH ---
    val sleepProgress = (sleepHours / sleepGoal).coerceIn(0f, 1f)
    val stepsProgress = (steps.toFloat() / stepsGoal).coerceIn(0f, 1f)
    val waterProgress = (waterGlasses.toFloat() / waterGoal).coerceIn(0f, 1f)

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Today's Activity",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .align(Alignment.Start)
        )

        Box(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            ActivityRings(
                moveProgress = sleepProgress,     // The Outer Red Ring is now Sleep!
                exerciseProgress = stepsProgress,
                standProgress = waterProgress
            )

            ParticleExplosion(
                isTriggered = showWaterExplosion,
                particleColor = Color(0xFF1DDAE2),
                onFinished = { showWaterExplosion = false }
            )
        }

        // --- 3. REPLACED THE CALORIE CARD ---
        ActivityCard(title = "Sleep", current = "$sleepHours", goal = " / ${sleepGoal.toInt()} hrs")

        ActivityCard(title = "Steps", current = "$steps", goal = " / $stepsGoal")

        WaterIntakeCard(
            current = waterGlasses,
            goal = waterGoal,
            onAddWater = onAddWater,
            onResetWater = onResetWater
        )
    }
}

@Composable
fun ActivityRings(
    moveProgress: Float,
    exerciseProgress: Float,
    standProgress: Float,
    modifier: Modifier = Modifier
) {
    // Apple-style hex colors
    val moveColor = Color(0xFFFA114F)     // Red/Pink
    val exerciseColor = Color(0xFF92E01D) // Neon Green
    val standColor = Color(0xFF1DDAE2)    // Cyan/Blue

    // Smoothly animate the rings when data changes or screen loads
    val animMove by animateFloatAsState(targetValue = moveProgress, animationSpec = tween(1000), label = "moveAnim")
    val animExercise by animateFloatAsState(targetValue = exerciseProgress, animationSpec = tween(1000, delayMillis = 200), label = "exerciseAnim")
    val animStand by animateFloatAsState(targetValue = standProgress, animationSpec = tween(1000, delayMillis = 400), label = "standAnim")

    Canvas(modifier = modifier.fillMaxSize()) {
        val strokeWidth = size.width * 0.12f // Ring thickness
        val spacing = strokeWidth * 0.25f    // Gap between rings
        val center = Offset(size.width / 2, size.height / 2)

        // Calculate the radius for each ring so they nest perfectly
        val r1 = (size.width - strokeWidth) / 2
        val r2 = r1 - strokeWidth - spacing
        val r3 = r2 - strokeWidth - spacing

        // Helper function to draw the background track and the progress arc
        fun drawRing(radius: Float, progress: Float, color: Color) {
            // Dark, semi-transparent background track
            drawArc(
                color = color.copy(alpha = 0.2f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth),
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )
            // The actual progress ring
            drawArc(
                color = color,
                startAngle = -90f, // -90 starts it at the top (12 o'clock)
                sweepAngle = progress * 360f, // Convert 0.0-1.0 to 0-360 degrees
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round), // Round caps!
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )
        }

        // Draw them from outside in
        drawRing(r1, animMove, moveColor)
        drawRing(r2, animExercise, exerciseColor)
        drawRing(r3, animStand, standColor)
    }
}

@Composable
fun ActivityCard(title: String, current: String, goal: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = current, style = MaterialTheme.typography.headlineMedium)
                Text(text = goal, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 4.dp))
            }
        }
    }
}

@Composable
fun WaterIntakeCard(current: Int, goal: Int, onAddWater: () -> Unit, onResetWater: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Side: Text
            Column {
                Text(text = "Water Intake", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(text = "$current", style = MaterialTheme.typography.headlineMedium)
                    Text(text = " / $goal glasses", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 4.dp))
                }
            }

            // Right Side: Buttons wrapped in a Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                // The new Reset Button (only shows up if you have drank water!)
                if (current > 0) {
                    IconButton(onClick = onResetWater) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Water",
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // The original Add Button
                IconButton(
                    onClick = onAddWater,
                    modifier = Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Water",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}