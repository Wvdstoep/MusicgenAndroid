package com.goal.aicontent.musicgen.musicprompt.elements

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.goal.aicontent.functions.TrimmedFileData
import com.goal.aicontent.models.MusicPromptViewModel
import com.goal.aicontent.models.TrimViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(UnstableApi::class)
@Composable
fun TrimmedFilesList(
    viewModel: MusicPromptViewModel,
) {
    val models: List<String> = listOf("musicgen-small", "musicgen-medium", "musicgen-melody")
    val durations: List<Int> = listOf(5, 10, 15, 30) // Sample durations

    val context = LocalContext.current
    var trimmedFiles by remember { mutableStateOf(listOf<TrimmedFileData>()) }
    var showDialogForFile by remember { mutableStateOf<TrimmedFileData?>(null) }

    LaunchedEffect(key1 = Unit) {
        val sharedPreferences = context.getSharedPreferences("trimmedFiles", Context.MODE_PRIVATE)
        val gson = Gson()
        val dataJson = sharedPreferences.getString("trimmedDataList", "[]")
        trimmedFiles = gson.fromJson(dataJson, object : TypeToken<List<TrimmedFileData>>() {}.type)
    }

    LazyColumn {
        items(trimmedFiles) { file ->
            ListItem(file) {
                showDialogForFile = file // keep showDialogForFile as TrimmedFileData?
            }
        }
    }

    if (showDialogForFile != null) {
        UnifiedApiCallDialog(
            models = models,
            durations = durations,
            onApiCallRequested = { apiFunction, model, duration, promptText ->
                val fileData = showDialogForFile!!
                // Assuming fileData.filePath is a content URI string
                val fileUri = Uri.parse(fileData.filePath)

                when (apiFunction) {
                   // "Generate Continuation" -> viewModel.generateContinuationForFile(fileUri, promptText, context)
                    //"Upload with Description" -> viewModel.uploadFileWithDescription(fileUri, promptText, context)
                }
            },
            onClose = { showDialogForFile = null },
            viewModel
        )
    }
}


@Composable
fun ListItem(trimmedFileData: TrimmedFileData, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { onClick() },
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Title: ${trimmedFileData.title}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Duration: ${trimmedFileData.duration}ms", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = { onClick() }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Actions")
            }
        }
    }
}
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(UnstableApi::class)
@Composable
fun UnifiedApiCallDialog(
    models: List<String> = listOf("Model A", "Model B"), // Default model list
    durations: List<Int> = listOf(5, 10, 15, 30), // Default duration list
    onApiCallRequested: (String, String, Int, String) -> Unit, // API function, model, duration, prompt
    onClose: () -> Unit,
    viewModel: MusicPromptViewModel // Pass ViewModel as a parameter

) {
    var selectedModel by remember { mutableStateOf(models.first()) }
    var selectedDuration by remember { mutableIntStateOf(durations.first()) }
    var promptText by remember { mutableStateOf(TextFieldValue("")) }
    var selectedFunction by remember { mutableStateOf("generateContinuationForFile") }
    var apiFunction by remember { mutableStateOf("Generate Continuation") }

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Customize API Call") },
        text = {
            Column {
                Text("Select the model and duration, and input your music prompt:")
                DropdownMenu(
                    options = listOf("Generate Continuation", "Upload with Description"),
                    selectedOption = apiFunction,
                    onOptionSelected = { selected ->
                        apiFunction = selected
                    }
                )
                Spacer(Modifier.height(8.dp))
                // Model selection
                ModelSelectionButton(models = models, selectedModel = selectedModel) { model ->
                    selectedModel = model
                }
                Spacer(Modifier.height(8.dp))
                // Duration selection
                DurationSelectionButton(selectedDuration = selectedDuration, onSelect = { duration ->
                    selectedDuration = duration
                })
                Spacer(Modifier.height(8.dp))
                // Music prompt input
                MusicPromptInputField(value = promptText, onValueChange = { newValue ->
                    promptText = newValue
                })
            }
        },
        confirmButton = {
            Button(onClick = {
                // Update ViewModel with the selected model and duration before making the API call
                viewModel.setSelectedModel(selectedModel)
                viewModel.setDuration(selectedDuration.toFloat())

                onApiCallRequested(apiFunction, selectedModel, selectedDuration, promptText.text)
                onClose()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            Button(onClick = onClose) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DropdownMenu(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        TextButton(
            onClick = { expanded = !expanded }
        ) {
            Text(text = selectedOption)
        }
        if (expanded) {
            options.forEach { option ->
                TextButton(
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                ) {
                    Text(text = option)
                }
            }
        }
    }
}
