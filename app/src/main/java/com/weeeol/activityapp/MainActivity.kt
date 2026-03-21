package com.weeeol.activityapp

import com.weeeol.activityapp.ui.theme.ActivityAppTheme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import java.time.LocalDate
import androidx.compose.runtime.getValue
import android.app.Activity
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.filled.Settings


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 1. Boot up the database to check the theme
            val context = LocalContext.current
            val dataManager = remember { DataManager(context) }

            // 2. Check the system default, then ask the database what the user prefers
            val systemTheme = isSystemInDarkTheme()
            var isDarkMode by remember { mutableStateOf(dataManager.loadTheme(systemTheme)) }

            // 3. Pass the dynamic state into your Theme!
            ActivityAppTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 4. Pass the state and the toggle function down to your main screen
                    ActivityAppMainScreen(
                        isDarkMode = isDarkMode,
                        onThemeToggle = {
                            isDarkMode = !isDarkMode // Flip the state
                            dataManager.saveTheme(isDarkMode) // Save it permanently
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ActivityAppMainScreen(isDarkMode: Boolean, onThemeToggle: () -> Unit) {
    val context = LocalContext.current
    val dataManager = remember { DataManager(context) }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // 1. Make the grey boxes completely transparent
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT

            // 2. Tell Android to stretch your app all the way to the absolute edges of the screen
            WindowCompat.setDecorFitsSystemWindows(window, false)

            // 3. Flip the Time/Battery icons to black if in Light Mode, and white if in Dark Mode
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkMode
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDarkMode
        }
    }
    // -----------------------------

    var selectedItem by remember { mutableStateOf(NavItem.Health) }

    // 1. THE STARTUP CHECK: Does today match the saved date?
    var waterGlasses by remember {
        val today = LocalDate.now().toString()
        val lastDate = dataManager.loadLastWaterDate()

        if (lastDate != today) {
            // It's a new day! Reset to 0 and save today's date
            dataManager.saveLastWaterDate(today)
            dataManager.saveWaterIntake(0)
            dataManager.saveSteps(0) // <-- THE FIX: Reset steps to 0 at midnight!
            mutableIntStateOf(0)
        } else {
            // Same day, load the saved water amount
            mutableIntStateOf(dataManager.loadWaterIntake())
        }
    }

    val timers = remember { mutableStateListOf(*dataManager.loadTimers().toTypedArray()) }
    val database = remember { AppDatabase.getDatabase(context) }
    val noteDao = database.noteDao()
    val folderDao = database.folderDao()

    val notes by noteDao.getAllNotes().collectAsStateWithLifecycle(initialValue = emptyList())
    val folders by folderDao.getAllFolders().collectAsStateWithLifecycle(initialValue = emptyList())

    LaunchedEffect(waterGlasses) { dataManager.saveWaterIntake(waterGlasses) }
    LaunchedEffect(timers.toList()) { dataManager.saveTimers(timers) }
// NEW: State to control if the settings screen is showing
    var showSettings by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // We only show the main content if settings is closed
        if (!showSettings) {
            MainContent(
                selectedItem = selectedItem,
                timers = timers,
                notes = notes,
                folders = folders,
                noteDao = noteDao,
                folderDao = folderDao,
                waterGlasses = waterGlasses,
                onAddWater = {
                    val today = LocalDate.now().toString()
                    if (dataManager.loadLastWaterDate() != today) {
                        waterGlasses = 1
                        dataManager.saveLastWaterDate(today)
                    } else {
                        waterGlasses++
                    }
                },
                onResetWater = { waterGlasses = 0 },
                modifier = Modifier.fillMaxSize()
            )

            // --- THE NEW SETTINGS ICON BUTTON ---
            IconButton(
                onClick = { showSettings = true }, // Opens the settings screen
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 16.dp, end = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            FloatingNavigationBar(
                selectedItem = selectedItem,
                onItemSelected = { selectedItem = it },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 32.dp)
            )
        } else {
            // --- SHOW THE SETTINGS SCREEN FULLY OVERLAYING EVERYTHING ---
            SettingsScreen(
                isDarkMode = isDarkMode,
                // We handle the actual saving logic right here!
                onThemeToggle = { isDark ->
                    onThemeToggle() // This triggers the parent function to flip the state
                },
                onClose = { showSettings = false } // Closes the screen
            )
        }
    }
}

@Composable
fun MainContent(
    selectedItem: NavItem,
    timers: MutableList<TimerEvent>,
    notes: List<Note>,                 // Changed from MutableList to List
    folders: List<ProjectFolder>,      // Changed from MutableList to List
    noteDao: NoteDao,                  // NEW
    folderDao: FolderDao,              // NEW
    waterGlasses: Int,
    onAddWater: () -> Unit,
    onResetWater: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(start = 16.dp, top = 16.dp, end = 16.dp),
        contentAlignment = Alignment.TopStart
    ) {
        when (selectedItem) {
            NavItem.Health -> HealthScreen(waterGlasses, onAddWater, onResetWater)
            // Pass the DAOs into the screens!
            NavItem.Notes -> NotesScreen(notes = notes, noteDao = noteDao)
            NavItem.Folders -> FoldersScreen(folders = folders, notes = notes, folderDao = folderDao, noteDao = noteDao)
            NavItem.Timer -> TimerScreen(timers = timers)
        }
    }
}
