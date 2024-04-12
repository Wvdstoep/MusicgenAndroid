package com.goal.aicontent.musicgen.musicprompt.homepage

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomSearchBar(searchQuery: MutableState<String>, onQueryChanged: (String) -> Unit) {
    val isFocused = remember { mutableStateOf(false) }
    // Use the animatedBorderColor function to animate the border color
    val borderColor = animatedBorderColor(isFocused.value).value
    val localFocusManager = LocalFocusManager.current

    OutlinedTextField(
        value = searchQuery.value,
        onValueChange = onQueryChanged,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = borderColor,
            unfocusedBorderColor = borderColor,
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
        singleLine = true,
        label = { Text("Search") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
        },
        trailingIcon = {
            if (searchQuery.value.isNotEmpty()) {
                IconButton(onClick = { searchQuery.value = "" }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        },
        placeholder = { Text("Enter a keyword", style = MaterialTheme.typography.bodyLarge) },
        shape = RoundedCornerShape(30.dp),
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = {
            localFocusManager.clearFocus() // Clear focus and dismiss the keyboard
            isFocused.value = false
        })
    )
}


