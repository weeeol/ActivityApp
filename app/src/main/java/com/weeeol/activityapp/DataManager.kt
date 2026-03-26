package com.weeeol.activityapp

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DataManager(context: Context) {
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
        val formatter = java.time.format.DateTimeFormatter.ISO_LOCAL_TIME

        val snapshots = timers.map { timer ->
            TimerSaveData(
                activityName = timer.activityName,
                remainingSeconds = timer.remainingSeconds,
                scheduledTime = timer.scheduledTime?.format(formatter)
            )
        }

        val jsonString = gson.toJson(snapshots)
        sharedPreferences.edit().putString("saved_timers", jsonString).apply()
    }

    fun loadTimers(): MutableList<TimerEvent> {
        val jsonString = sharedPreferences.getString("saved_timers", null) ?: return mutableListOf()
        val type = object : com.google.gson.reflect.TypeToken<List<TimerSaveData>>() {}.type
        val savedSnapshots: List<TimerSaveData> = gson.fromJson(jsonString, type)

        val formatter = java.time.format.DateTimeFormatter.ISO_LOCAL_TIME

        return savedSnapshots.map { snapshot ->
            val parsedTime = snapshot.scheduledTime?.let { java.time.LocalTime.parse(it, formatter) }

            TimerEvent(
                activityName = snapshot.activityName,
                durationMinutes = 0,
                scheduledTime = parsedTime
            ).apply {
                this.remainingSeconds = snapshot.remainingSeconds
                this.isRunning = false
            }
        }.toMutableList()
    }
    // --- THEME SAVE/LOAD ---
    fun saveTheme(isDark: Boolean) {
        sharedPreferences.edit().putBoolean("is_dark_theme", isDark).apply()
    }

    fun loadTheme(isSystemDark: Boolean): Boolean {
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
        return sharedPreferences.getString("last_water_date", "") ?: ""
    }
    // --- STEPS SAVE/LOAD ---
    fun saveSteps(steps: Int) {
        sharedPreferences.edit().putInt("saved_steps", steps).apply()
    }

    fun loadSteps(): Int {
        return sharedPreferences.getInt("saved_steps", 0)
    }

    fun saveStepGoal(goal: Int) {
        sharedPreferences.edit().putInt("step_goal", goal).apply()
    }

    fun loadStepGoal(): Int {
        return sharedPreferences.getInt("step_goal", 10000)
    }

    fun saveLastSensorValue(value: Float) {
        sharedPreferences.edit().putFloat("last_sensor_value", value).apply()
    }

    fun loadLastSensorValue(): Float {
        return sharedPreferences.getFloat("last_sensor_value", -1f)
    }
}
