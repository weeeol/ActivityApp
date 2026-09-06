package com.weeeol.activityapp

import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getAllNotes(): Flow<List<Note>>
    fun getNotesByFolder(folderId: String): Flow<List<Note>>
    fun searchNotes(query: String): Flow<List<Note>>
    suspend fun insertNote(note: Note)
    suspend fun deleteNote(note: Note)
    suspend fun deleteNotesByFolder(folderId: String)
}

interface FolderRepository {
    fun getAllFolders(): Flow<List<ProjectFolder>>
    suspend fun insertFolder(folder: ProjectFolder)
    suspend fun deleteFolder(folder: ProjectFolder)
}

class NoteRepositoryImpl(private val noteDao: NoteDao) : NoteRepository {
    override fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()
    override fun getNotesByFolder(folderId: String): Flow<List<Note>> = noteDao.getNotesByFolder(folderId)
    override fun searchNotes(query: String): Flow<List<Note>> = noteDao.searchNotes(query)
    override suspend fun insertNote(note: Note) = noteDao.insertNote(note)
    override suspend fun deleteNote(note: Note) = noteDao.deleteNote(note)
    override suspend fun deleteNotesByFolder(folderId: String) = noteDao.deleteNotesByFolder(folderId)
}

class FolderRepositoryImpl(private val folderDao: FolderDao) : FolderRepository {
    override fun getAllFolders(): Flow<List<ProjectFolder>> = folderDao.getAllFolders()
    override suspend fun insertFolder(folder: ProjectFolder) = folderDao.insertFolder(folder)
    override suspend fun deleteFolder(folder: ProjectFolder) = folderDao.deleteFolder(folder)
}
