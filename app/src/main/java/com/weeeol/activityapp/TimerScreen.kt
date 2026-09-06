package com.weeeol.activityapp

import android.app.TimePickerDialog
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

val TimerPresets = listOf(
    "💻 Work" to 25,
    "📚 Study" to 45,
    "🏃 Workout" to 30,
    "🧘 Meditate" to 10,
    "☕ Break" to 5,
    "🍳 Cook" to 15
)

val DurationOptions = listOf(5, 10, 15, 20, 25, 30, 45, 60)

// Apple Timer Orange
val TimerAccentColor = Color(0xFFFF9500)

@Composable
fun TimerScreen(
    timers: List<TimerEvent>,
    onAddTimer: (TimerEvent) -> Unit,
    onDeleteTimer: (TimerEvent) -> Unit,
    onToggleTimer: (TimerEvent) -> Unit = {},
    onResetTimer: (TimerEvent) -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var selectedActivity by remember { mutableStateOf("") }
    var selectedDuration by remember { mutableIntStateOf(25) } // Default 25 min Pomodoro
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }
    var showAddTimerDialog by remember { mutableStateOf(false) }

    val runningCount = timers.count { it.isRunning }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp)
        ) {
            // Header (Aligned with Notes, Folders, and Health screens)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Timers",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (runningCount > 0) {
                            "${timers.size} ${if (timers.size == 1) "timer" else "timers"} • $runningCount running"
                        } else {
                            "${timers.size} ${if (timers.size == 1) "timer" else "timers"}"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (timers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassEmpty,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Text(
                            text = "No Active Timers",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Tap + to start a countdown or Pomodoro timer",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 200.dp)
                ) {
                    items(timers, key = { it.id }) { timerEvent ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { dismissValue ->
                                if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onDeleteTimer(timerEvent)
                                    true
                                } else {
                                    false
                                }
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            backgroundContent = {
                                val isSwiping = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart
                                if (isSwiping) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(22.dp))
                                            .padding(end = 24.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }
                        ) {
                            TimerCard(
                                timer = timerEvent,
                                onDelete = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onDeleteTimer(timerEvent)
                                },
                                onToggle = { onToggleTimer(timerEvent) },
                                onReset = { onResetTimer(timerEvent) }
                            )
                        }
                    }
                }
            }
        }

        // Add Timer Floating Action Button (Clean Apple Style)
        FloatingActionButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                showAddTimerDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 190.dp),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Timer")
        }

        // Create Timer Dialog
        if (showAddTimerDialog) {
            AlertDialog(
                onDismissRequest = { showAddTimerDialog = false },
                title = { Text("New Timer", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Quick Presets
                        Text("Quick Activities:", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(TimerPresets) { (label, duration) ->
                                val isSelected = selectedActivity == label.substringAfter(" ")
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            selectedActivity = label.substringAfter(" ")
                                            selectedDuration = duration
                                        },
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = label,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Activity Name Input
                        OutlinedTextField(
                            value = selectedActivity,
                            onValueChange = { selectedActivity = it },
                            label = { Text("Activity Name") },
                            placeholder = { Text("e.g. Focus, Reading") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Duration Chips
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Duration (minutes):", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(DurationOptions) { duration ->
                                    val isSelected = selectedDuration == duration
                                    Surface(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                selectedDuration = duration
                                            },
                                        color = if (isSelected) TimerAccentColor.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    ) {
                                        Text(
                                            text = if (duration == 25) "25m (Pomodoro)" else "${duration}m",
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) TimerAccentColor else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // Scheduled Start Option
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Schedule:", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                            OutlinedButton(
                                onClick = {
                                    val now = LocalTime.now()
                                    TimePickerDialog(
                                        context,
                                        { _, hour, minute -> selectedTime = LocalTime.of(hour, minute) },
                                        now.hour, now.minute, false
                                    ).show()
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                val timeText = selectedTime?.format(DateTimeFormatter.ofPattern("hh:mm a")) ?: "Start Now"
                                Text(timeText)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val finalName = selectedActivity.ifBlank { "Timer" }
                            onAddTimer(TimerEvent(finalName, selectedDuration, selectedTime))

                            selectedTime = null
                            selectedActivity = ""
                            selectedDuration = 25
                            showAddTimerDialog = false
                        }
                    ) {
                        Text("Start Timer")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showAddTimerDialog = false
                        selectedTime = null
                        selectedActivity = ""
                        selectedDuration = 25
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun TimerCard(
    timer: TimerEvent,
    onDelete: () -> Unit,
    onToggle: () -> Unit = {},
    onReset: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current

    val isFinished = timer.remainingSeconds <= 0L
    val minutes = (timer.remainingSeconds / 60).toString().padStart(2, '0')
    val seconds = (timer.remainingSeconds % 60).toString().padStart(2, '0')

    val progress = if (timer.totalSeconds > 0) {
        (timer.remainingSeconds.toFloat() / timer.totalSeconds.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "timerProgress"
    )

    // Breathing pulse when timer is running
    val infiniteTransition = rememberInfiniteTransition(label = "runningPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val activeColor = if (isFinished) MaterialTheme.colorScheme.error else TimerAccentColor

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.975f else 1.0f,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "timerCardScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            },
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(
            1.dp,
            if (timer.isRunning) activeColor.copy(alpha = pulseAlpha)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isFinished) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth()
        ) {
            // Top Row: Activity Name & State Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = timer.activityName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (isFinished) {
                    Surface(
                        color = MaterialTheme.colorScheme.error,
                        shape = CircleShape,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = "TIME'S UP",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onError,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                } else if (timer.scheduledTime != null && !timer.isRunning) {
                    val timeString = timer.scheduledTime!!.format(DateTimeFormatter.ofPattern("hh:mm a"))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = TimerAccentColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = timeString,
                            style = MaterialTheme.typography.labelSmall,
                            color = TimerAccentColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel Timer",
                        tint = Color.Gray.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Center Content: Circular Countdown Ring + Large Digits + Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Circular Countdown Ring with Digital Time Display
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Apple-style Circular Progress Ring
                    Box(
                        modifier = Modifier.size(64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 6.dp.toPx()
                            val center = Offset(size.width / 2, size.height / 2)
                            val radius = (size.width - strokeWidth) / 2

                            // Background Track
                            drawArc(
                                color = activeColor.copy(alpha = 0.18f),
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = Offset(center.x - radius, center.y - radius),
                                size = Size(radius * 2, radius * 2),
                                style = Stroke(width = strokeWidth)
                            )

                            // Active Progress
                            if (animatedProgress > 0f) {
                                drawArc(
                                    color = activeColor,
                                    startAngle = -90f,
                                    sweepAngle = animatedProgress * 360f,
                                    useCenter = false,
                                    topLeft = Offset(center.x - radius, center.y - radius),
                                    size = Size(radius * 2, radius * 2),
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            }
                        }

                        Icon(
                            imageVector = if (timer.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = if (timer.isRunning) activeColor else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Time Digits
                    Column {
                        Text(
                            text = "$minutes:$seconds",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "of ${timer.totalSeconds / 60} min",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }

                // Controls: Reset & Play/Pause Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Reset Button (appears if paused or completed)
                    if (!timer.isRunning && timer.remainingSeconds != timer.totalSeconds) {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onReset()
                            },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset Timer",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Primary Play/Pause Button
                    val playBtnBg by animateColorAsState(
                        targetValue = if (timer.isRunning) TimerAccentColor.copy(alpha = 0.2f) else TimerAccentColor,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "playBtnBg"
                    )
                    val playBtnIconTint by animateColorAsState(
                        targetValue = if (timer.isRunning) TimerAccentColor else Color.White,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "playBtnTint"
                    )

                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggle()
                        },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(playBtnBg)
                    ) {
                        Icon(
                            imageVector = if (timer.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (timer.isRunning) "Pause" else "Start",
                            tint = playBtnIconTint,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}