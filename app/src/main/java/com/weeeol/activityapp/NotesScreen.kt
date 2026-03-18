package com.weeeol.activityapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.material3.Switch
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

@Composable
fun NotesScreen(notes: List<Note>, noteDao: NoteDao) { // Updated signature
    val scope = rememberCoroutineScope()
    var titleText by remember { mutableStateOf("") }
    var contentText by remember { mutableStateOf("") }

    // NEW: State to trigger the popup window!
    var showAddNoteDialog by remember { mutableStateOf(false) }

    var editingNote by remember { mutableStateOf<Note?>(null) }

    if (editingNote != null) {
        EditNoteFullscreen(
            note = editingNote!!,
            onBack = { updatedNote ->
                // Room's REPLACE strategy handles updates automatically!
                scope.launch(Dispatchers.IO) { noteDao.insertNote(updatedNote) }
                editingNote = null
            }
        )
    } else {
        // We wrap the screen in a Box to float the button over the grid
        Box(modifier = Modifier.fillMaxSize()) {

            Column(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
                Text(
                    text = "Notes",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // The Grid (Now takes up the whole screen!)
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalItemSpacing = 8.dp,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    // Adds padding to the bottom so the last notes don't get hidden behind the nav bar
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(notes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onDelete = {scope.launch(Dispatchers.IO){ noteDao.deleteNote(note) } }, // Delete via DAO
                            onClick = { editingNote = note }
                        )
                    }
                }
            }

            // --- THE NEW FLOATING ADD BUTTON ---
            FloatingActionButton(
                onClick = { showAddNoteDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 120.dp), // Pushed up above your nav bar
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Note")
            }

            // --- THE ADD NOTE POP-UP WINDOW ---
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
                                    // 1. Take a snapshot of the text FIRST
                                    val newNote = Note(titleText, contentText)

                                    // 2. Send the snapshot to the background
                                    scope.launch(Dispatchers.IO) { noteDao.insertNote(newNote) }

                                    // 3. Now it is safe to clear the UI
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

// NEW: Added the onClick parameter
@Composable
fun NoteCard(note: Note, onDelete: () -> Unit, onClick: () -> Unit) {
    // THE FIX: Adaptive styling for the grid cards
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
                    // Optional: You can also cap the title to 1 or 2 lines!
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // THE FIX: Restrict the main content to 5 lines
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                fontFamily = fontFamily,
                maxLines = 5, // Change this number to whatever looks best to you
                overflow = TextOverflow.Ellipsis // Adds the "..." when cut off
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = note.timestamp, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@Composable
fun EditNoteFullscreen(note: Note, onBack: (Note) -> Unit) {
    var tempTitle by remember { mutableStateOf(note.title) }

    // 1. THE UPGRADE: TextFieldValue tracks the cursor position and selection!
    var tempContent by remember { mutableStateOf(TextFieldValue(note.content)) }
    var tempIsCodeMode by remember { mutableStateOf(note.isCodeMode) }

    val backgroundColor = if (tempIsCodeMode) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onSurface
    val titleColor = if (tempIsCodeMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    val fontFamily = if (tempIsCodeMode) FontFamily.Monospace else FontFamily.Default
    val scrollState = rememberScrollState()

    // --- NEW: HIGHLIGHT LOGIC ---
    val highlightColor = MaterialTheme.colorScheme.primaryContainer
    val onHighlightTextColor = MaterialTheme.colorScheme.onPrimaryContainer

    // 2. Find the word the cursor is touching
    val activeWord = remember(tempContent) {
        val text = tempContent.text
        val selection = tempContent.selection

        if (selection.collapsed) { // Cursor is just resting somewhere
            val cursor = selection.start
            val regex = Regex("\\w+")
            regex.findAll(text).firstOrNull { cursor in it.range.first..it.range.last + 1 }?.value ?: ""
        } else { // User actively highlighted a chunk of text
            text.substring(selection.start, selection.end)
        }
    }

    // 3. Paint the highlights over matching words!
    val codeVisualTransformation = remember(activeWord, tempIsCodeMode) {
        // Standard IDE Colors (Works well in both light and dark modes)
        val keywordColor = Color(0xFFC678DD) // Purple
        val stringColor = Color(0xFF98C379)  // Green
        val numberColor = Color(0xFFD19A66)  // Orange
        val commentColor = Color(0xFF7F848E) // Grey

        // Regex Patterns to find code elements
        val keywordRegex = Regex("\\b(val|var|fun|class|interface|if|else|for|while|return|true|false|null|import|package)\\b")
        val numberRegex = Regex("\\b\\d+(\\.\\d+)?\\b")
        val stringRegex = Regex("\".*?\"")
        val commentRegex = Regex("//.*")

        VisualTransformation { text ->
            val annotatedString = buildAnnotatedString {
                append(text.text)

                if (tempIsCodeMode) {
                    // --- SYNTAX HIGHLIGHTING ---

                    // 1. Highlight Numbers
                    numberRegex.findAll(text.text).forEach { match ->
                        addStyle(SpanStyle(color = numberColor), match.range.first, match.range.last + 1)
                    }

                    // 2. Highlight Keywords (Make them bold and purple!)
                    keywordRegex.findAll(text.text).forEach { match ->
                        addStyle(SpanStyle(color = keywordColor, fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
                    }

                    // 3. Highlight Strings (Done after keywords so words inside quotes stay green)
                    stringRegex.findAll(text.text).forEach { match ->
                        addStyle(SpanStyle(color = stringColor), match.range.first, match.range.last + 1)
                    }

                    // 4. Highlight Comments (Overrides everything else on that line)
                    commentRegex.findAll(text.text).forEach { match ->
                        addStyle(SpanStyle(color = commentColor), match.range.first, match.range.last + 1)
                    }

                    // --- ACTIVE WORD HIGHLIGHTING (From our previous step) ---
                    if (activeWord.isNotBlank() && activeWord.matches(Regex("\\w+"))) {
                        var startIndex = 0
                        while (startIndex < text.length) {
                            val index = text.indexOf(activeWord, startIndex)
                            if (index == -1) break

                            val isStartBoundary = index == 0 || !text[index - 1].isLetterOrDigit()
                            val isEndBoundary = index + activeWord.length == text.length || !text[index + activeWord.length].isLetterOrDigit()

                            if (isStartBoundary && isEndBoundary) {
                                addStyle(
                                    // Change the text color to contrast with the highlight box!
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
    // ----------------------------

    Column(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                note.title = tempTitle
                // Notice we save .text from the TextFieldValue here!
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

        Row(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            if (tempIsCodeMode) {
                // Update line counter to read from the TextFieldValue
                val lineCount = tempContent.text.count { it == '\n' } + 1
                val lineNumbers = (1..lineCount).joinToString("\n")

                Text(
                    text = lineNumbers,
                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = fontFamily),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .padding(start = 16.dp, top = 16.dp)
                        .width(28.dp)
                )
            }

            TextField(
                // Hook up the new TextFieldValue and the Visual Transformation!
                value = tempContent,
                onValueChange = { tempContent = it },
                visualTransformation = if (tempIsCodeMode) codeVisualTransformation else VisualTransformation.None,
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = fontFamily),
                placeholder = { Text("Write your code snippet...", color = Color.Gray) },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}