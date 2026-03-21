package com.weeeol.activityapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField // <-- NEW IMPORT
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor // <-- NEW IMPORT
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun NotesScreen(notes: List<Note>, noteDao: NoteDao) {
    val scope = rememberCoroutineScope()

    var titleText by remember { mutableStateOf("") }
    var contentText by remember { mutableStateOf("") }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<Note?>(null) }

    var searchQuery by remember { mutableStateOf("") }

    val filteredNotes = notes.filter { note ->
        note.title.contains(searchQuery, ignoreCase = true) ||
                note.content.contains(searchQuery, ignoreCase = true)
    }

    if (editingNote != null) {
        EditNoteFullscreen(
            note = editingNote!!,
            onBack = { updatedNote ->
                scope.launch(Dispatchers.IO) { noteDao.insertNote(updatedNote) }
                editingNote = null
            }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {

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

                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalItemSpacing = 8.dp,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(filteredNotes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onDelete = { scope.launch(Dispatchers.IO) { noteDao.deleteNote(note) } },
                            onClick = { editingNote = note }
                        )
                    }
                }
            }

            FloatingActionButton(
                onClick = { showAddNoteDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 190.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Note")
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
                                    scope.launch(Dispatchers.IO) { noteDao.insertNote(newNote) }
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

@Composable
fun NoteCard(note: Note, onDelete: () -> Unit, onClick: () -> Unit) {
    val containerColor = if (note.isCodeMode) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    val titleColor = if (note.isCodeMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    val fontFamily = if (note.isCodeMode) FontFamily.Monospace else FontFamily.Default

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onDelete, modifier = Modifier.size(20.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Note", tint = Color.Gray)
                }
            }

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

    Column(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                note.title = tempTitle
                note.content = tempContent.text
                note.isCodeMode = tempIsCodeMode
                note.timestamp = java.text.SimpleDateFormat("MMM dd, yyyy • hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())
                onBack(note)
            }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Code Mode", color = textColor, style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.width(8.dp))
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

        // THE FIX: Upgraded Editor Layout with BasicTextField!
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
                        .padding(start = 8.dp, end = 12.dp) // Tighter edge padding for line numbers
                        .width(32.dp)
                )
            } else {
                Spacer(modifier = Modifier.width(16.dp)) // Standard left margin when NOT in code mode
            }

            Box(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                // BasicTextField doesn't have a built-in placeholder, so we draw it manually underneath!
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