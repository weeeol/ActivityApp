package com.weeeol.activityapp

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun FoldersScreen(
    folders: List<ProjectFolder>,
    notes: List<Note>,
    onAddFolder: (ProjectFolder) -> Unit,
    onUpdateFolder: (ProjectFolder) -> Unit,
    onDeleteFolder: (ProjectFolder) -> Unit,
    onAddNote: (Note) -> Unit,
    onUpdateNote: (Note) -> Unit,
    onDeleteNote: (Note) -> Unit,
    onEditingStateChange: (Boolean) -> Unit
) {
    var newFolderName by remember { mutableStateOf("") }
    var newFolderEmoji by remember { mutableStateOf("") }

    var showAddDialog by remember { mutableStateOf(false) }

    var openedFolder by remember { mutableStateOf<ProjectFolder?>(null) }
    var folderToDelete by remember { mutableStateOf<ProjectFolder?>(null) }
    var folderToEdit by remember { mutableStateOf<ProjectFolder?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }


    if (openedFolder != null) {
        BackHandler {
            openedFolder = null
        }

        FolderDetailScreen(
            folder = openedFolder!!,
            notes = notes,
            onBack = { openedFolder = null },
            onAddNote = onAddNote,
            onUpdateNote = onUpdateNote,
            onDeleteNote = onDeleteNote,
            onEditingStateChange = onEditingStateChange
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {

            Column(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
                Text(
                    text = "My Folders",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(folders, key = { it.id }) { folder ->
                        val noteCount = notes.count { it.folderId == folder.id }
                        FolderCard(
                            folder = folder,
                            noteCount = noteCount,
                            onDelete = { folderToDelete = folder },
                            onEdit = { folderToEdit = folder },
                            onClick = { openedFolder = folder }
                        )
                    }
                }
            }

            // --- THE NEW FLOATING ADD BUTTON ---
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 190.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Folder")
            }

            // --- THE ADD FOLDER POP-UP WINDOW ---
            if (showAddDialog) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = {
                        showAddDialog = false
                        nameError = null
                    },
                    title = { Text("Create New Folder") },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            // --- THE LIVE PREVIEW ---
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = "Folder Preview",
                                    modifier = Modifier.size(100.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                )
                                Text(
                                    text = newFolderEmoji.ifBlank { "📂" },
                                    style = MaterialTheme.typography.displaySmall
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // --- THE INPUT FIELDS ---
                            OutlinedTextField(
                                value = newFolderName,
                                onValueChange = {
                                    newFolderName = it
                                    nameError = null
                                },
                                label = { Text("Folder Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                isError = nameError != null,
                                supportingText = {
                                    if (nameError != null) {
                                        Text(text = nameError!!, color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            )
                            OutlinedTextField(
                                value = newFolderEmoji,
                                onValueChange = { if (it.length <= 2) newFolderEmoji = it },
                                label = { Text("Emoji") },
                                placeholder = { Text("📂") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val trimmedName = newFolderName.trim()

                                if (trimmedName.isNotBlank()) {
                                    val isDuplicate = folders.any { it.name.equals(trimmedName, ignoreCase = true) }

                                    if (isDuplicate) {
                                        nameError = "A folder with this name already exists"
                                    } else {
                                        val finalEmoji = newFolderEmoji.ifBlank { "📂" }
                                        val newFolder = ProjectFolder(trimmedName, finalEmoji)

                                        onAddFolder(newFolder)

                                        newFolderName = ""
                                        newFolderEmoji = ""
                                        nameError = null
                                        showAddDialog = false
                                    }
                                }
                            }
                        ) {
                            Text("Create")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showAddDialog = false
                            newFolderName = ""
                            newFolderEmoji = ""
                            nameError = null
                        }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // --- THE DELETE CONFIRMATION DIALOG (From earlier) ---
            if (folderToDelete != null) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { folderToDelete = null },
                    title = { Text("Delete Folder?") },
                    text = {
                        Text("Are you sure you want to delete '${folderToDelete?.name}'? All notes inside this folder will also be permanently deleted.")
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                onDeleteFolder(folderToDelete!!)
                                folderToDelete = null
                            },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { folderToDelete = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }
            // --- THE EDIT FOLDER POP-UP WINDOW ---
            if (folderToEdit != null) {
                var editName by remember { mutableStateOf(folderToEdit!!.name) }
                var editEmoji by remember { mutableStateOf(folderToEdit!!.emoji) }
                var editNameError by remember { mutableStateOf<String?>(null) }

                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { folderToEdit = null },
                    title = { Text("Edit Folder") },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Live Preview
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = "Folder Preview",
                                    modifier = Modifier.size(100.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                )
                                Text(
                                    text = editEmoji.ifBlank { "📂" },
                                    style = MaterialTheme.typography.displaySmall
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))

                            OutlinedTextField(
                                value = editName,
                                onValueChange = {
                                    editName = it
                                    editNameError = null
                                },
                                label = { Text("Folder Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                isError = editNameError != null,
                                supportingText = {
                                    if (editNameError != null) Text(text = editNameError!!, color = MaterialTheme.colorScheme.error)
                                }
                            )
                            OutlinedTextField(
                                value = editEmoji,
                                onValueChange = { if (it.length <= 2) editEmoji = it },
                                label = { Text("Emoji") },
                                placeholder = { Text("📂") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val trimmedName = editName.trim()
                                if (trimmedName.isNotBlank()) {
                                    val isDuplicate = folders.any { it.name.equals(trimmedName, ignoreCase = true) && it.id != folderToEdit!!.id }

                                    if (isDuplicate) {
                                        editNameError = "A folder with this name already exists"
                                    } else {
                                        val finalEmoji = editEmoji.ifBlank { "📂" }

                                        val updatedFolder = folderToEdit!!.copy(name = trimmedName, emoji = finalEmoji)
                                        onUpdateFolder(updatedFolder)

                                        folderToEdit = null
                                    }
                                }
                            }
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { folderToEdit = null }) { Text("Cancel") }
                    }
                )
            }
        }
    }
}

@Composable
fun FolderDetailScreen(
    folder: ProjectFolder,
    notes: List<Note>,
    onBack: () -> Unit,
    onAddNote: (Note) -> Unit,
    onUpdateNote: (Note) -> Unit,
    onDeleteNote: (Note) -> Unit,
    onEditingStateChange: (Boolean) -> Unit
) {
    var titleText by remember { mutableStateOf("") }
    var contentText by remember { mutableStateOf("") }
    var editingNote by remember { mutableStateOf<Note?>(null) }

    val folderNotes = notes.filter { it.folderId == folder.id }

    if (editingNote != null) {
        EditNoteFullscreen(
            note = editingNote!!,
            onBack = { updatedNote ->
                onUpdateNote(updatedNote)
                editingNote = null
                onEditingStateChange(false)
            }
        )
    } else {
        Column(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(text = folder.name, style = MaterialTheme.typography.titleLarge)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Text inputs to create a new note
            OutlinedTextField(
                value = titleText, onValueChange = { titleText = it }, label = { Text("Title") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = contentText, onValueChange = { contentText = it }, label = { Text("Take a note...") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).heightIn(min = 100.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (contentText.isNotBlank() || titleText.isNotBlank()) {
                        val newNote = Note(titleText, contentText, folder.id)
                        onAddNote(newNote)
                        titleText = ""
                        contentText = ""
                    }
                },
                modifier = Modifier.align(Alignment.End).padding(horizontal = 16.dp)
            ) { Text("Save to Folder") }

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalItemSpacing = 8.dp,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(folderNotes, key = { it.id }) { note ->
                    NoteCard(
                        note = note,
                        onDelete = {onDeleteNote(note) },
                        onClick = {
                            editingNote = note
                            onEditingStateChange(true)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FolderCard(folder: ProjectFolder, noteCount: Int, onDelete: () -> Unit, onEdit: () -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {

            // The main content of the card
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "Folder",
                        modifier = Modifier.size(100.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                    )
                    Text(text = folder.emoji, style = MaterialTheme.typography.displaySmall)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = folder.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = if (noteCount == 1) "1 item" else "$noteCount items", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            IconButton(
                onClick = onEdit,
                modifier = Modifier.align(Alignment.TopStart).padding(4.dp)
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray)
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
            }
        }
    }
}