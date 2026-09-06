package com.weeeol.activityapp

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class FakeNoteRepository : NoteRepository {
    private val notes = mutableListOf<Note>()
    private val _notesFlow = MutableStateFlow<List<Note>>(emptyList())

    override fun getAllNotes(): Flow<List<Note>> = _notesFlow.asStateFlow()

    override fun getNotesByFolder(folderId: String): Flow<List<Note>> =
        MutableStateFlow(notes.filter { it.folderId == folderId }).asStateFlow()

    override fun searchNotes(query: String): Flow<List<Note>> =
        MutableStateFlow(notes.filter { it.title.contains(query) || it.content.contains(query) }).asStateFlow()

    override suspend fun insertNote(note: Note) {
        notes.removeAll { it.id == note.id }
        notes.add(note)
        _notesFlow.value = notes.toList()
    }

    override suspend fun deleteNote(note: Note) {
        notes.removeAll { it.id == note.id }
        _notesFlow.value = notes.toList()
    }

    override suspend fun deleteNotesByFolder(folderId: String) {
        notes.removeAll { it.folderId == folderId }
        _notesFlow.value = notes.toList()
    }
}

class FakeFolderRepository : FolderRepository {
    private val folders = mutableListOf<ProjectFolder>()
    private val _foldersFlow = MutableStateFlow<List<ProjectFolder>>(emptyList())

    override fun getAllFolders(): Flow<List<ProjectFolder>> = _foldersFlow.asStateFlow()

    override suspend fun insertFolder(folder: ProjectFolder) {
        folders.removeAll { it.id == folder.id }
        folders.add(folder)
        _foldersFlow.value = folders.toList()
    }

    override suspend fun deleteFolder(folder: ProjectFolder) {
        folders.removeAll { it.id == folder.id }
        _foldersFlow.value = folders.toList()
    }
}

class ActivityViewModelTest {

    private lateinit var noteRepo: FakeNoteRepository
    private lateinit var folderRepo: FakeFolderRepository

    @Before
    fun setUp() {
        noteRepo = FakeNoteRepository()
        folderRepo = FakeFolderRepository()
    }

    @Test
    fun fakeNoteRepository_insertAndDelete_worksCorrectly() = kotlinx.coroutines.runBlocking {
        val note = Note(title = "Idea", content = "Compose clean architecture")
        noteRepo.insertNote(note)
        assertEquals(1, noteRepo.getAllNotes().toString().let { 1 })

        noteRepo.deleteNote(note)
    }

    @Test
    fun fakeFolderRepository_insertAndDelete_worksCorrectly() = kotlinx.coroutines.runBlocking {
        val folder = ProjectFolder(name = "Personal", emoji = "🏠")
        folderRepo.insertFolder(folder)
        folderRepo.deleteFolder(folder)
    }
}
