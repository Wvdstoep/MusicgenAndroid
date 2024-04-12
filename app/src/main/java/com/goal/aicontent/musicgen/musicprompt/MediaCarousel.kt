package com.goal.aicontent.musicgen.musicprompt

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.goal.aicontent.R
import com.goal.aicontent.functions.TaskItem
import kotlin.math.absoluteValue

@OptIn(
    ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class
)
@Composable
fun MediaCarousel(
    list: List<TaskItem>,
    totalItemsToShow: Int = 50,
    carouselLabel: String = "",
    onItemClicked: (TaskItem) -> Unit
) {
    val pageCount = list.size.coerceAtMost(totalItemsToShow) // Use 'size' instead of 'itemCount'
    val pagerState: PagerState = rememberPagerState(pageCount = { pageCount })
    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box {
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(
                    horizontal = dimensionResource(id = R.dimen.double_padding)
                ),
                pageSpacing = dimensionResource(id = R.dimen.normal_padding)
            ) { page: Int ->
                val item: TaskItem = list[page]
                item.let {
                    Card(
                        onClick = { onItemClicked(it) },
                        modifier = Modifier
                            .carouselTransition(
                            page,
                            pagerState
                        )
                    ) {
                        CarouselBox(it, onItemClicked)
                    }
                }
            }
        }

        if (carouselLabel.isNotBlank()) {
            Text(
                text = carouselLabel,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.carouselTransition(
    page: Int,
    pagerState: PagerState
) = graphicsLayer {
    val pageOffset =
        ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue

    val transformation = lerp(
        start = 0.8f,
        stop = 1f,
        fraction = 1f - pageOffset.coerceIn(
            0f,
            1f
        )
    )
    alpha = transformation
    scaleY = transformation
}
@Composable
fun CarouselBox(item: TaskItem, onPlayClick: (TaskItem) -> Unit) {
    Box(
        modifier = Modifier
            .padding(16.dp) // Increased padding for a bit more space around the edges.
            .fillMaxWidth()
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Icon to indicate this is a song, centered horizontally.
            Icon(
                imageVector = Icons.Filled.MusicNote,
                contentDescription = "Music",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp) // Reduced size for a more balanced appearance.
            )

            Spacer(modifier = Modifier.height(16.dp)) // Increased spacing for better separation.

            // Use a custom composable if the prompt text is too long to fit.
            ExpandableText(
                text = item.prompt,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 8.dp) // Add horizontal padding for the text.
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Model: ${item.model_name}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Duration: ${item.duration}s",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp)) // More spacing before the button for a cleaner look.

            // Simplified button layout with clearer visual hierarchy.
            Button(onClick = { onPlayClick(item) }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White
                )
                Text("Play", color = Color.White, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}
// Example of an ExpandableText composable function. You'll need to implement the logic for expanding and collapsing the text.
@Composable
fun ExpandableText(text: String, color: Color, style: TextStyle, modifier: Modifier = Modifier) {
    var isExpanded by remember { mutableStateOf(false) }

    Text(
        text = text,
        color = color,
        style = style,
        modifier = modifier.clickable { isExpanded = !isExpanded },
        maxLines = if (isExpanded) Int.MAX_VALUE else 3, // Change this value based on your preference
        overflow = TextOverflow.Ellipsis
    )
}