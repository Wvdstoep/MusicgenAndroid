package com.goal.aicontent.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material.Badge
import androidx.compose.material.BadgedBox
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Merge
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import com.goal.aicontent.models.MusicPromptViewModel
import com.goal.aicontent.models.TrimViewModel

@androidx.annotation.OptIn(UnstableApi::class)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun MergeFilesFABWithConfirmation(viewModel: MusicPromptViewModel) {
    val context = LocalContext.current
    val selectedItemsCount = viewModel.selectedItemsOrder.collectAsState().value.size
    var showDialog by remember { mutableStateOf(false) }
    val trimViewModel: TrimViewModel = viewModel()

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
            },
            title = {
                Text(text = "Confirm Merge")
            },
            text = {
                Text(text = "Are you sure you want to merge the selected files?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.concatenateSelectedAudioFiles(context)
                        showDialog = false
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    BadgedBox(badge = {
        if (selectedItemsCount > 0) {
            Badge(backgroundColor = Color.White) {
                Text(
                    text = selectedItemsCount.toString(),
                    fontSize = 10.sp,
                    color = Color.Black
                )
            }
        }
    }) {
        FloatingActionButton(onClick = { showDialog = true }) {
            Icon(Icons.Filled.Merge, contentDescription = "Merge")
        }
    }
}