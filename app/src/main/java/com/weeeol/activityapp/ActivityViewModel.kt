package com.weeeol.activityapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import android.content.Context
import androidx.lifecycle.ViewModelProvider

class ActivityViewModel(
    private val dataManager: DataManager,
    // Exposing the DAOs so we don't have to rewrite your entire Notes/Folders UI yet
    val noteDao: NoteDao,
    val folderDao: FolderDao
) : ViewModel() {

    // --- Theme State ---
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // --- Navigation State ---
    private val _selectedNavItem = MutableStateFlow(NavItem.Health)
    val selectedNavItem: StateFlow<NavItem> = _selectedNavItem.asStateFlow()

    // --- Water Intake State ---
    private val _waterGlasses = MutableStateFlow(0)
    val waterGlasses: StateFlow<Int> = _waterGlasses.asStateFlow()

    // --- Timers State ---
    private val _timers = MutableStateFlow<List<TimerEvent>>(emptyList())
    val timers: StateFlow<List<TimerEvent>> = _timers.asStateFlow()

    // --- Room Database Streams ---
    val notes = noteDao.getAllNotes() // Already returns a Flow!
    val folders = folderDao.getAllFolders() // Already returns a Flow!

    init {
        // Startup Logic: Checking if it's a new day for the water and step trackers
        val today = LocalDate.now().toString()
        val lastDate = dataManager.loadLastWaterDate()

        if (lastDate != today) {
            dataManager.saveLastWaterDate(today)
            dataManager.saveWaterIntake(0)
            dataManager.saveSteps(0)
            _waterGlasses.value = 0
        } else {
            _waterGlasses.value = dataManager.loadWaterIntake()
        }

        // Load saved timers from disk
        _timers.value = dataManager.loadTimers()
    }

    // --- Actions / Intents ---

    fun initTheme(systemTheme: Boolean) {
        _isDarkMode.value = dataManager.loadTheme(systemTheme)
    }

    fun toggleTheme() {
        _isDarkMode.value = !_isDarkMode.value
        dataManager.saveTheme(_isDarkMode.value)
    }

    fun selectNavItem(item: NavItem) {
        _selectedNavItem.value = item
    }

    fun addWater() {
        val today = LocalDate.now().toString()
        if (dataManager.loadLastWaterDate() != today) {
            _waterGlasses.value = 1
            dataManager.saveLastWaterDate(today)
        } else {
            _waterGlasses.value++
        }
        dataManager.saveWaterIntake(_waterGlasses.value)
    }

    fun resetWater() {
        _waterGlasses.value = 0
        dataManager.saveWaterIntake(0)
    }

    fun addTimer(timer: TimerEvent) {
        val updatedList = _timers.value.toMutableList().apply { add(timer) }
        _timers.value = updatedList
        dataManager.saveTimers(updatedList)
    }

    fun removeTimer(timer: TimerEvent) {
        val updatedList = _timers.value.toMutableList().apply { remove(timer) }
        _timers.value = updatedList
        dataManager.saveTimers(updatedList)
    }
}

class ActivityViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActivityViewModel::class.java)) {
            val dataManager = DataManager(context)
            val database = AppDatabase.getDatabase(context)

            @Suppress("UNCHECKED_CAST")
            return ActivityViewModel(
                dataManager = dataManager,
                noteDao = database.noteDao(),
                folderDao = database.folderDao()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}