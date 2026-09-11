package com.weeeol.activityapp.ui.health

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weeeol.activityapp.ui.components.IosCard
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    var lastSensorValue by remember { mutableFloatStateOf(onGetLastSensorValue()) }

    var weatherLocation by remember { mutableStateOf("Mangaluru") }
    var weatherCondition by remember { mutableStateOf("Sunny") }
    var showWeatherDialog by remember { mutableStateOf(false) }

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
            delay(4500L)
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
        SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date()).uppercase()
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 8.dp)
        ) {
            // Header
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
                AnimatedContent(
                    targetState = isDashboardExpanded,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.8f, animationSpec = tween(400)))
                            .togetherWith(
                                fadeOut(animationSpec = tween(400)) + scaleOut(targetScale = 0.8f, animationSpec = tween(400))
                            )
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
                                StepsCard(
                                    steps = steps,
                                    stepsGoal = stepsGoal,
                                    onClick = { showGoalDialog = true },
                                    modifier = Modifier.weight(1f)
                                )
                                SleepCard(
                                    sleepHours = sleepHours,
                                    sleepGoal = sleepGoal,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Stats Grid - Row 2: Water Card
                            WaterCard(
                                waterGlasses = waterGlasses,
                                waterGoal = waterGoal,
                                onAddWater = onAddWater,
                                onResetWater = onResetWater
                            )
                        }
                    }
                }

                // Weather Card (Always displayed, full width)
                WeatherCard(
                    weatherLocation = weatherLocation,
                    weatherCondition = weatherCondition,
                    onClick = { showWeatherDialog = true }
                )
            }

            // Bottom clearance for the Floating Navigation Bar
            Spacer(modifier = Modifier.height(140.dp))
        }

        // Dialogs
        if (showGoalDialog) {
            StepGoalDialog(
                currentGoal = stepsGoal,
                onDismiss = { showGoalDialog = false },
                onConfirm = {
                    onUpdateStepGoal(it)
                    showGoalDialog = false
                }
            )
        }

        if (showWeatherDialog) {
            WeatherDialog(
                currentLocation = weatherLocation,
                currentCondition = weatherCondition,
                onDismiss = { showWeatherDialog = false },
                onConfirm = { loc, cond ->
                    weatherLocation = loc
                    weatherCondition = cond
                    showWeatherDialog = false
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
