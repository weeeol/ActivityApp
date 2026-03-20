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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: ProjectFolder) // <-- Added suspend

    @Delete
    suspend fun deleteFolder(folder: ProjectFolder) // <-- Added suspend
}

// --- 2. THE NOTE DAO ---
@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note) // <-- Added suspend

    @Delete
    suspend fun deleteNote(note: Note) // <-- Added suspend

    @Query("DELETE FROM notes WHERE folderId = :folderId")
    suspend fun deleteNotesByFolder(folderId: String) // <-- Added suspend
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