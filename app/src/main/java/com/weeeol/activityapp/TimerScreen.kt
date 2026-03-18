package com.weeeol.activityapp

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun TimerScreen(timers: MutableList<TimerEvent>){
    val activityList = listOf("College Assignments", "Game Dev", "Python & Git", "Running")
    var selectedActivity by remember { mutableStateOf("")
    }
    val durationOptions = listOf(5, 15, 25, 45, 60)
    var selectedDuration by remember { mutableIntStateOf(durationOptions[2]) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }

    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Create New Timer", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        // --- NEW: THE TEXT INPUT FIELD ---
        OutlinedTextField(
            value = selectedActivity,
            onValueChange = { selectedActivity = it }, // Updates text as you type
            label = { Text("Activity Name") },
            placeholder = { Text("e.g. Reading, Meditation") },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Activity Selection (Now acts as quick-fill chips)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            items(activityList) { activity ->
                val isSelected = selectedActivity == activity
                Surface(
                    modifier = Modifier.clip(CircleShape).clickable {
                        // Tapping a chip fills the text field!
                        selectedActivity = activity
                    },
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        text = activity,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Time Slot Selection
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            items(durationOptions) { duration ->
                val isSelected = selectedDuration == duration
                Surface(
                    modifier = Modifier.clip(CircleShape).clickable { selectedDuration = duration },
                    color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        text = "$duration min",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Row for selecting start time and adding the timer
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = {
                val now = LocalTime.now()
                TimePickerDialog(
                    context,
                    { _, hour, minute -> selectedTime = LocalTime.of(hour, minute) },
                    now.hour, now.minute, false
                ).show()
            }) {
                val timeText = selectedTime?.format(DateTimeFormatter.ofPattern("hh:mm a")) ?: "Start Manually"
                Text(timeText)
            }

            Button(onClick = {
                // NEW: Safety check! If they leave it blank, give it a default name.
                val finalName = selectedActivity.ifBlank { "Custom Timer" }
                timers.add(TimerEvent(finalName, selectedDuration, selectedTime))

                // Reset the form so it's clean for the next timer
                selectedTime = null
                selectedActivity = ""
            }) {
                Text("Add Timer")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // Active Timers List
        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(timers, key = { it.id }) { timerEvent ->
                TimerCard(timer = timerEvent, onDelete = { timers.remove(timerEvent) })
            }
        }
    }
}

@Composable
fun TimerCard(timer: TimerEvent, onDelete: () -> Unit) {

    // NEW ALARM LOGIC: Watch the clock to auto-start the timer
    LaunchedEffect(timer.scheduledTime, timer.isRunning) {
        if (timer.scheduledTime != null && !timer.isRunning && timer.remainingSeconds > 0) {
            while (true) {
                delay(1000L) // Check the clock every second
                val now = LocalTime.now()

                // FIXED: Start ONLY when the exact hour and minute match!
                if (now.hour == timer.scheduledTime.hour && now.minute == timer.scheduledTime.minute) {
                    timer.isRunning = true
                    break // Stop watching the clock once we start
                }
            }
        }
    }

    // ORIGINAL COUNTDOWN LOGIC: Ticks down the seconds
    LaunchedEffect(timer.isRunning) {
        while (timer.isRunning && timer.remainingSeconds > 0) {
            delay(1000L)
            timer.remainingSeconds--
            if (timer.remainingSeconds <= 0L) timer.isRunning = false
        }
    }

    val minutes = (timer.remainingSeconds / 60).toString().padStart(2, '0')
    val seconds = (timer.remainingSeconds % 60).toString().padStart(2, '0')

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (timer.remainingSeconds <= 0L) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = timer.activityName, style = MaterialTheme.typography.titleMedium)

                // NEW: Show the scheduled start time on the card if one exists
                if (timer.scheduledTime != null && !timer.isRunning && timer.remainingSeconds > 0) {
                    val timeString = timer.scheduledTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
                    Text(text = "Starts at $timeString", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }

                Text(
                    text = "$minutes:$seconds",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = { if (timer.remainingSeconds > 0) timer.isRunning = !timer.isRunning },
                modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
            ) {
                Icon(
                    imageVector = if (timer.isRunning) Icons.Default.Close else Icons.Default.PlayArrow,
                    contentDescription = "Start/Pause",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Timer", tint = Color.Gray)
            }
        }
    }
}