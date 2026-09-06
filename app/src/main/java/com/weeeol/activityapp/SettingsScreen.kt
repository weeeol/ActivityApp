package com.weeeol.activityapp

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(
    isDarkMode: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    onClose: () -> Unit,
    notesCount: Int = 0,
    foldersCount: Int = 0,
    timersCount: Int = 0,
    waterGlasses: Int = 0,
    stepGoal: Int = 10000,
    waterGoal: Int = 8,
    onUpdateStepGoal: (Int) -> Unit = {},
    onUpdateWaterGoal: (Int) -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current

    var showStepGoalDialog by remember { mutableStateOf(false) }
    var stepGoalInput by remember(stepGoal) { mutableStateOf(stepGoal.toString()) }
    var showWaterGoalDialog by remember { mutableStateOf(false) }
    var waterGoalInput by remember(waterGoal) { mutableStateOf(waterGoal.toString()) }

    // Intercept system back button / gesture to dismiss dialogs or exit settings back to main screen
    BackHandler {
        if (showStepGoalDialog) {
            showStepGoalDialog = false
        } else if (showWaterGoalDialog) {
            showWaterGoalDialog = false
        } else {
            onClose()
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(onClick = onClose) {
                    Text(
                        text = "Done",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // --- SECTION 1: APPEARANCE ---
            SettingsSection(title = "APPEARANCE") {
                SettingsCard {
                    SettingsRow(
                        icon = Icons.Default.DarkMode,
                        iconBackground = Color(0xFF5856D6),
                        title = "Dark Theme",
                        subtitle = "Match your environment"
                    ) {
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onThemeToggle(it)
                            }
                        )
                    }
                }
            }

            // --- SECTION 2: HEALTH & GOALS ---
            SettingsSection(title = "HEALTH & TARGETS") {
                SettingsCard {
                    SettingsRow(
                        icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                        iconBackground = Color(0xFF34C759),
                        title = "Daily Step Goal",
                        subtitle = "$stepGoal steps",
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            stepGoalInput = stepGoal.toString()
                            showStepGoalDialog = true
                        }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$stepGoal",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(start = 56.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    SettingsRow(
                        icon = Icons.Default.WaterDrop,
                        iconBackground = Color(0xFF007AFF),
                        title = "Hydration Goal",
                        subtitle = "$waterGoal glasses per day",
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            waterGoalInput = waterGoal.toString()
                            showWaterGoalDialog = true
                        }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$waterGoal gls",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(start = 56.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    SettingsRow(
                        icon = Icons.Default.Bedtime,
                        iconBackground = Color(0xFFAF52DE),
                        title = "Sleep Target",
                        subtitle = "Recommended 8.0 hours"
                    ) {
                        Text(
                            text = "8.0 hrs",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }

            // --- SECTION 3: DATA & OVERVIEW ---
            SettingsSection(title = "DATA OVERVIEW") {
                SettingsCard {
                    SettingsRow(
                        icon = Icons.Default.Description,
                        iconBackground = Color(0xFFFF9500),
                        title = "Total Notes",
                        subtitle = "$notesCount items stored"
                    ) {
                        DataCountBadge(count = notesCount)
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(start = 56.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    SettingsRow(
                        icon = Icons.Default.Folder,
                        iconBackground = Color(0xFFFFCC00),
                        title = "Organized Folders",
                        subtitle = "$foldersCount categories"
                    ) {
                        DataCountBadge(count = foldersCount)
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(start = 56.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    SettingsRow(
                        icon = Icons.Default.Timer,
                        iconBackground = Color(0xFFFF3B30),
                        title = "Active Timers",
                        subtitle = "$timersCount countdowns"
                    ) {
                        DataCountBadge(count = timersCount)
                    }
                }
            }

            // --- SECTION 4: ABOUT & APP INFO ---
            SettingsSection(title = "ABOUT") {
                SettingsCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = "Activity App",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Version 0.15.0",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Developed by Veol Steve Jose",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        // Edit Step Goal Dialog
        if (showStepGoalDialog) {
            AlertDialog(
                onDismissRequest = { showStepGoalDialog = false },
                title = { Text("Daily Step Goal", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Set your personal target for daily walking activity.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        OutlinedTextField(
                            value = stepGoalInput,
                            onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) stepGoalInput = it },
                            label = { Text("Steps") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val newGoal = stepGoalInput.toIntOrNull() ?: 10000
                            onUpdateStepGoal(newGoal)
                            showStepGoalDialog = false
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showStepGoalDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Edit Water Goal Dialog
        if (showWaterGoalDialog) {
            AlertDialog(
                onDismissRequest = { showWaterGoalDialog = false },
                title = { Text("Hydration Goal", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Set your daily target for glasses of water.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        OutlinedTextField(
                            value = waterGoalInput,
                            onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) waterGoalInput = it },
                            label = { Text("Glasses") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val newGoal = waterGoalInput.toIntOrNull() ?: 8
                            onUpdateWaterGoal(newGoal)
                            showWaterGoalDialog = false
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showWaterGoalDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 12.dp)
        )
        content()
    }
}

@Composable
fun SettingsCard(
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column {
            content()
        }
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    iconBackground: Color,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit
) {
    val rowModifier = Modifier
        .fillMaxWidth()
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
        .padding(horizontal = 16.dp, vertical = 12.dp)

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }

        trailing()
    }
}

@Composable
fun DataCountBadge(count: Int) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}