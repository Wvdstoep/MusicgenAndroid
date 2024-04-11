package com.goal.aicontent.musicgen.musicprompt.elements

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.goal.aicontent.musicgen.musicprompt.elements.PromptCard


@Composable
fun PromptsSelector(prompts: List<String>, onPromptSelected: (String) -> Unit) {
    // Horizontal carousel for prompts
    LazyRow(modifier = Modifier.padding(vertical = 8.dp)) {
        items(prompts) { prompt ->
            PromptCard(prompt = prompt, onPromptSelected = onPromptSelected)
        }
    }
}

