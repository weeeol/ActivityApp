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
import androidx.room.Entity
import androidx.room.PrimaryKey

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

@Entity(tableName = "notes")
data class Note(
    var title: String,
    var content: String,
    var folderId: String? = null,
    var timestamp: String = java.text.SimpleDateFormat("MMM dd, yyyy • hh:mm a", java.util.Locale.getDefault()).format(java.util.Date()),
    var isCodeMode: Boolean = false,

    // The unique key for the note
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val createdAt: Long = System.currentTimeMillis()
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

@Entity(tableName = "folders")
data class ProjectFolder(
    var name: String,
    var emoji: String = "📂",

    // Tell Room that this ID is the unique key for this row
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val createdAt: Long = System.currentTimeMillis()
)

data class TimerSaveData(
    val activityName: String,
    val remainingSeconds: Long,
    val scheduledTime: String? = null // NEW: Safely store the time as a string
)