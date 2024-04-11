package com.goal.aicontent.edit

import android.net.Uri
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.goal.aicontent.models.AudioEditViewModel
import com.goal.aicontent.functions.DownloadableContent
import com.goal.aicontent.drawer.DrawerContent
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicEditView(navController: NavHostController) {
    val viewModel: AudioEditViewModel = viewModel()
    val isLoading = viewModel.isLoading.collectAsState().value
    val downloadableContents = viewModel.downloadableContents.collectAsState().value
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        scrimColor = Color.Transparent, // Keep the scrim transparent
        drawerContent = {
            // Conditionally render the DrawerContent only when the drawer is open
            if (drawerState.isOpen) {
                DrawerContent(navController, drawerState)
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "My Music App") },
                    navigationIcon = {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                if (drawerState.isClosed) {
                                    drawerState.open()
                                } else {
                                    drawerState.close()
                                }
                            }
                        }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        // Add an icon that navigates to the trimmed files list
                        IconButton(onClick = { navController.navigate("trimmedFilesList") }) {
                            Icon(Icons.AutoMirrored.Filled.List, contentDescription = "View Trimmed Files")
                        }
                    }
                )
            },
            content = { paddingValues ->
                Surface(modifier = Modifier.fillMaxSize()) {
                    MusicEditContent(
                        isLoading = isLoading,
                        downloadableContents = downloadableContents,
                        navController = navController,
                        viewModel = viewModel,
                        paddingValues = paddingValues // Pass paddingValues to MusicEditContent
                    )
                }
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun MusicEditContent(
    isLoading: Boolean,
    downloadableContents: List<DownloadableContent>,
    navController: NavHostController,
    viewModel: AudioEditViewModel,
    paddingValues: PaddingValues // Receive paddingValues parameter
) {
    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Music Converter",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(20.dp))
        FilePickerButton(isLoading, viewModel)
        Spacer(modifier = Modifier.height(20.dp))
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        } else {
            DownloadableContentsList(downloadableContents, navController, viewModel)
        }
    }
}



@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun FilePickerButton(isLoading: Boolean, viewModel: AudioEditViewModel) {
    // File picker launcher setup remains the same
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    val pickMp3Launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        selectedUri = uri
        uri?.let { viewModel.processFile(it) }
    }

    Button(
        onClick = { if (!isLoading) pickMp3Launcher.launch("audio/mpeg") },
        enabled = !isLoading,
        shape = RoundedCornerShape(50), // More rounded corners
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Text(
            text = if (!isLoading) "MP3-> WAV Conversion" else "Processing...",
            color = Color.White
        )
    }
}

@Composable
fun DownloadableContentsList(contents: List<DownloadableContent>,  navController: NavHostController, viewModel: AudioEditViewModel) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(contents) { content ->
            DownloadableContentRow(content, navController, viewModel)
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        }
    }
}
@Composable
fun DownloadableContentRow(content: DownloadableContent,  navController: NavHostController, viewModel: AudioEditViewModel) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                content.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Duration: ${content.duration} seconds",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = { /* TODO: Implement play functionality */ }) {
            Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = MaterialTheme.colorScheme.primary)
        }
        IconButton(onClick = {
            content.filePath?.let { viewModel.validateAndNavigate(it, context, navController) }
        }) {
            Icon(Icons.Filled.Edit, contentDescription = "Trim", tint = MaterialTheme.colorScheme.primary)
        }
        IconButton(onClick = { /* TODO: Implement download functionality */ }) {
            Icon(Icons.Filled.Download, contentDescription = "Download", tint = MaterialTheme.colorScheme.primary)
        }
    }
}


