package com.goal.aicontent.navigation

import android.net.Uri
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.goal.aicontent.HomePageView
import com.goal.aicontent.HomePageViewModel
import com.goal.aicontent.HomePageViewWrapper
import com.goal.aicontent.models.AudioEditViewModel
import com.goal.aicontent.models.ChatViewModel
import com.goal.aicontent.Screen
import com.goal.aicontent.edit.TrimView
import com.goal.aicontent.models.TrimViewModel
import com.goal.aicontent.openai.FavoritesView
import com.goal.aicontent.edit.MusicEditView
import com.goal.aicontent.musicgen.musicprompt.MusicPromptView

import com.goal.aicontent.models.MusicPromptViewModel
import com.goal.aicontent.musicgen.musicprompt.elements.TrimmedFilesList
import com.goal.aicontent.ui.theme.ThemeViewModel
import com.goal.aicontent.ui.theme.UserColorSettingsView

@OptIn(UnstableApi::class) @RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun NavigationGraph(navController: NavHostController, paddingValues: PaddingValues) {


    val viewModel: MusicPromptViewModel = viewModel()
    val editViewModel: AudioEditViewModel = viewModel()
    val chatViewModel: ChatViewModel = viewModel()
    val themeViewModel: ThemeViewModel = viewModel()
    val musicPromptViewModel: MusicPromptViewModel = viewModel()
    val homePageViewModel: HomePageViewModel = viewModel() // Add your HomePageViewModel

    NavHost(
        navController = navController,
        startDestination = Screen.HomePage.route, // Change startDestination to HomePage
        modifier = Modifier.padding(paddingValues) // Apply padding here
    ) {
        composable(Screen.MusicPromptView.route) {
            MusicPromptView(viewModel = viewModel, navController = navController)
        }
        composable(Screen.MusicEditView.route) {
            MusicEditView( navController = navController)
        }
        composable(Screen.UserColorSettingsView.route) {
            UserColorSettingsView(viewModel = themeViewModel)
        }
        composable(Screen.HomePage.route) {
            HomePageViewWrapper(viewModel = homePageViewModel, navController = navController)
        }

        composable("trimmedFilesList") { TrimmedFilesList(viewModel = musicPromptViewModel) }

        composable("musicPrompt") { MusicPromptView(viewModel = viewModel, navController = navController) }
        composable(
            route = "trimViewRoute/{mediaUrl}",
            arguments = listOf(navArgument("mediaUrl") { type = NavType.StringType })
        ) { backStackEntry ->
            val mediaUrlString = backStackEntry.arguments?.getString("mediaUrl") ?: ""
            val mediaUri = Uri.parse(mediaUrlString)
            val trimViewModel: TrimViewModel = viewModel()
            trimViewModel.prepareMediaData(mediaUri)
            TrimView(navController = navController, trimViewModel = trimViewModel) {
                navController.popBackStack()
            }
        }

    }
}


