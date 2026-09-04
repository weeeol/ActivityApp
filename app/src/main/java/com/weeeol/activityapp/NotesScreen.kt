package com.weeeol.activityapp

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotesScreen(
    notes: List<Note>,
    onAddNote: (Note) -> Unit,
    onUpdateNote: (Note) -> Unit,
    onDeleteNote: (Note) -> Unit,
    onEditingStateChange: (Boolean) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var editingNote by remember { mutableStateOf<Note?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedNoteIds by remember { mutableStateOf(setOf<String>()) }

    val filteredNotes = notes.filter { note ->
        note.title.contains(searchQuery, ignoreCase = true) ||
                note.content.contains(searchQuery, ignoreCase = true)
    }

    // Automatically hide navbar and settings when editing OR selecting
    LaunchedEffect(selectedNoteIds.isNotEmpty(), editingNote != null) {
        onEditingStateChange(selectedNoteIds.isNotEmpty() || editingNote != null)
    }

    // Handle system back press to clear selection
    BackHandler(enabled = selectedNoteIds.isNotEmpty()) {
        selectedNoteIds = emptySet()
    }

    if (editingNote != null) {
        EditNoteFullscreen(
            note = editingNote!!,
            onBack = { updatedNote ->
                // Clean up blank note if backed out without typing anything
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

                    Spacer(modifier = Modifier.height(16.dp))
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
                    val pinnedNotes = filteredNotes.filter { it.isPinned }
                    val unpinnedNotes = filteredNotes.filter { !it.isPinned }

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
                            // If searching or nothing pinned, render straight list
                            items(filteredNotes, key = { it.id }) { note ->
                                NoteItem(
                                    note = note,
                                    selectedNoteIds = selectedNoteIds,
                                    onSelect = { selectedNoteIds = it },
                                    onEdit = { editingNote = note },
                                    onUpdateNote = onUpdateNote
                                )
                            }
                        } else {
                            // Pinned Section
                            if (pinnedNotes.isNotEmpty()) {
                                item(span = StaggeredGridItemSpan.FullLine) {
                                    SectionHeader(title = "PINNED", count = pinnedNotes.size)
                                }
                                items(pinnedNotes, key = { it.id }) { note ->
                                    NoteItem(
                                        note = note,
                                        selectedNoteIds = selectedNoteIds,
                                        onSelect = { selectedNoteIds = it },
                                        onEdit = { editingNote = note },
                                        onUpdateNote = onUpdateNote
                                    )
                                }
                            }

                            // Unpinned Section
                            if (unpinnedNotes.isNotEmpty()) {
                                item(span = StaggeredGridItemSpan.FullLine) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    SectionHeader(title = "NOTES", count = unpinnedNotes.size)
                                }
                                items(unpinnedNotes, key = { it.id }) { note ->
                                    NoteItem(
                                        note = note,
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

            // Direct Note Creation Floating Action Button (Apple Notes Style)
            FloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    val newNote = Note(title = "", content = "")
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

@Composable
fun SectionHeader(title: String, count: Int? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.2.sp
        )
        if (count != null) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "• $count",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun NoteItem(
    note: Note,
    selectedNoteIds: Set<String>,
    onSelect: (Set<String>) -> Unit,
    onEdit: () -> Unit,
    onUpdateNote: (Note) -> Unit
) {
    val isSelected = selectedNoteIds.contains(note.id)
    NoteCard(
        note = note,
        isSelected = isSelected,
        onClick = {
            if (selectedNoteIds.isNotEmpty()) {
                onSelect(if (isSelected) selectedNoteIds - note.id else selectedNoteIds + note.id)
            } else {
                onEdit()
            }
        },
        onLongClick = {
            onSelect(if (isSelected) selectedNoteIds - note.id else selectedNoteIds + note.id)
        },
        onTogglePin = {
            val updated = note.copy(isPinned = !note.isPinned)
            onUpdateNote(updated)
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    note: Note,
    onDelete: () -> Unit = {}, // Preserved for backwards compatibility
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    isSelected: Boolean = false,
    onTogglePin: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    val baseContainerColor = if (note.isCodeMode) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    }
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
    } else {
        baseContainerColor
    }

    val textColor = MaterialTheme.colorScheme.onSurface
    val titleColor = if (note.isCodeMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    val fontFamily = if (note.isCodeMode) FontFamily.Monospace else FontFamily.Default

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(18.dp),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
        },
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth()
        ) {
            // Header Row: Title & Pin/Selection Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                if (note.title.isNotBlank()) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = titleColor,
                        fontFamily = fontFamily,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onTogglePin()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin Note",
                            tint = if (note.isPinned) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            // Note Content Snippet
            if (note.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor.copy(alpha = 0.85f),
                    fontFamily = fontFamily,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer Row: Timestamp & Code Mode Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontSize = 11.sp
                )

                if (note.isCodeMode) {
                    Text(
                        text = "</>",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun EditNoteFullscreen(note: Note, onBack: (Note) -> Unit) {
    val haptic = LocalHapticFeedback.current
    var tempTitle by remember { mutableStateOf(note.title) }
    var tempContent by remember { mutableStateOf(TextFieldValue(note.content)) }
    var tempIsCodeMode by remember { mutableStateOf(note.isCodeMode) }
    var tempIsPinned by remember { mutableStateOf(note.isPinned) }

    BackHandler {
        note.title = tempTitle
        note.content = tempContent.text
        note.isCodeMode = tempIsCodeMode
        note.isPinned = tempIsPinned
        note.timestamp = java.text.SimpleDateFormat("MMM dd, yyyy • hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())
        onBack(note)
    }

    val backgroundColor = if (tempIsCodeMode) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.background
    }
    val textColor = MaterialTheme.colorScheme.onSurface
    val titleColor = if (tempIsCodeMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    val fontFamily = if (tempIsCodeMode) FontFamily.Monospace else FontFamily.Default
    val scrollState = rememberScrollState()

    val highlightColor = MaterialTheme.colorScheme.primaryContainer
    val onHighlightTextColor = MaterialTheme.colorScheme.onPrimaryContainer

    val activeWord = remember(tempContent) {
        val text = tempContent.text
        val selection = tempContent.selection

        if (selection.collapsed) {
            val cursor = selection.start
            val regex = Regex("\\w+")
            regex.findAll(text).firstOrNull { cursor in it.range.first..it.range.last + 1 }?.value ?: ""
        } else {
            text.substring(selection.start, selection.end)
        }
    }

    val codeVisualTransformation = remember(activeWord, tempIsCodeMode) {
        val keywordColor = Color(0xFFC678DD)
        val stringColor = Color(0xFF98C379)
        val numberColor = Color(0xFFD19A66)
        val commentColor = Color(0xFF7F848E)

        val keywordRegex = Regex("\\b(val|var|fun|class|interface|if|else|for|while|return|true|false|null|import|package)\\b")
        val numberRegex = Regex("\\b\\d+(\\.\\d+)?\\b")
        val stringRegex = Regex("\".*?\"")
        val commentRegex = Regex("//.*")

        VisualTransformation { text ->
            val annotatedString = buildAnnotatedString {
                append(text.text)

                if (tempIsCodeMode) {
                    numberRegex.findAll(text.text).forEach { match ->
                        addStyle(SpanStyle(color = numberColor), match.range.first, match.range.last + 1)
                    }
                    keywordRegex.findAll(text.text).forEach { match ->
                        addStyle(SpanStyle(color = keywordColor, fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
                    }
                    stringRegex.findAll(text.text).forEach { match ->
                        addStyle(SpanStyle(color = stringColor), match.range.first, match.range.last + 1)
                    }
                    commentRegex.findAll(text.text).forEach { match ->
                        addStyle(SpanStyle(color = commentColor), match.range.first, match.range.last + 1)
                    }

                    if (activeWord.isNotBlank() && activeWord.matches(Regex("\\w+"))) {
                        var startIndex = 0
                        while (startIndex < text.length) {
                            val index = text.indexOf(activeWord, startIndex)
                            if (index == -1) break

                            val isStartBoundary = index == 0 || !text[index - 1].isLetterOrDigit()
                            val isEndBoundary = index + activeWord.length == text.length || !text[index + activeWord.length].isLetterOrDigit()

                            if (isStartBoundary && isEndBoundary) {
                                addStyle(
                                    style = SpanStyle(
                                        background = highlightColor,
                                        color = onHighlightTextColor
                                    ),
                                    start = index,
                                    end = index + activeWord.length
                                )
                            }
                            startIndex = index + activeWord.length
                        }
                    }
                }
            }
            TransformedText(annotatedString, OffsetMapping.Identity)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .imePadding()
    ) {
        // Apple-style Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                note.title = tempTitle
                note.content = tempContent.text
                note.isCodeMode = tempIsCodeMode
                note.isPinned = tempIsPinned
                note.timestamp = java.text.SimpleDateFormat("MMM dd, yyyy • hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())
                onBack(note)
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = textColor
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    tempIsPinned = !tempIsPinned
                }) {
                    Icon(
                        imageVector = if (tempIsPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = "Pin Note",
                        tint = if (tempIsPinned) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }

                FilterChip(
                    selected = tempIsCodeMode,
                    onClick = { tempIsCodeMode = !tempIsCodeMode },
                    label = { Text("Code") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        // Title Field
        TextField(
            value = tempTitle,
            onValueChange = { tempTitle = it },
            textStyle = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = fontFamily
            ),
            placeholder = {
                Text(
                    "Title",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.Gray.copy(alpha = 0.5f)
                )
            },
            colors = TextFieldDefaults.colors(
                focusedTextColor = titleColor,
                unfocusedTextColor = titleColor,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        // Content Area with scroll and line numbers (in Code mode)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(top = 4.dp, bottom = 24.dp)
        ) {
            if (tempIsCodeMode) {
                val lineCount = tempContent.text.count { it == '\n' } + 1
                val lineNumbers = (1..lineCount).joinToString("\n")

                Text(
                    text = lineNumbers,
                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = fontFamily),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .padding(start = 12.dp, end = 12.dp)
                        .width(36.dp)
                )
            } else {
                Spacer(modifier = Modifier.width(16.dp))
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp)
            ) {
                if (tempContent.text.isEmpty()) {
                    Text(
                        text = if (tempIsCodeMode) "Write code snippet..." else "Note...",
                        color = Color.Gray.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodyLarge.copy(fontFamily = fontFamily)
                    )
                }

                BasicTextField(
                    value = tempContent,
                    onValueChange = { tempContent = it },
                    visualTransformation = if (tempIsCodeMode) codeVisualTransformation else VisualTransformation.None,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = fontFamily,
                        color = textColor,
                        lineHeight = 22.sp
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}