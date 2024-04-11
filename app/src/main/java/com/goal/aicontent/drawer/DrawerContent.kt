package com.goal.aicontent.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.goal.aicontent.Screen
import kotlinx.coroutines.launch


@Composable
fun DrawerContent(navController: NavHostController, drawerState: DrawerState) {
    val coroutineScope = rememberCoroutineScope()


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(top = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally // Center items horizontally
    ) {
        // Drawer Header
        DrawerHeader()

        // Divider
        Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))

        Spacer(modifier = Modifier.height(16.dp))

        // Drawer Items
        DrawerItem(text = "Home", icon = Icons.Filled.Home, onClick = { /* Handle click */ })
        DrawerItem(text = "Settings", icon = Icons.Filled.Settings,     onClick = {
            navController.navigate(Screen.UserColorSettingsView.route)
        })
        DrawerItem(text = "About", icon = Icons.Filled.Info, onClick = { /* Handle click */ })
        DrawerItem(text = "Edit Music", icon = Icons.Filled.Edit, onClick = {
            coroutineScope.launch {
                drawerState.close() // Close the drawer inside the coroutine
                navController.navigate(Screen.MusicEditView.route) // Navigate after closing
            }
        })
        Spacer(modifier = Modifier.weight(1f))

        // Footer or any additional content
        Text(
            text = "Version 1.0.0",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 20.dp)
        )
    }
}