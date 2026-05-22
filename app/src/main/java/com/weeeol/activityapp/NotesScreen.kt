package com.weeeol.activityapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Title
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.imePadding

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotesScreen(
    notes: List<Note>,
    onAddNote: (Note) -> Unit,
    onUpdateNote: (Note) -> Unit,
    onDeleteNote: (Note) -> Unit,
    onEditingStateChange: (Boolean) -> Unit
) {
    var titleText by remember { mutableStateOf("") }
    var contentText by remember { mutableStateOf("") }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<Note?>(null) }

    var isFabExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }

    // State for multi-selection
    var selectedNoteIds by remember { mutableStateOf(setOf<String>()) }

    val filteredNotes = notes.filter { note ->
        note.title.contains(searchQuery, ignoreCase = true) ||
                note.content.contains(searchQuery, ignoreCase = true)
    }

    // NEW: Automatically hide navbar and settings when editing OR selecting
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
                onUpdateNote(updatedNote)
                editingNote = null
            }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {

                // Contextual Action Bar vs Standard Header
                if (selectedNoteIds.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start // Aligns everything to the left
                    ) {
                        IconButton(onClick = { selectedNoteIds = emptySet() }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear Selection")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "${selectedNoteIds.size} Selected",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        // Delete button moved right next to the text
                        IconButton(onClick = {
                            val notesToDelete = notes.filter { it.id in selectedNoteIds }
                            notesToDelete.forEach { onDeleteNote(it) }
                            selectedNoteIds = emptySet()
                        }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Selected", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                } else {
                    Text(
                        text = "Notes",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search your notes...") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = Color.Gray)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }

                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalItemSpacing = 8.dp,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(filteredNotes, key = { it.id }) { note ->
                        val isSelected = selectedNoteIds.contains(note.id)
                        NoteCard(
                            note = note,
                            isSelected = isSelected,
                            onClick = {
                                if (selectedNoteIds.isNotEmpty()) {
                                    // Toggle selection if we are in select mode
                                    selectedNoteIds = if (isSelected) selectedNoteIds - note.id else selectedNoteIds + note.id
                                } else {
                                    editingNote = note
                                }
                            },
                            onLongClick = {
                                selectedNoteIds = if (isSelected) selectedNoteIds - note.id else selectedNoteIds + note.id
                            },
                            onTogglePin = {
                                val updated = note.copy(isPinned = !note.isPinned)
                                onUpdateNote(updated)
                            }
                        )
                    }
                }
            }

            // --- THE DIMMED OVERLAY ---
            AnimatedVisibility(
                visible = isFabExpanded,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { isFabExpanded = false }
                        )
                )
            }

            // --- THE EXPANDABLE FAB MENU ---
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 190.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AnimatedVisibility(
                    visible = isFabExpanded,
                    enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
                    exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ExtendedFloatingActionButton(
                            text = { Text("List") },
                            icon = { Icon(Icons.Default.FormatListBulleted, contentDescription = "List Note") },
                            onClick = {
                                isFabExpanded = false
                                Toast.makeText(context, "Checklist feature coming soon!", Toast.LENGTH_SHORT).show()
                            },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        ExtendedFloatingActionButton(
                            text = { Text("Text") },
                            icon = { Icon(Icons.Default.Title, contentDescription = "Text Note") },
                            onClick = {
                                isFabExpanded = false
                                showAddNoteDialog = true
                            },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                FloatingActionButton(
                    onClick = { isFabExpanded = !isFabExpanded },
                    containerColor = if (isFabExpanded) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer,
                    contentColor = if (isFabExpanded) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Crossfade(targetState = isFabExpanded, label = "fab_icon") { expanded ->
                        if (expanded) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close Menu")
                        } else {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Expand Menu")
                        }
                    }
                }
            }

            if (showAddNoteDialog) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showAddNoteDialog = false },
                    title = { Text("Create New Note") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = titleText,
                                onValueChange = { titleText = it },
                                label = { Text("Title") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = contentText,
                                onValueChange = { contentText = it },
                                label = { Text("Take a note...") },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (contentText.isNotBlank() || titleText.isNotBlank()) {
                                    val newNote = Note(titleText, contentText)
                                    onAddNote(newNote)
                                    titleText = ""
                                    contentText = ""
                                    showAddNoteDialog = false
                                }
                            }
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showAddNoteDialog = false
                            titleText = ""
                            contentText = ""
                        }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    note: Note,
    onDelete: () -> Unit = {}, // Kept to avoid breaking older screens
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    isSelected: Boolean = false,
    onTogglePin: () -> Unit = {}
) {
    val baseContainerColor = if (note.isCodeMode) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else baseContainerColor

    val textColor = MaterialTheme.colorScheme.onSurface
    val titleColor = if (note.isCodeMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    val fontFamily = if (note.isCodeMode) FontFamily.Monospace else FontFamily.Default

    Card(
        modifier = Modifier.fillMaxWidth().combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {

            // Delete button removed, Pin button remains
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onTogglePin, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = if (note.isPinned) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Pin Note",
                        tint = if (note.isPinned) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (note.title.isNotBlank()) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                    fontFamily = fontFamily,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                fontFamily = fontFamily,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = note.timestamp, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@Composable
fun EditNoteFullscreen(note: Note, onBack: (Note) -> Unit) {
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

    val backgroundColor = if (tempIsCodeMode) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.background
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
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
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
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { tempIsPinned = !tempIsPinned }) {
                    Icon(
                        imageVector = if (tempIsPinned) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Pin Note",
                        tint = if (tempIsPinned) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(text = "Code", color = textColor, style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.width(4.dp))
                Switch(
                    checked = tempIsCodeMode,
                    onCheckedChange = { tempIsCodeMode = it }
                )
            }
        }

        TextField(
            value = tempTitle,
            onValueChange = { tempTitle = it },
            textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontFamily = fontFamily),
            placeholder = { Text("Title", style = MaterialTheme.typography.headlineMedium, color = Color.Gray) },
            colors = TextFieldDefaults.colors(
                focusedTextColor = titleColor,
                unfocusedTextColor = titleColor,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(top = 8.dp, bottom = 16.dp)
        ) {
            if (tempIsCodeMode) {
                val lineCount = tempContent.text.count { it == '\n' } + 1
                val lineNumbers = (1..lineCount).joinToString("\n")

                Text(
                    text = lineNumbers,
                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = fontFamily),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .padding(start = 8.dp, end = 12.dp)
                        .width(32.dp)
                )
            } else {
                Spacer(modifier = Modifier.width(16.dp))
            }

            Box(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                if (tempContent.text.isEmpty()) {
                    Text(
                        text = "Write your snippet...",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyLarge.copy(fontFamily = fontFamily)
                    )
                }

                BasicTextField(
                    value = tempContent,
                    onValueChange = { tempContent = it },
                    visualTransformation = if (tempIsCodeMode) codeVisualTransformation else VisualTransformation.None,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = fontFamily, color = textColor),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}