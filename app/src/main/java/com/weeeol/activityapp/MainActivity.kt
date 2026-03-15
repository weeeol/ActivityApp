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



// 2. The missing MainActivity class! This is what tells Android to draw the screen.
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

    var selectedItem by remember { mutableStateOf(NavItem.Health) }

    // 1. THE STARTUP CHECK: Does today match the saved date?
    var waterGlasses by remember {
        val today = LocalDate.now().toString()
        val lastDate = dataManager.loadLastWaterDate()

        if (lastDate != today) {
            // It's a new day! Reset to 0 and save today's date
            dataManager.saveLastWaterDate(today)
            dataManager.saveWaterIntake(0)
            mutableIntStateOf(0)
        } else {
            // Same day, load the saved water amount
            mutableIntStateOf(dataManager.loadWaterIntake())
        }
    }

    val timers = remember { mutableStateListOf(*dataManager.loadTimers().toTypedArray()) }
    val notes = remember { mutableStateListOf(*dataManager.loadNotes().toTypedArray()) }
    val folders = remember { mutableStateListOf(*dataManager.loadFolders().toTypedArray()) }

    LaunchedEffect(waterGlasses) { dataManager.saveWaterIntake(waterGlasses) }
    LaunchedEffect(notes.toList()) { dataManager.saveNotes(notes) }
    LaunchedEffect(folders.toList()) { dataManager.saveFolders(folders) }
    LaunchedEffect(timers.toList()) { dataManager.saveTimers(timers) }

    Box(modifier = Modifier.fillMaxSize()) {
        MainContent(
            selectedItem = selectedItem,
            timers = timers,
            notes = notes,
            folders = folders,
            waterGlasses = waterGlasses,

            // 2. THE OVERNIGHT CHECK: What if they kept the app open past midnight?
            onAddWater = {
                val today = LocalDate.now().toString()
                if (dataManager.loadLastWaterDate() != today) {
                    // It rolled over to a new day while the app was open!
                    waterGlasses = 1 // Set to 1 because they just drank their first glass
                    dataManager.saveLastWaterDate(today)
                } else {
                    // Normal behavior
                    waterGlasses++
                }
            },

            onResetWater = { waterGlasses = 0 },
            modifier = Modifier.fillMaxSize()
        )

        // --- THE THEME TOGGLE BUTTON ---
        IconButton(
            onClick = onThemeToggle,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding() // Keeps it safely below the battery icon
                .padding(top = 16.dp, end = 16.dp)
        ) {
            Icon(
                // Show a Sun if in dark mode, or a Moon if in light mode!
                imageVector = if (isDarkMode) Icons.Default.Brightness7 else Icons.Default.Brightness4,
                contentDescription = "Toggle Theme",
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
    }
}

@Composable
fun MainContent(
    selectedItem: NavItem,
    timers: MutableList<TimerEvent>,
    notes: MutableList<Note>,
    folders: MutableList<ProjectFolder>,
    waterGlasses: Int,
    onAddWater: () -> Unit,
    onResetWater: () -> Unit,  // NEW
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
            // Pass the state into HealthScreen!
            NavItem.Health -> HealthScreen(waterGlasses, onAddWater, onResetWater)
            NavItem.Notes -> NotesScreen(notes = notes)
            NavItem.Folders -> FoldersScreen(folders = folders, notes = notes)
            NavItem.Timer -> TimerScreen(timers = timers)
        }
    }
}
