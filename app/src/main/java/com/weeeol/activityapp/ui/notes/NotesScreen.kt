package com.weeeol.activityapp.ui.notes

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.weeeol.activityapp.Note
import com.weeeol.activityapp.ProjectFolder
import com.weeeol.activityapp.ui.components.SectionHeader

@Composable
fun NotesScreen(
    notes: List<Note>,
    folders: List<ProjectFolder> = emptyList(),
    onAddNote: (Note) -> Unit,
    onUpdateNote: (Note) -> Unit,
    onDeleteNote: (Note) -> Unit,
    onEditingStateChange: (Boolean) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var editingNote by remember { mutableStateOf<Note?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedNoteIds by remember { mutableStateOf(setOf<String>()) }
    var selectedFolderId by remember { mutableStateOf<String?>(null) }

    val filteredNotes = remember(notes, searchQuery, selectedFolderId) {
        notes.filter { note ->
            val matchesSearch = if (searchQuery.isBlank()) true else {
                note.title.contains(searchQuery, ignoreCase = true) ||
                        note.content.contains(searchQuery, ignoreCase = true)
            }
            val matchesFolder = when (selectedFolderId) {
                null -> true
                else -> note.folderId == selectedFolderId
            }
            matchesSearch && matchesFolder
        }
    }

    // Automatically hide navbar and settings when editing OR selecting
    LaunchedEffect(selectedNoteIds.isNotEmpty(), editingNote != null) {
        onEditingStateChange(selectedNoteIds.isNotEmpty() || editingNote != null)
    }

    // Handle system back press to clear selection
    BackHandler(enabled = selectedNoteIds.isNotEmpty()) {
        selectedNoteIds = emptySet()
    }

    AnimatedContent(
        targetState = editingNote,
        transitionSpec = {
            if (targetState != null) {
                (slideInVertically(
                    initialOffsetY = { it / 5 },
                    animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow)
                ) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)))
                    .togetherWith(
                        scaleOut(
                            targetScale = 0.94f,
                            animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)
                        ) + fadeOut()
                    )
            } else {
                (scaleIn(
                    initialScale = 0.94f,
                    animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)
                ) + fadeIn())
                    .togetherWith(
                        slideOutVertically(
                            targetOffsetY = { it / 5 },
                            animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)
                        ) + fadeOut()
                    )
            }
        },
        label = "notesScreenTransition"
    ) { activeNote ->
        if (activeNote != null) {
            EditNoteFullscreen(
                note = activeNote,
                folders = folders,
                onBack = { updatedNote ->
                    if (updatedNote.title.isBlank() && updatedNote.content.isBlank()) {
                        onDeleteNote(updatedNote)
                    } else {
                        onUpdateNote(updatedNote)
                    }
                    editingNote = null
                }
            )
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp)
                ) {
                    // Top Header or Contextual Selection Bar
                    if (selectedNoteIds.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { selectedNoteIds = emptySet() }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Clear Selection")
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${selectedNoteIds.size} Selected",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = {
                                    selectedNoteIds = if (selectedNoteIds.size == filteredNotes.size) {
                                        emptySet()
                                    } else {
                                        filteredNotes.map { it.id }.toSet()
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.SelectAll,
                                        contentDescription = "Select All",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                IconButton(onClick = {
                                    val notesToDelete = notes.filter { it.id in selectedNoteIds }
                                    notesToDelete.forEach { onDeleteNote(it) }
                                    selectedNoteIds = emptySet()
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Selected",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Notes",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${notes.size} ${if (notes.size == 1) "note" else "notes"}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Apple-style Search Bar
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    "Search notes...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                            )
                        )

                        if (folders.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                item {
                                    val isSelected = selectedFolderId == null
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            selectedFolderId = null
                                        },
                                        label = { Text("All (${notes.size})") },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    )
                                }

                                items(folders, key = { it.id }) { folder ->
                                    val isSelected = selectedFolderId == folder.id
                                    val count = notes.count { it.folderId == folder.id }
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            selectedFolderId = if (isSelected) null else folder.id
                                        },
                                        label = { Text("${folder.emoji} ${folder.name} ($count)") },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Empty State or Notes Grid
                    if (filteredNotes.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 120.dp),
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
                                        imageVector = if (searchQuery.isNotEmpty()) Icons.Default.Search else Icons.Default.Description,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Text(
                                    text = if (searchQuery.isNotEmpty()) "No Matching Notes" else "No Notes Yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (searchQuery.isNotEmpty()) "No results found for \"$searchQuery\"" else "Tap the compose button to start writing",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        val (pinnedNotes, unpinnedNotes) = remember(filteredNotes) {
                            filteredNotes.partition { it.isPinned }
                        }

                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalItemSpacing = 10.dp,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 200.dp)
                        ) {
                            if (searchQuery.isNotBlank() || pinnedNotes.isEmpty()) {
                                items(filteredNotes, key = { it.id }) { note ->
                                    val folder = folders.firstOrNull { it.id == note.folderId }
                                    NoteItem(
                                        note = note,
                                        folder = folder,
                                        selectedNoteIds = selectedNoteIds,
                                        onSelect = { selectedNoteIds = it },
                                        onEdit = { editingNote = note },
                                        onUpdateNote = onUpdateNote
                                    )
                                }
                            } else {
                                if (pinnedNotes.isNotEmpty()) {
                                    item(span = StaggeredGridItemSpan.FullLine) {
                                        SectionHeader(title = "PINNED", count = pinnedNotes.size)
                                    }
                                    items(pinnedNotes, key = { it.id }) { note ->
                                        val folder = folders.firstOrNull { it.id == note.folderId }
                                        NoteItem(
                                            note = note,
                                            folder = folder,
                                            selectedNoteIds = selectedNoteIds,
                                            onSelect = { selectedNoteIds = it },
                                            onEdit = { editingNote = note },
                                            onUpdateNote = onUpdateNote
                                        )
                                    }
                                }

                                if (unpinnedNotes.isNotEmpty()) {
                                    item(span = StaggeredGridItemSpan.FullLine) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        SectionHeader(title = "NOTES", count = unpinnedNotes.size)
                                    }
                                    items(unpinnedNotes, key = { it.id }) { note ->
                                        val folder = folders.firstOrNull { it.id == note.folderId }
                                        NoteItem(
                                            note = note,
                                            folder = folder,
                                            selectedNoteIds = selectedNoteIds,
                                            onSelect = { selectedNoteIds = it },
                                            onEdit = { editingNote = note },
                                            onUpdateNote = onUpdateNote
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Floating Action Button
                FloatingActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val newNote = Note(title = "", content = "", folderId = selectedFolderId)
                        onAddNote(newNote)
                        editingNote = newNote
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 24.dp, bottom = 190.dp),
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "New Note"
                    )
                }
            }
        }
    }
}
