package com.weeeol.activityapp.ui.health

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun StepGoalDialog(
    currentGoal: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var goalInput by remember { mutableStateOf(currentGoal.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Daily Step Goal") },
        text = {
            OutlinedTextField(
                value = goalInput,
                onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) goalInput = it },
                label = { Text("Steps") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = {
                val newGoal = goalInput.toIntOrNull() ?: 10000
                onConfirm(newGoal)
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun WeatherDialog(
    currentLocation: String,
    currentCondition: String,
    onDismiss: () -> Unit,
    onConfirm: (location: String, condition: String) -> Unit
) {
    var tempLocationInput by remember { mutableStateOf(currentLocation) }
    var selectedCondition by remember { mutableStateOf(currentCondition) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Weather") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = tempLocationInput,
                    onValueChange = { tempLocationInput = it },
                    label = { Text("Location") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(
                        onClick = { selectedCondition = "Sunny" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedCondition == "Sunny") MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    ) { Text("Sunny") }
                    Button(
                        onClick = { selectedCondition = "Cloudy" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedCondition == "Cloudy") MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    ) { Text("Cloudy") }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val loc = tempLocationInput.ifBlank { "Unknown" }
                onConfirm(loc, selectedCondition)
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
