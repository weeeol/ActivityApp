package com.weeeol.activityapp.ui.timer

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.weeeol.activityapp.TimerEvent
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun AddTimerDialog(
    onDismiss: () -> Unit,
    onConfirm: (TimerEvent) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var selectedActivity by remember { mutableStateOf("") }
    var selectedDuration by remember { mutableIntStateOf(25) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
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
                    onConfirm(TimerEvent(finalName, selectedDuration, selectedTime))
                }
            ) {
                Text("Start Timer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
