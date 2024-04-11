package com.goal.aicontent.musicgen.musicprompt

import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import com.goal.aicontent.models.ChatViewModel
import com.goal.aicontent.models.MusicPromptViewModel
import com.goal.aicontent.musicgen.musicprompt.promptgenview.MessageBubble
import com.goal.aicontent.musicgen.musicprompt.promptgenview.PremiumSlider
import com.goal.aicontent.musicgen.musicprompt.promptgenview.WelcomeMessageBubble
import com.goal.aicontent.musicgen.musicprompt.promptgenview.waveform.FilePreviewBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@kotlin.OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun PromptGenView( modifier: Modifier = Modifier) {
    val chatViewModel: ChatViewModel = viewModel()
    val musicPromptViewModel: MusicPromptViewModel = viewModel()
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    Log.d("FileSelection", "File Name: $selectedFileName, URI: $selectedFileUri")
    var waveform by remember { mutableStateOf<List<Float>>(emptyList()) }
    var durationInSeconds by remember { mutableStateOf(0) }
    val showDialog = remember { mutableStateOf(false) }
    val chatHistory by chatViewModel.chatHistory.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val isLoading by chatViewModel.isLoading.collectAsState()
    val context = LocalContext.current
    var showChoiceDialog by remember { mutableStateOf(false) }
    val selectedDuration by musicPromptViewModel.duration.collectAsState()

    val lazyListState = rememberLazyListState()
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                val fileName = DocumentFile.fromSingleUri(context, uri)?.name ?: "Unknown File"
                selectedFileName = fileName
                waveform = chatViewModel.generateWaveformData(context, uri)
                durationInSeconds = chatViewModel.calculateAudioDuration(context, uri)
            }
        }
    )
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            // Encapsulate both FilePreviewBar and TextField in a Column
            Column {
                if (waveform.isNotEmpty()) {
                    FilePreviewBar("Selected File", waveform, onRemove = { waveform = listOf() },                        durationInSeconds = durationInSeconds,
                    )
                }
                selectedFileUri?.let {
                    FilePreviewBar(
                        fileName = selectedFileName ?: "Selected File",
                        waveform = waveform,
                        durationInSeconds = durationInSeconds,
                        onRemove = {
                            selectedFileName = null
                            waveform = emptyList() // Clear waveform data
                        }
                    )
                }

                // Row for the TextField and Send button
                Row(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 36.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp), // Add some padding if needed
                        placeholder = { Text("Type a message...") },
                        singleLine = true,
                        trailingIcon = {
                            // Container for both icons
                            Row {
                                // Attachment icon
                                IconButton(onClick = {
                                    filePickerLauncher.launch("audio/*")
                                    Log.d("PromptGenView", "Attachment icon clicked")
                                }) {
                                    Icon(Icons.Filled.AttachFile, contentDescription = "Attach")
                                }
                                // Send message icon
                                IconButton(onClick = {
                                    if (messageText.isNotBlank()) {
                                        showChoiceDialog = true // Show the choice dialog instead of directly showing the generate dialog
                                    }
                                }) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                                }

                            }
                        },
                        colors = TextFieldDefaults.textFieldColors(
                            // Customize colors to match your theme
                            containerColor = MaterialTheme.colorScheme.surface,
                            cursorColor = MaterialTheme.colorScheme.onSurface,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding).padding(horizontal = 8.dp),
            state = lazyListState
        ) {
            if (chatHistory.isEmpty()) {
                item {
                    WelcomeMessageBubble() // Define this Composable to show the welcome message
                }
            } else {
                items(chatHistory) { message ->
                    MessageBubble(message = message, onUsePrompt = { selectedPrompt ->
                        musicPromptViewModel.generateMusic(selectedPrompt)
                    })
                }
            }
        }
    }
    if (isLoading) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            CircularProgressIndicator()
        }
    }
    val scope = rememberCoroutineScope()
    if (showChoiceDialog) {
        AlertDialog(
            onDismissRequest = { showChoiceDialog = false },
            title = { Text("Choose Action") },
            text = {
                Text("Do you want to generate music with this text or get help?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showChoiceDialog = false
                        showDialog.value = true // Proceed to show the generate song dialog
                    }
                ) { Text("Generate Music") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showChoiceDialog = false
                    if (messageText.isNotBlank()) {
                        chatViewModel.sendMessage(messageText.trim())
                        messageText = ""
                    }
                }) {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }
        )
    }
    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text("Generate Song?") },
            text = {
                Column {
                    Text("Choose the duration for your song:")
                    Spacer(modifier = Modifier.height(8.dp))
                    PremiumSlider(
                        selectedDuration = selectedDuration,
                        onDurationChange = {  musicPromptViewModel.setDuration(it) },
                        onCrownClick = {
                            // Define what happens when the crown is clicked
                            // For example, showing a dialog or toast
                            Log.d("PremiumFeature", "Crown clicked - premium feature")
                        }
                    )
                    Text("${selectedDuration.toInt()} seconds")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog.value = false
                        musicPromptViewModel.generateMusic(messageText) // Use the current ViewModel's duration
                    },
                   // enabled = selectedDuration <= 9 // Button is disabled if the duration is over 8 seconds
                ) { Text("Yes, generate") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog.value = false }) { Text("No, thanks") }
            }
        )
    }

    LaunchedEffect(chatHistory.size) {
        val lastIndex = chatHistory.lastIndex
        if (lastIndex >= 0) { // Check if lastIndex is non-negative
            scope.launch {
                // Delay a bit to ensure the list is updated before scrolling
                delay(100)
                // Scroll to the last item
                lazyListState.animateScrollToItem(lastIndex)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(UnstableApi::class)
@Composable
fun ClickableMessageSegment(text: String) {
    val showDialog = remember { mutableStateOf(false) }
    val musicPromptViewModel: MusicPromptViewModel = viewModel()

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text("Generate Song?") },
            text = {
                Column {
                    Text("Your song will be generated with an 8-second duration.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Want the full version? Upgrade to our premium or pro version for the complete experience.")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog.value = false
                        musicPromptViewModel.setDuration(8f) // Set the duration to 8 seconds
                        musicPromptViewModel.generateMusic(text) // Then generate the music
                    }
                ) { Text("Yes, generate") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog.value = false }) { Text("No, thanks") }
            }
        )
    }

    Text(
        text = text,
        modifier = Modifier.clickable { showDialog.value = true }
            .padding(4.dp)
            .background(Color.LightGray),
        color = Color.Black
    )
}