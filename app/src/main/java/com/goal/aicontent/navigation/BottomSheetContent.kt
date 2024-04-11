package com.goal.aicontent.navigation

import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Badge
import androidx.compose.material.BadgedBox
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import com.goal.aicontent.models.MusicPromptViewModel
import com.goal.aicontent.models.TrimViewModel
import com.goal.aicontent.musicgen.musicprompt.DownloadableView
import kotlinx.coroutines.launch

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterialApi::class)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun BottomSheetContent(navController: NavHostController, bottomSheetScaffoldState: BottomSheetScaffoldState
) {
    val context = LocalContext.current
    val viewModel: MusicPromptViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()
    val selectedItemsCount = viewModel.selectedItemsOrder.collectAsState().value.size
    val selectedItems = viewModel.selectedItemsOrder.collectAsState().value
    var showDialog by remember { mutableStateOf(false) }
    val trimViewModel: TrimViewModel = viewModel()

    val icon = if (bottomSheetScaffoldState.bottomSheetState.isExpanded) {
        Icons.Filled.ArrowDropDown
    } else {
        Icons.Filled.ArrowDropUp
    }
    Scaffold{ paddingValues ->
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            // Toggle the bottom sheet state between expanded and collapsed
                            if (bottomSheetScaffoldState.bottomSheetState.isExpanded) {
                                bottomSheetScaffoldState.bottomSheetState.collapse()
                            } else {
                                bottomSheetScaffoldState.bottomSheetState.expand()
                            }
                        }
                    }) {
                    Icon(icon, contentDescription = "Toggle Bottom Sheet")
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                MergeFilesFABWithConfirmation(viewModel)

                FloatingActionButton(
                    onClick = {
                        if (selectedItemsCount == 1) {
                            trimViewModel.editSelectedItem(context, navController)
                        } else if (selectedItemsCount > 1) {
                            Toast.makeText(context, "Please select only one item to edit.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "No item selected for editing", Toast.LENGTH_SHORT).show()
                        }
                    },
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit Selected")
                }
                FloatingActionButton(
                    onClick = {
                        if (selectedItemsCount == 1) {
                            val firstSelectedItemTitle = selectedItems.first()
                            viewModel.downloadableContents.value.find { it.title == firstSelectedItemTitle }?.filePath?.let { filePath ->
                                val fileUri = Uri.parse(filePath)
                                trimViewModel.shareSong(fileUri, firstSelectedItemTitle)
                            }
                        } else if (selectedItemsCount > 1) {
                            Toast.makeText(context, "Please select only one item to share.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Please select an item to share.", Toast.LENGTH_SHORT).show()
                        }
                    },
                ) {
                    Icon(Icons.Filled.Share, contentDescription = "Share")
                }
                FloatingActionButton(onClick = { /* Action 4 */ }) {
                    Icon(Icons.Filled.Favorite, contentDescription = "Action 4")
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
                    FloatingActionButton(
                        onClick = {
                            showDialog = true
                        },
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Remove Selected Items")
                    }
                }
            }
            LazyColumn(modifier = Modifier.padding(paddingValues)) {

                item {

                    DownloadableView(context = context)
                }
            }
        }
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
            },
            title = {
                Text(text = "Confirm Removal")
            },
            text = {
                Text("Are you sure you want to remove the selected items?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Perform the removal
                        viewModel.removeSelectedItems()
                        Toast.makeText(context, "Selected items removed", Toast.LENGTH_SHORT).show()
                        showDialog = false // Dismiss the dialog
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

}