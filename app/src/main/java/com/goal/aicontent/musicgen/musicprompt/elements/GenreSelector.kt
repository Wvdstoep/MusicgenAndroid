package com.goal.aicontent.musicgen.musicprompt.elements

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.goal.aicontent.musicgen.musicprompt.elements.GenreCard

@Composable
fun GenreSelector(genres: List<String>, onGenreSelected: (String) -> Unit) {
    // Horizontal carousel for genres
    LazyRow(modifier = Modifier.padding(vertical = 8.dp)) {
        items(genres) { genre ->
            GenreCard(genre = genre, onGenreSelected = onGenreSelected)
        }
    }
}