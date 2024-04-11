package com.goal.aicontent

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, @StringRes val titleRes: Int, val icon: ImageVector) {
    data object MusicPromptView : Screen("music_prompt_view", R.string.title_prompt, Icons.Filled.MusicNote)
    data object MusicEditView : Screen("music_edit_view", R.string.title_edit, Icons.Filled.Cached)
    data object HomePage : Screen("homePage", R.string.title_home, Icons.Filled.Home)
    data object UserColorSettingsView : Screen("User_Color_Settings_View", R.string.User_Color_Settings_View, Icons.Filled.Edit)

}
