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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotesScreen(notes: MutableList<Note>) {
    // Delete the old "val notes" line!

    var titleText by remember { mutableStateOf("") }
    var contentText by remember { mutableStateOf("") }

    // NEW: This remembers which note you are currently editing
    var editingNote by remember { mutableStateOf<Note?>(null) }

    // THE SWITCH: Are we editing a note, or looking at the grid?
    if (editingNote != null) {
        EditNoteFullscreen(
            note = editingNote!!,
            onBack = { updatedNote ->
                // This forces the list to recognize the change and triggers the auto-save!
                val index = notes.indexOfFirst { it.id == updatedNote.id }
                if (index != -1) notes[index] = updatedNote.copy()
                editingNote = null
            }
        )
    } else {
        // Show the normal grid
        Column(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
            Text(text = "Keep Notes", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = titleText,
                onValueChange = { titleText = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = contentText,
                onValueChange = { contentText = it },
                label = { Text("Take a note...") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).heightIn(min = 100.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (contentText.isNotBlank() || titleText.isNotBlank()) {
                        notes.add(0, Note(titleText, contentText))
                        titleText = ""
                        contentText = ""
                    }
                },
                modifier = Modifier.align(Alignment.End).padding(horizontal = 16.dp)
            ) {
                Text("Save")
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalItemSpacing = 8.dp,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notes, key = { it.id }) { note ->
                    NoteCard(
                        note = note,
                        onDelete = { notes.remove(note) },
                        onClick = { editingNote = note } // NEW: Pass the clicked note up!
                    )
                }
            }
        }
    }
}

// NEW: Added the onClick parameter
@Composable
fun NoteCard(note: Note, onDelete: () -> Unit, onClick: () -> Unit) {
    Card(
        // NEW: Added .clickable so the whole card responds to a tap
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onDelete, modifier = Modifier.size(20.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Note", tint = Color.Gray)
                }
            }

            if (note.title.isNotBlank()) {
                Text(text = note.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(text = note.content, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = note.timestamp, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@Composable
fun EditNoteFullscreen(note: Note, onBack: (Note) -> Unit) { // Notice onBack now passes the note back!
    // Temporary memory for the screen while you type
    var tempTitle by remember { mutableStateOf(note.title) }
    var tempContent by remember { mutableStateOf(note.content) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.Start) {
            IconButton(onClick = {
                // When we leave, update the real note and send it back to trigger a save!
                note.title = tempTitle
                note.content = tempContent
                note.timestamp = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date())
                onBack(note)
            }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }

        TextField(
            value = tempTitle,
            onValueChange = { tempTitle = it }, // Now edits the temp title
            textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            placeholder = { Text("Title", style = MaterialTheme.typography.headlineMedium, color = Color.LightGray) },
            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = tempContent,
            onValueChange = { tempContent = it }, // Now edits the temp content
            textStyle = MaterialTheme.typography.bodyLarge,
            placeholder = { Text("Note", color = Color.LightGray) },
            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
            modifier = Modifier.fillMaxSize()
        )
    }
}