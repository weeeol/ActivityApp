package com.weeeol.activityapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.weeeol.activityapp.ui.theme.ActivityAppTheme
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current

            val viewModel: ActivityViewModel = viewModel(
                factory = ActivityViewModelFactory(context.applicationContext)
            )

            val systemTheme = isSystemInDarkTheme()
            LaunchedEffect(Unit) {
                viewModel.initTheme(systemTheme)
            }

            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

            ActivityAppTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ActivityAppMainScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun ActivityAppMainScreen(viewModel: ActivityViewModel) {

    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    val selectedItem by viewModel.selectedNavItem.collectAsStateWithLifecycle()
    val waterGlasses by viewModel.waterGlasses.collectAsStateWithLifecycle()
    val timers by viewModel.timers.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle(initialValue = emptyList())
    val folders by viewModel.folders.collectAsStateWithLifecycle(initialValue = emptyList())

    var showSettings by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!showSettings) {
            MainContent(
                selectedItem = selectedItem,
                timers = timers,
                notes = notes,
                folders = folders,
                viewModel = viewModel,
                waterGlasses = waterGlasses,
                modifier = Modifier.fillMaxSize()
            )

            IconButton(
                onClick = { showSettings = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 16.dp, end = 16.dp)
            ) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onBackground)
            }

            FloatingNavigationBar(
                selectedItem = selectedItem,
                onItemSelected = { viewModel.selectNavItem(it) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 32.dp)
            )
        } else {
            SettingsScreen(
                isDarkMode = isDarkMode,
                onThemeToggle = { viewModel.toggleTheme() },
                onClose = { showSettings = false }
            )
        }
    }
}

@Composable
fun MainContent(
    selectedItem: NavItem,
    timers: List<TimerEvent>,
    notes: List<Note>,
    folders: List<ProjectFolder>,
    viewModel: ActivityViewModel,
    waterGlasses: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        contentAlignment = Alignment.TopStart
    ) {
        when (selectedItem) {
            NavItem.Health -> HealthScreen(
                waterGlasses = waterGlasses,
                onAddWater = { viewModel.addWater() },
                onResetWater = { viewModel.resetWater() }
            )
            NavItem.Notes -> NotesScreen(
                notes = notes,
                onAddNote = { viewModel.addNote(it) },
                onUpdateNote = { viewModel.updateNote(it) },
                onDeleteNote = { viewModel.deleteNote(it) }
            )
            NavItem.Folders -> FoldersScreen(
                folders = folders,
                notes = notes,
                onAddFolder = { viewModel.addFolder(it) },
                onUpdateFolder = { viewModel.updateFolder(it) },
                onDeleteFolder = { viewModel.deleteFolder(it) },
                onAddNote = { viewModel.addNote(it) },
                onUpdateNote = { viewModel.updateNote(it) },
                onDeleteNote = { viewModel.deleteNote(it) }
            )
            NavItem.Timer -> TimerScreen(
                timers = timers,
                onAddTimer = { viewModel.addTimer(it) },
                onDeleteTimer = { viewModel.removeTimer(it) }
            )
        }
    }
}