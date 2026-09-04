package com.weeeol.activityapp

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
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
    Folders("Folders", Icons.Default.Folder),
    Timer("Timer", Icons.Default.PlayArrow)
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
    var isPinned: Boolean = false, // <-- Added pinned state

    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val createdAt: Long = System.currentTimeMillis()
)

class TimerEvent(
    val activityName: String,
    durationMinutes: Int,
    var scheduledTime: LocalTime? = null,
    totalSecondsParam: Long? = null
) {
    val id: String = java.util.UUID.randomUUID().toString()
    val totalSeconds: Long = totalSecondsParam ?: ((if (durationMinutes > 0) durationMinutes else 1) * 60).toLong()
    var remainingSeconds by mutableLongStateOf(totalSeconds)
    var isRunning by mutableStateOf(false)
}

@Entity(tableName = "folders")
data class ProjectFolder(
    var name: String,
    var emoji: String = "📂",

    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val createdAt: Long = System.currentTimeMillis()
)

data class TimerSaveData(
    val activityName: String,
    val remainingSeconds: Long,
    val scheduledTime: String? = null,
    val totalSeconds: Long? = null
)