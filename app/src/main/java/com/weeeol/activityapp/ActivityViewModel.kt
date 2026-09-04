package com.weeeol.activityapp

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class ActivityViewModel(
    private val dataManager: DataManager,
    private val noteDao: NoteDao,
    private val folderDao: FolderDao
) : ViewModel() {

    // --- Theme State ---
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // --- Navigation State ---
    private val _selectedNavItem = MutableStateFlow(NavItem.Health)
    val selectedNavItem: StateFlow<NavItem> = _selectedNavItem.asStateFlow()

    // --- UI State ---
    private val _isEditingNote = MutableStateFlow(false)
    val isEditingNote: StateFlow<Boolean> = _isEditingNote.asStateFlow()

    // --- Health & Goals State ---
    private val _steps = MutableStateFlow(dataManager.loadSteps())
    val steps: StateFlow<Int> = _steps.asStateFlow()

    private val _stepGoal = MutableStateFlow(dataManager.loadStepGoal())
    val stepGoal: StateFlow<Int> = _stepGoal.asStateFlow()

    private val _waterGlasses = MutableStateFlow(0)
    val waterGlasses: StateFlow<Int> = _waterGlasses.asStateFlow()

    private val _waterGoal = MutableStateFlow(8)
    val waterGoal: StateFlow<Int> = _waterGoal.asStateFlow()

    // --- Timers State ---
    private val _timers = MutableStateFlow<List<TimerEvent>>(emptyList())
    val timers: StateFlow<List<TimerEvent>> = _timers.asStateFlow()

    // Background Timer Coroutine Job
    private var timerTickerJob: Job? = null

    // --- Room Database Streams ---
    val notes = noteDao.getAllNotes()
    val folders = folderDao.getAllFolders()

    init {
        val today = LocalDate.now().toString()
        val lastDate = dataManager.loadLastWaterDate()

        if (lastDate != today) {
            dataManager.saveLastWaterDate(today)
            dataManager.saveWaterIntake(0)
            dataManager.saveSteps(0)
            _waterGlasses.value = 0
            _steps.value = 0
        } else {
            _waterGlasses.value = dataManager.loadWaterIntake()
            _steps.value = dataManager.loadSteps()
        }

        _timers.value = dataManager.loadTimers()
        startBackgroundTimerEngine()
    }

    // --- Background Timer Engine ---
    private fun startBackgroundTimerEngine() {
        if (timerTickerJob?.isActive == true) return
        timerTickerJob = viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                delay(1000L)
                val now = LocalTime.now()
                var stateChanged = false
                val currentTimers = _timers.value

                currentTimers.forEach { timer ->
                    // Check scheduled start
                    if (timer.scheduledTime != null && !timer.isRunning && timer.remainingSeconds > 0) {
                        if (now.hour == timer.scheduledTime?.hour && now.minute == timer.scheduledTime?.minute) {
                            timer.isRunning = true
                            timer.scheduledTime = null
                            stateChanged = true
                        }
                    }

                    // Tick running timer
                    if (timer.isRunning && timer.remainingSeconds > 0) {
                        timer.remainingSeconds--
                        if (timer.remainingSeconds <= 0L) {
                            timer.isRunning = false
                        }
                        stateChanged = true
                    }
                }

                if (stateChanged) {
                    dataManager.saveTimers(currentTimers)
                }
            }
        }
    }

    // --- Theme Intents ---
    fun initTheme(systemTheme: Boolean) {
        _isDarkMode.value = dataManager.loadTheme(systemTheme)
    }

    fun toggleTheme() {
        _isDarkMode.value = !_isDarkMode.value
        dataManager.saveTheme(_isDarkMode.value)
    }

    // --- Navigation Intents ---
    fun selectNavItem(item: NavItem) {
        _selectedNavItem.value = item
    }

    fun setEditingNote(isEditing: Boolean) {
        _isEditingNote.value = isEditing
    }

    // --- Health Intents ---
    fun updateSteps(newSteps: Int) {
        _steps.value = newSteps
        dataManager.saveSteps(newSteps)
    }

    fun setStepGoal(newGoal: Int) {
        _stepGoal.value = newGoal
        dataManager.saveStepGoal(newGoal)
    }

    fun setWaterGoal(newGoal: Int) {
        _waterGoal.value = newGoal
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

    // --- Folder Intents ---
    fun addFolder(folder: ProjectFolder) {
        viewModelScope.launch(Dispatchers.IO) { folderDao.insertFolder(folder) }
    }

    fun updateFolder(folder: ProjectFolder) {
        viewModelScope.launch(Dispatchers.IO) { folderDao.insertFolder(folder) }
    }

    fun deleteFolder(folder: ProjectFolder) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.deleteNotesByFolder(folder.id)
            folderDao.deleteFolder(folder)
        }
    }

    // --- Note Intents ---
    fun addNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) { noteDao.insertNote(note) }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) { noteDao.insertNote(note) }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) { noteDao.deleteNote(note) }
    }

    // --- Timer Intents ---
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

    fun toggleTimer(timer: TimerEvent) {
        if (timer.remainingSeconds <= 0L) {
            timer.remainingSeconds = timer.totalSeconds
            timer.isRunning = true
        } else {
            timer.isRunning = !timer.isRunning
            timer.scheduledTime = null
        }
        dataManager.saveTimers(_timers.value)
    }

    fun resetTimer(timer: TimerEvent) {
        timer.remainingSeconds = timer.totalSeconds
        timer.isRunning = false
        dataManager.saveTimers(_timers.value)
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