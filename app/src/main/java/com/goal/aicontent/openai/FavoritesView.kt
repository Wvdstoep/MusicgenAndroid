package com.goal.aicontent.openai

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.goal.aicontent.models.ChatViewModel

@Composable
fun FavoritesView(viewModel: ChatViewModel = viewModel()) {
    val favorites by viewModel.favorites.collectAsState()

    LazyColumn {
        items(favorites.entries.toList()) { favorite ->
            Text(text = favorite.value, modifier = Modifier.padding(8.dp))
            Divider()
        }
    }
}
