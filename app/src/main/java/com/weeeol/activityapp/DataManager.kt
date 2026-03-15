package com.weeeol.activityapp

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DataManager(context: Context) {
    // This is the file on the hard drive where your data lives
    private val sharedPreferences = context.getSharedPreferences("ActivityAppDatabase", Context.MODE_PRIVATE)
    private val gson = Gson()

    // --- NOTES SAVE/LOAD ---
    fun saveNotes(notes: List<Note>) {
        val jsonString = gson.toJson(notes)
        sharedPreferences.edit().putString("saved_notes", jsonString).apply()
    }

    fun loadNotes(): MutableList<Note> {
        val jsonString = sharedPreferences.getString("saved_notes", null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<Note>>() {}.type
        return gson.fromJson(jsonString, type)
    }

    // --- FOLDERS SAVE/LOAD ---
    fun saveFolders(folders: List<ProjectFolder>): MutableList<ProjectFolder> {
        val jsonString = gson.toJson(folders)
        sharedPreferences.edit().putString("saved_folders", jsonString).apply()
        return folders.toMutableList()
    }

    fun loadFolders(): MutableList<ProjectFolder> {
        val jsonString = sharedPreferences.getString("saved_folders", null)
        if (jsonString == null) {
            // First time opening the app? Give them default folders!
            return mutableListOf(
                ProjectFolder("College Assignments", "🎓"),
                ProjectFolder("Game Dev", "🎮"),
                ProjectFolder("Python & Git", "💻")
            )
        }
        val type = object : TypeToken<MutableList<ProjectFolder>>() {}.type
        return gson.fromJson(jsonString, type)
    }
    // --- TIMERS SAVE/LOAD ---
    fun saveTimers(timers: List<TimerEvent>) {
        // 1. Take a clean snapshot of the current timers
        val snapshots = timers.map { TimerSaveData(it.activityName, it.remainingSeconds) }

        // 2. Save the snapshots to the hard drive
        val jsonString = gson.toJson(snapshots)
        sharedPreferences.edit().putString("saved_timers", jsonString).apply()
    }

    fun loadTimers(): MutableList<TimerEvent> {
        val jsonString = sharedPreferences.getString("saved_timers", null) ?: return mutableListOf()
        val type = object : TypeToken<List<TimerSaveData>>() {}.type
        val savedSnapshots: List<TimerSaveData> = gson.fromJson(jsonString, type)

        // 3. Convert the simple snapshots back into living, breathing TimerEvents!
        return savedSnapshots.map { snapshot ->
            TimerEvent(
                activityName = snapshot.activityName,
                durationMinutes = 0 // Dummy value because we overwrite it on the next line
            ).apply {
                this.remainingSeconds = snapshot.remainingSeconds
                this.isRunning = false // Ensure timers start paused when you reopen the app
            }
        }.toMutableList()
    }
    // --- THEME SAVE/LOAD ---
    fun saveTheme(isDark: Boolean) {
        sharedPreferences.edit().putBoolean("is_dark_theme", isDark).apply()
    }

    fun loadTheme(isSystemDark: Boolean): Boolean {
        // If they haven't set a preference yet, default to their phone's system setting
        return sharedPreferences.getBoolean("is_dark_theme", isSystemDark)
    }
    // --- WATER SAVE/LOAD ---
    fun saveWaterIntake(glasses: Int) {
        sharedPreferences.edit().putInt("saved_water", glasses).apply()
    }

    fun loadWaterIntake(): Int {
        return sharedPreferences.getInt("saved_water", 0)
    }
    // --- DATE SAVE/LOAD FOR WATER RESET ---
    fun saveLastWaterDate(dateString: String) {
        sharedPreferences.edit().putString("last_water_date", dateString).apply()
    }

    fun loadLastWaterDate(): String {
        // Returns an empty string if it's the very first time opening the app
        return sharedPreferences.getString("last_water_date", "") ?: ""
    }
}
