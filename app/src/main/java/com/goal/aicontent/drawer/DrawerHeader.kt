package com.goal.aicontent.drawer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.goal.aicontent.R

@Composable
fun DrawerHeader() {
    // Consider replacing with actual image resources or avatars
    Image(
        painter = painterResource(id = R.drawable.ic_launcher_background), // Your logo or header image
        contentDescription = "App Logo",
        modifier = Modifier
            .size(100.dp)
    )
    Text(
        text = "My Music App",
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier
    )
}