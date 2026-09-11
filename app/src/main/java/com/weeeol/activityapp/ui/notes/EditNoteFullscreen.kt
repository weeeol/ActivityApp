package com.weeeol.activityapp.ui.notes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weeeol.activityapp.Note
import com.weeeol.activityapp.ProjectFolder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EditNoteFullscreen(
    note: Note,
    folders: List<ProjectFolder> = emptyList(),
    onBack: (Note) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var tempTitle by remember { mutableStateOf(note.title) }
    var tempContent by remember { mutableStateOf(TextFieldValue(note.content)) }
    var tempIsCodeMode by remember { mutableStateOf(note.isCodeMode) }
    var tempIsPinned by remember { mutableStateOf(note.isPinned) }
    var tempColorIndex by remember { mutableStateOf(note.colorIndex) }
    var tempFolderId by remember { mutableStateOf(note.folderId) }
    var showFolderMenu by remember { mutableStateOf(false) }

    fun insertTextAtCursor(insertStr: String) {
        val currentText = tempContent.text
        val selection = tempContent.selection
        val newText = currentText.replaceRange(selection.start, selection.end, insertStr)
        val newPos = selection.start + insertStr.length
        tempContent = TextFieldValue(
            text = newText,
            selection = TextRange(newPos)
        )
    }

    fun saveAndExit() {
        note.title = tempTitle
        note.content = tempContent.text
        note.isCodeMode = tempIsCodeMode
        note.isPinned = tempIsPinned
        note.colorIndex = tempColorIndex
        note.folderId = tempFolderId
        note.timestamp = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date())
        onBack(note)
    }

    BackHandler {
        saveAndExit()
    }

    val currentTheme = getNoteColorTheme(tempColorIndex)
    val isDark = isSystemInDarkTheme()

    val backgroundColor = if (tempIsCodeMode) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    } else if (currentTheme.id != 0) {
        if (isDark) currentTheme.darkBg else currentTheme.lightBg
    } else {
        MaterialTheme.colorScheme.background
    }
    val textColor = if (currentTheme.id != 0 && !isDark) Color(0xFF1C1C1E) else MaterialTheme.colorScheme.onSurface
    val titleColor = if (tempIsCodeMode) {
        MaterialTheme.colorScheme.primary
    } else if (currentTheme.id != 0) {
        if (isDark) currentTheme.accentColor else Color(0xFF1C1C1E)
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val fontFamily = if (tempIsCodeMode) FontFamily.Monospace else FontFamily.Default
    val scrollState = rememberScrollState()

    val highlightColor = MaterialTheme.colorScheme.primaryContainer
    val onHighlightTextColor = MaterialTheme.colorScheme.onPrimaryContainer

    val activeWord = remember(tempContent) {
        NoteSyntaxHighlighter.findActiveWord(tempContent)
    }

    val codeVisualTransformation = remember(activeWord, tempIsCodeMode) {
        NoteSyntaxHighlighter.createTransformation(
            isCodeMode = tempIsCodeMode,
            activeWord = activeWord,
            highlightColor = highlightColor,
            onHighlightTextColor = onHighlightTextColor
        )
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
            IconButton(onClick = { saveAndExit() }) {
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
                // Folder Selector Dropdown
                if (folders.isNotEmpty()) {
                    val currentFolder = remember(tempFolderId, folders) {
                        folders.firstOrNull { it.id == tempFolderId }
                    }

                    Box {
                        FilterChip(
                            selected = currentFolder != null,
                            onClick = { showFolderMenu = true },
                            label = {
                                Text(
                                    text = if (currentFolder != null) "${currentFolder.emoji} ${currentFolder.name}" else "Folder",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                            },
                            shape = RoundedCornerShape(12.dp)
                        )

                        DropdownMenu(
                            expanded = showFolderMenu,
                            onDismissRequest = { showFolderMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("No Folder (Unassigned)") },
                                onClick = {
                                    tempFolderId = null
                                    showFolderMenu = false
                                }
                            )
                            folders.forEach { f ->
                                DropdownMenuItem(
                                    text = { Text("${f.emoji} ${f.name}") },
                                    onClick = {
                                        tempFolderId = f.id
                                        showFolderMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

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

        // Modern Toolbar: Color Swatches & Quick Formatting Shortcuts
        NoteEditorToolbar(
            selectedColorIndex = tempColorIndex,
            onColorSelected = { tempColorIndex = it },
            onInsertText = { insertTextAtCursor(it) }
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
                    visualTransformation = codeVisualTransformation,
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
