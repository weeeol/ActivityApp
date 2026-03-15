package com.weeeol.activityapp

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import java.time.LocalTime


enum class NavItem(val title: String, val icon: ImageVector) {
    Health("Health", Icons.Default.FavoriteBorder),
    Notes("Notes", Icons.Default.Edit),
    Folders("Folders", Icons.AutoMirrored.Filled.List),       // Changed from Folder to List
    Timer("Timer", Icons.Default.PlayArrow)       // Changed from Timer to PlayArrow
}
data class Particle(
    val angle: Float,
    val speed: Float,
    val radius: Float
)

data class Note(
    var title: String,
    var content: String,
    var folderId: String? = null,
    var timestamp: String = java.text.SimpleDateFormat("MMM dd, yyyy • hh:mm a", java.util.Locale.getDefault()).format(java.util.Date()),

    // NEW: The flag to track IDE mode! Defaults to false for normal notes.
    var isCodeMode: Boolean = false,

    val id: String = java.util.UUID.randomUUID().toString()
)

class TimerEvent(
    val activityName: String,
    durationMinutes: Int,
    // NEW: Optional start time. Null means "start manually"
    val scheduledTime: LocalTime? = null
) {
    val id: String = java.util.UUID.randomUUID().toString()
    var remainingSeconds by mutableLongStateOf((durationMinutes * 60).toLong())
    var isRunning by mutableStateOf(false)
}

data class ProjectFolder(
    var name: String,
    var emoji: String = "📂",
    val id: String = java.util.UUID.randomUUID().toString()
)

data class TimerSaveData(
    val activityName: String,
    val remainingSeconds: Long
)
