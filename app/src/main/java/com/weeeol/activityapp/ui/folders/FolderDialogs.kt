package com.weeeol.activityapp.ui.folders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weeeol.activityapp.ProjectFolder

val EmojiPresets = listOf("📂", "📁", "💼", "📚", "💡", "🎯", "🏠", "🎨", "💻", "❤️", "✈️", "🎵")

@Composable
fun EmojiPicker(
    selectedEmoji: String,
    onSelectEmoji: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        items(EmojiPresets) { emoji ->
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (selectedEmoji == emoji) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSelectEmoji(emoji)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 20.sp)
            }
        }
    }
}

@Composable
fun AddFolderDialog(
    existingFolders: List<ProjectFolder>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, emoji: String) -> Unit
) {
    var newFolderName by remember { mutableStateOf("") }
    var newFolderEmoji by remember { mutableStateOf("📂") }
    var nameError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Folder", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Emoji Preview
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = newFolderEmoji.ifBlank { "📂" },
                        fontSize = 38.sp
                    )
                }

                Text(
                    text = "Choose an icon:",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )

                EmojiPicker(
                    selectedEmoji = newFolderEmoji,
                    onSelectEmoji = { newFolderEmoji = it }
                )

                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = {
                        newFolderName = it
                        nameError = null
                    },
                    label = { Text("Folder Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    isError = nameError != null,
                    supportingText = {
                        if (nameError != null) {
                            Text(text = nameError!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedName = newFolderName.trim()
                    if (trimmedName.isNotBlank()) {
                        val isDuplicate = existingFolders.any { it.name.equals(trimmedName, ignoreCase = true) }
                        if (isDuplicate) {
                            nameError = "A folder with this name already exists"
                        } else {
                            val finalEmoji = newFolderEmoji.ifBlank { "📂" }
                            onConfirm(trimmedName, finalEmoji)
                        }
                    }
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditFolderDialog(
    folder: ProjectFolder,
    existingFolders: List<ProjectFolder>,
    onDismiss: () -> Unit,
    onConfirm: (updatedFolder: ProjectFolder) -> Unit
) {
    var editName by remember { mutableStateOf(folder.name) }
    var editEmoji by remember { mutableStateOf(folder.emoji) }
    var editNameError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Folder", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = editEmoji.ifBlank { "📂" },
                        fontSize = 38.sp
                    )
                }

                Text(
                    text = "Choose an icon:",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )

                EmojiPicker(
                    selectedEmoji = editEmoji,
                    onSelectEmoji = { editEmoji = it }
                )

                OutlinedTextField(
                    value = editName,
                    onValueChange = {
                        editName = it
                        editNameError = null
                    },
                    label = { Text("Folder Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    isError = editNameError != null,
                    supportingText = {
                        if (editNameError != null) Text(text = editNameError!!, color = MaterialTheme.colorScheme.error)
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedName = editName.trim()
                    if (trimmedName.isNotBlank()) {
                        val isDuplicate = existingFolders.any {
                            it.name.equals(trimmedName, ignoreCase = true) && it.id != folder.id
                        }
                        if (isDuplicate) {
                            editNameError = "A folder with this name already exists"
                        } else {
                            val finalEmoji = editEmoji.ifBlank { "📂" }
                            onConfirm(folder.copy(name = trimmedName, emoji = finalEmoji))
                        }
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeleteFolderDialog(
    folder: ProjectFolder,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Folder?") },
        text = {
            Text("Are you sure you want to delete '${folder.name}'? All notes inside this folder will also be permanently deleted.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
