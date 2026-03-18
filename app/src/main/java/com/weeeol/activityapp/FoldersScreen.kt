package com.weeeol.activityapp

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.Add

@Composable
fun FoldersScreen(folders: MutableList<ProjectFolder>, notes: MutableList<Note>) {
    var newFolderName by remember { mutableStateOf("") }
    var newFolderEmoji by remember { mutableStateOf("") }

    // NEW: State to trigger the popup window!
    var showAddDialog by remember { mutableStateOf(false) }

    var openedFolder by remember { mutableStateOf<ProjectFolder?>(null) }
    var folderToDelete by remember { mutableStateOf<ProjectFolder?>(null) }

    if (openedFolder != null) {
        FolderDetailScreen(
            folder = openedFolder!!,
            notes = notes,
            onBack = { openedFolder = null }
        )
    } else {
        // We use a Box here so we can float the Add button over the grid
        Box(modifier = Modifier.fillMaxSize()) {

            Column(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
                Text(
                    text = "My Folders",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // The Grid (Now with more breathing room at the top!)
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
                    // We pad the bottom heavily so it sits safely above your custom nav bar
                    .padding(end = 24.dp, bottom = 120.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Folder")
            }

            // --- THE ADD FOLDER POP-UP WINDOW ---
            if (showAddDialog) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showAddDialog = false },
                    title = { Text("Create New Folder") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = newFolderName,
                                onValueChange = { newFolderName = it },
                                label = { Text("Folder Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
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
                                if (newFolderName.isNotBlank()) {
                                    val finalEmoji = newFolderEmoji.ifBlank { "📂" }
                                    folders.add(ProjectFolder(newFolderName, finalEmoji))

                                    // Reset and close!
                                    newFolderName = ""
                                    newFolderEmoji = ""
                                    showAddDialog = false
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
                                notes.removeAll { it.folderId == folderToDelete?.id }
                                folders.remove(folderToDelete)
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
        }
    }
}

// NEW: The screen that shows when you open a folder
@Composable
fun FolderDetailScreen(folder: ProjectFolder, notes: MutableList<Note>, onBack: () -> Unit) {
    var titleText by remember { mutableStateOf("") }
    var contentText by remember { mutableStateOf("") }
    var editingNote by remember { mutableStateOf<Note?>(null) }

    // MAGIC: Filter the main notes list so we ONLY see notes for this folder
    val folderNotes = notes.filter { it.folderId == folder.id }

    if (editingNote != null) {
        EditNoteFullscreen(
            note = editingNote!!,
            onBack = { updatedNote ->
                val index = notes.indexOfFirst { it.id == updatedNote.id }
                if (index != -1) notes[index] = updatedNote.copy()
                editingNote = null
            }
        )
    } else {
        Column(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {

            // Header with Back Arrow and Folder Name
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
                        // NEW: Notice we pass the folder.id here to lock it to this folder!
                        notes.add(0, Note(titleText, contentText, folder.id))
                        titleText = ""
                        contentText = ""
                    }
                },
                modifier = Modifier.align(Alignment.End).padding(horizontal = 16.dp)
            ) { Text("Save to Folder") }

            Spacer(modifier = Modifier.height(16.dp))

            // The Keep-style Grid for this specific folder
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalItemSpacing = 8.dp,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(folderNotes, key = { it.id }) { note ->
                    // Reuse our beautiful NoteCard
                    NoteCard(note = note, onDelete = { notes.remove(note) }, onClick = { editingNote = note })
                }
            }
        }
    }
}

// Updated FolderCard to show note counts and accept clicks
@Composable
fun FolderCard(folder: ProjectFolder, noteCount: Int, onDelete: () -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {

            // The main content of the card
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {

                // Centered Folder Image & Emoji
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_folder_blue),
                        contentDescription = "Folder",
                        modifier = Modifier.size(100.dp) // Scaled down from 150dp so it fits the grid better!
                    )
                    Text(
                        text = folder.emoji,
                        style = MaterialTheme.typography.displaySmall
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(text = folder.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = if (noteCount == 1) "1 item" else "$noteCount items", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            // The Delete Button, pinned to the absolute top-right corner of the Card
            IconButton(
                onClick = onDelete, // This will now trigger the pop-up we added earlier!
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
            }
        }
    }
}