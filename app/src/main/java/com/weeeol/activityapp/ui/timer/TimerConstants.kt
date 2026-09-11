package com.weeeol.activityapp.ui.timer

import androidx.compose.ui.graphics.Color

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

fun formatTimerDigits(totalSeconds: Long): Pair<String, String> {
    val minutes = (totalSeconds / 60).toString().padStart(2, '0')
    val seconds = (totalSeconds % 60).toString().padStart(2, '0')
    return minutes to seconds
}
