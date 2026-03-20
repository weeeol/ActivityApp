package com.weeeol.activityapp

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

// --- 1. THE FOLDER DAO ---
@Dao
interface FolderDao {
    @Query("SELECT * FROM folders ORDER BY createdAt DESC")
    fun getAllFolders(): Flow<List<ProjectFolder>>

    // Removed "suspend" and return types!
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFolder(folder: ProjectFolder)

    @Delete
    fun deleteFolder(folder: ProjectFolder)
}

// --- 2. THE NOTE DAO ---
@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    fun getAllNotes(): Flow<List<Note>>

    // Removed "suspend" and return types!
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNote(note: Note)

    @Delete
    fun deleteNote(note: Note)

    @Query("DELETE FROM notes WHERE folderId = :folderId")
    fun deleteNotesByFolder(folderId: String)
}

// --- 3. THE ACTUAL DATABASE ---
@Database(entities = [ProjectFolder::class, Note::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // Connect the DAOs
    abstract fun folderDao(): FolderDao
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // If the database already exists, return it. Otherwise, build it!
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "activity_app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}