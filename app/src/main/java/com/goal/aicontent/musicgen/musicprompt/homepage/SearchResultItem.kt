package com.goal.aicontent.musicgen.musicprompt.homepage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.goal.aicontent.HomePageViewModel
import com.goal.aicontent.functions.TaskItem

@Composable
fun SearchResultItem(taskItem: TaskItem, onPlayClick: (String) -> Unit, onFavoriteToggle: (String) -> Unit  // Expecting a String for itemId
) {
    val homePageViewModel: HomePageViewModel = viewModel()

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {
                Text(
                    text = taskItem.prompt,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Duration: ${taskItem.duration}s",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            IconButton(onClick = {
                onFavoriteToggle(taskItem.id) // Correctly passing the ID
            }) {
                Icon(
                    imageVector = if (taskItem.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Toggle Favorite",
                    tint = if (taskItem.isFavorite) Color.Red else Color.Gray
                )
            }

            IconButton(onClick = { taskItem.downloadUrl?.let(onPlayClick) }) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play")
            }
        }
    }
}
