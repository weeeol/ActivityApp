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
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.roundToInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.weeeol.activityapp.ui.theme.ActivityAppTheme
import androidx.activity.compose.BackHandler
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
            LaunchedEffect(systemTheme) {
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
    val isEditingNote by viewModel.isEditingNote.collectAsStateWithLifecycle()

    val selectedItem by viewModel.selectedNavItem.collectAsStateWithLifecycle()
    val waterGlasses by viewModel.waterGlasses.collectAsStateWithLifecycle()
    val waterGoal by viewModel.waterGoal.collectAsStateWithLifecycle()
    val steps by viewModel.steps.collectAsStateWithLifecycle()
    val stepGoal by viewModel.stepGoal.collectAsStateWithLifecycle()
    val timers by viewModel.timers.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle(initialValue = emptyList())
    val folders by viewModel.folders.collectAsStateWithLifecycle(initialValue = emptyList())

    val navItems = remember { NavItem.entries }
    val selectedIndex = navItems.indexOf(selectedItem)
    val navPosition = remember { Animatable(selectedIndex.toFloat()) }

    var showSettings by remember { mutableStateOf(false) }

    // Intercept system back button/gesture to exit settings back to main screen
    BackHandler(enabled = showSettings) {
        showSettings = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MainContent(
            navPosition = navPosition,
            timers = timers,
            notes = notes,
            folders = folders,
            viewModel = viewModel,
            waterGlasses = waterGlasses,
            waterGoal = waterGoal,
            steps = steps,
            stepGoal = stepGoal,
            modifier = Modifier.fillMaxSize()
        )

        // Hide the Settings button when editing a note or in settings
        androidx.compose.animation.AnimatedVisibility(
            visible = !isEditingNote && !showSettings,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 8.dp, end = 12.dp),
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(initialScale = 0.8f),
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut(targetScale = 0.8f)
        ) {
            IconButton(onClick = { showSettings = true }) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onBackground)
            }
        }

        // Hide the Navigation bar when editing a note or in settings
        androidx.compose.animation.AnimatedVisibility(
            visible = !isEditingNote && !showSettings,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 32.dp),
            enter = androidx.compose.animation.slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)
            ) + androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.slideOutVertically(
                targetOffsetY = { it },
                animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)
            ) + androidx.compose.animation.fadeOut()
        ) {
            FloatingNavigationBar(
                selectedItem = selectedItem,
                onItemSelected = { viewModel.selectNavItem(it) },
                navPosition = navPosition
            )
        }

        // Settings Screen with fluid slide-up spring transition
        androidx.compose.animation.AnimatedVisibility(
            visible = showSettings,
            enter = androidx.compose.animation.slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow)
            ) + androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.slideOutVertically(
                targetOffsetY = { it },
                animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)
            ) + androidx.compose.animation.fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            SettingsScreen(
                isDarkMode = isDarkMode,
                onThemeToggle = { viewModel.toggleTheme() },
                onClose = { showSettings = false },
                notesCount = notes.size,
                foldersCount = folders.size,
                timersCount = timers.size,
                waterGlasses = waterGlasses,
                stepGoal = stepGoal,
                waterGoal = waterGoal,
                onUpdateStepGoal = { viewModel.setStepGoal(it) },
                onUpdateWaterGoal = { viewModel.setWaterGoal(it) }
            )
        }
    }
}

@Composable
fun MainContent(
    navPosition: Animatable<Float, AnimationVector1D>,
    timers: List<TimerEvent>,
    notes: List<Note>,
    folders: List<ProjectFolder>,
    viewModel: ActivityViewModel,
    waterGlasses: Int,
    waterGoal: Int,
    steps: Int,
    stepGoal: Int,
    modifier: Modifier = Modifier
) {
    val navItems = remember { NavItem.entries }

    Box(
        modifier = modifier
            .statusBarsPadding(),
        contentAlignment = Alignment.TopStart
    ) {
        navItems.forEachIndexed { index, item ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val pageOffset = index - navPosition.value
                        translationX = pageOffset * size.width
                        alpha = if (kotlin.math.abs(pageOffset) >= 1.05f) 0f else 1f
                        clip = true
                    }
            ) {
                when (item) {
                    NavItem.Health -> HealthScreen(
                        steps = steps,
                        stepsGoal = stepGoal,
                        waterGlasses = waterGlasses,
                        waterGoal = waterGoal,
                        onAddWater = { viewModel.addWater() },
                        onResetWater = { viewModel.resetWater() },
                        onUpdateSteps = { viewModel.updateSteps(it) },
                        onUpdateStepGoal = { viewModel.setStepGoal(it) }
                    )
                    NavItem.Notes -> NotesScreen(
                        notes = notes,
                        folders = folders,
                        onAddNote = { viewModel.addNote(it) },
                        onUpdateNote = { viewModel.updateNote(it) },
                        onDeleteNote = { viewModel.deleteNote(it) },
                        onEditingStateChange = { viewModel.setEditingNote(it) }
                    )
                    NavItem.Folders -> FoldersScreen(
                        folders = folders,
                        notes = notes,
                        onAddFolder = { viewModel.addFolder(it) },
                        onUpdateFolder = { viewModel.updateFolder(it) },
                        onDeleteFolder = { viewModel.deleteFolder(it) },
                        onAddNote = { viewModel.addNote(it) },
                        onUpdateNote = { viewModel.updateNote(it) },
                        onDeleteNote = { viewModel.deleteNote(it) },
                        onEditingStateChange = { viewModel.setEditingNote(it) }
                    )
                    NavItem.Timer -> TimerScreen(
                        timers = timers,
                        onAddTimer = { viewModel.addTimer(it) },
                        onDeleteTimer = { viewModel.removeTimer(it) },
                        onToggleTimer = { viewModel.toggleTimer(it) },
                        onResetTimer = { viewModel.resetTimer(it) }
                    )
                }
            }
        }
    }
}