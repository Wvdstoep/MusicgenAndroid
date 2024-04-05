package com.goal.aicontent.navigation

import android.os.Build
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.goal.aicontent.Screen
import com.goal.aicontent.musicgen.MusicPromptView

import com.goal.aicontent.musicgen.MusicPromptViewModel

@OptIn(UnstableApi::class) @RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun NavigationGraph(navController: NavHostController, paddingValues: PaddingValues) {


    val viewModel: MusicPromptViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.MusicPromptView.route,
        modifier = Modifier.padding(paddingValues) // Apply padding here
    ) {
        composable(Screen.MusicPromptView.route) {
            MusicPromptView(viewModel = viewModel, navController = navController)
        }
        composable("musicPrompt") { MusicPromptView(viewModel = viewModel, navController = navController) }

    }
}


