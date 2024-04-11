package com.goal.aicontent.musicgen.musicprompt

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import com.goal.aicontent.DownloadStatus
import com.goal.aicontent.models.MusicPromptViewModel
import com.goal.aicontent.models.TrimViewModel
import com.goal.aicontent.musicgen.musicprompt.elements.ConfirmDeletionDialog

enum class SortCriterion {
    None, Title, Duration
}

enum class SortOrder {
    Ascending, Descending
}
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(UnstableApi::class)
@Composable
fun DownloadableView(context: Context) {
    val viewModel: MusicPromptViewModel = viewModel()

    val selectedItemsOrder by viewModel.selectedItemsOrder.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var titleToDelete by remember { mutableStateOf("") }
    var deleteFromStorage by remember { mutableStateOf(false) }
    var sortCriterion by remember { mutableStateOf(SortCriterion.None) }
    var sortOrder by remember { mutableStateOf(SortOrder.Ascending) }

    val downloadableContent by viewModel.downloadableContents.collectAsState()
    val sortedContent = when (sortCriterion) {
        SortCriterion.Title -> if (sortOrder == SortOrder.Ascending) downloadableContent.sortedBy { it.title } else downloadableContent.sortedByDescending { it.title }
        SortCriterion.Duration -> if (sortOrder == SortOrder.Ascending) downloadableContent.sortedBy { it.duration } else downloadableContent.sortedByDescending { it.duration }
        else -> downloadableContent
    }
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentlyPlaying by viewModel.currentlyPlaying.collectAsState()


    LaunchedEffect(true) {
        Log.d("DownloadableView", "LaunchedEffect called to load persisted downloads")
        viewModel.loadDownloadableContents()
    }

    Column(modifier = Modifier.padding(12.dp)) {
        if (sortedContent.isEmpty()) {
            Log.d("DownloadableView", "No content to display")
        }
        sortedContent.forEach { content ->
            val isSelected = selectedItemsOrder.contains(content.title)
            val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { viewModel.toggleItemSelection(content.title) },
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = backgroundColor)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Text(
                            text = content.title,
                            modifier = Modifier
                                .weight(1f)
                                .padding(8.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (content.status.value != DownloadStatus.PROCESSING) {
                            IconButton(onClick = {
                                titleToDelete = content.title
                                showDialog = true
                            }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete")
                            }
                            when (content.status.value) {
                                DownloadStatus.NOT_DOWNLOADED,
                                DownloadStatus.DOWNLOADING -> IconButton(onClick = {
                                    content.downloadUrl?.let { url ->
                                        // Pass content.taskId as the finalTaskId parameter
                                        viewModel.downloadFile(context, url, "${content.title}.wav", content.taskId)
                                    }
                                }) {
                                    Icon(Icons.Filled.Download, contentDescription = "Download")
                                }
                                DownloadStatus.DOWNLOADED -> IconButton(onClick = {
                                    content.filePath?.let { filePath ->
                                        viewModel.playAudioFromUri(context, Uri.parse(filePath), content.title)
                                    }
                                }) {
                                    Icon(if (isPlaying && currentlyPlaying == content.title) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = "Play/Pause")
                                }
                                else -> {}
                            }
                        }
                    }
                    if (showDialog && titleToDelete == content.title) {
                        ConfirmDeletionDialog(
                            title = titleToDelete,
                            showDeleteFromFileSystemCheckbox = true,
                            deleteFromFileSystem = deleteFromStorage,
                            onConfirm = { deleteFromFileSystem ->
                                viewModel.removeItemFromDownloads(titleToDelete, deleteFromFileSystem)
                                showDialog = false
                            },
                            onDismiss = {
                                showDialog = false
                                deleteFromStorage = false
                            },
                            onCheckboxChange = { deleteFromStorage = it }
                        )
                    }
                    if (content.status.value == DownloadStatus.DOWNLOADING || content.status.value == DownloadStatus.PROCESSING) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}