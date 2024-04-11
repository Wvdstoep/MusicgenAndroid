package com.goal.aicontent.musicgen.musicprompt

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.navigation.NavHostController
import com.goal.aicontent.musicgen.musicprompt.elements.DurationSelectionButton
import com.goal.aicontent.musicgen.musicprompt.elements.GenreSelector
import com.goal.aicontent.musicgen.musicprompt.elements.MusicPromptInputField
import com.goal.aicontent.models.MusicPromptViewModel
import com.goal.aicontent.musicgen.musicprompt.elements.PromptsSelector

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(UnstableApi::class)
@Composable
fun SampleGenView(viewModel: MusicPromptViewModel, navController: NavHostController, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var selectedGenre by remember { mutableStateOf<String?>(null) }
    var promptText by remember { mutableStateOf(TextFieldValue("")) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    val isLoading by viewModel.isLoading.collectAsState()
    listOf("musicgen-small", "musicgen-medium", "musicgen-melody")
    var selectedDuration by remember { mutableIntStateOf(listOf(5, 8, 12, 15, 20, 25, 30).first()) }

    val filePickerLauncherApi = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                selectedFileUri = uri
            }
        }
    )

    Surface(modifier = modifier.fillMaxSize().padding(16.dp)) {
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Text(
                    "Create Your Music",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground // Use the onBackground color for text

                ) }
            item {
                GenreSelector(viewModel.genres.keys.toList()) { genre ->
                    selectedGenre = genre
                }
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                if (selectedGenre != null) {
                    PromptsSelector(viewModel.genres[selectedGenre] ?: listOf()) { selectedPrompt ->
                        promptText = TextFieldValue(selectedPrompt)
                    }
                }
                MusicPromptInputField(promptText, onValueChange = { promptText = it })
            }
            item {
                Row(modifier = Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    DurationSelectionButton(selectedDuration) { selectedDuration = it; viewModel.setDuration(it.toFloat()) }
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { filePickerLauncherApi.launch("audio/*") }) {
                    Text("Select WAV File")
                }
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp).size(24.dp))
                } else {
                    Button(
                        modifier = Modifier.padding(top = 8.dp),
                        onClick = {
                            selectedFileUri?.let { uri ->
                                //viewModel.uploadFileWithDescription(uri, promptText.text, context)
                            }
                        },
                        enabled = selectedFileUri != null // Enable the button only if a file is selected
                    ) {
                        Text("Upload & Generate Music")
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
