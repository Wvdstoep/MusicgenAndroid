package com.goal.aicontent

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.goal.aicontent.drawer.DrawerContent
import com.goal.aicontent.models.MusicPromptViewModel
import com.goal.aicontent.musicgen.musicprompt.MediaCarousel
import com.goal.aicontent.navigation.BottomSheetContent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun HomePageViewWrapper(viewModel: HomePageViewModel, navController: NavHostController) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
    )

    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetContent = { BottomSheetContent(navController, bottomSheetScaffoldState) }, // Define your bottom sheet content here
        sheetPeekHeight = 30.dp,
        topBar = {
            TopAppBar(
                title = { Text("MusicBox", color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = {
                        coroutineScope.launch {
                            drawerState.open()
                        }
                    }) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            scrimColor = Color.Transparent,
            drawerContent = {
                if (drawerState.isOpen) {
                    DrawerContent(navController, drawerState) // Define your drawer content here
                }
            }
        ) {
            Scaffold { paddingValues ->
                HomePageView(modifier = Modifier.padding(paddingValues))
            }
        }
    }
}
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun HomePageView(modifier: Modifier = Modifier) {
    val musicPromptViewModel: MusicPromptViewModel = viewModel()
    val homePageViewModel: HomePageViewModel = viewModel()
    val tasks by homePageViewModel.items.collectAsState()
    val context = LocalContext.current // Obtain Context using LocalContext

    LazyColumn(
        contentPadding = PaddingValues() // Adjust padding as needed
    ) {
        item {
            Text(
                text = "Featured Items",
                modifier = Modifier
                    .fillMaxWidth() // Ensures the Text composable fills the available width
                    .padding(bottom = 8.dp), // Adds some space below the text
                color = Color.Black, // Choose a color that fits your app theme
                fontSize = 24.sp, // Adjust font size to suit your needs
                fontWeight = FontWeight.Bold, // Make the text bold
                textAlign = TextAlign.Center, // Centers the text horizontally
                style = MaterialTheme.typography.headlineMedium.copy(letterSpacing = 0.15.sp) // Example of additional styling
            )

            MediaCarousel(
                list = tasks,
                onItemClicked = { taskItem ->
                    // Your existing click handling logic
                    taskItem.downloadUrl?.let { homePageViewModel.playAudioFromUrl(context, it) }
                }
            )
        }
    }
}