package com.goal.aicontent.musicgen

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController

@OptIn(UnstableApi::class) @RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun MusicPromptView(viewModel: MusicPromptViewModel, navController: NavHostController,
) {
    val context = LocalContext.current
    var selectedGenre by remember { mutableStateOf<String?>(null) }
    val genres = mapOf(
        "Pop Music" to listOf(
            "Create a catchy and upbeat chorus with a memorable hook.",
            "Generate a verse with a modern pop sound and relatable lyrics.",
            "Provide a danceable drum pattern for a pop party anthem.",
            "Design a synth riff that’s both energetic and infectious.",
            "Generate a bridge that builds anticipation and leads to an explosive final chorus."
        ),
        "Classical Music" to listOf(
            "Generate a melancholic and emotive violin melody in a minor key.",
            "Provide a regal and majestic brass section for a grand orchestral piece.",
            "Create a delicate and serene piano introduction for a sonata.",
            "Design a contrapuntal melody for a chamber music ensemble.",
            "Generate a hauntingly beautiful choir arrangement for a choral composition."
        ),
        "Hip-Hop/Rap" to listOf(
            "Create a head-nodding beat with a deep bassline for a rap freestyle.",
            "Generate a catchy hook with rhymes that flow seamlessly together.",
            "Provide a trap-style drum pattern with hi-hats that add rhythm and groove.",
            "Design a verse with confident and clever wordplay.",
            "Generate a chorus that incorporates auto-tuned vocals for a melodic rap track."
        ),
        "Rock Music" to listOf(
            "Create a powerful guitar riff that sets the tone for a rock anthem.",
            "Generate a hard-hitting drum pattern with driving rhythms for a rock ballad.",
            "Provide a dynamic and intense build-up for a guitar solo.",
            "Design a gritty and raw vocal melody for a rock song with attitude.",
            "Generate a bridge that transitions smoothly from a soft verse to a loud chorus."
        ),
        "Electronic/EDM" to listOf(
            "Create an infectious and energetic synth melody for a club banger.",
            "Generate a drop that unleashes a burst of energy with pounding bass and synths.",
            "Provide a build-up that elevates the anticipation before a euphoric chorus.",
            "Design a glitchy and experimental intro for an electronic track.",
            "Generate a vocal chop sequence that adds a unique twist to the composition."
        ),
        "Jazz" to listOf(
            "Craft a smooth saxophone solo with improvisational flair, capturing the essence of a smoky jazz club.",
            "Compose a walking bass line that swings, becoming the heartbeat of a quintessential jazz ensemble.",
            "Arrange a complex drum rhythm with brush strokes and syncopated beats, embodying the spirit of bebop jazz.",
            "Write a piano riff that weaves through modal changes, paying homage to the legends of jazz improvisation.",
            "Develop a trumpet solo that tells a story with every note, reminiscent of the pioneers of cool jazz."
        ),
        "Country" to listOf(
            "Compose a guitar intro with a heartfelt twang, setting the scene for a tale of American heartlands.",
            "Write a chorus that carries the melody on the wings of country harmonies, echoing the expanse of rural landscapes.",
            "Craft a fiddle solo that's as lively as a barn dance under a moonlit sky, stirring the soul with rustic warmth.",
            "Develop a bass pattern that trots along like a horse on a dusty trail, anchoring the soul of country music.",
            "Structure a bridge that weaves a narrative of heartache and hope, leading to a climax that's as cathartic as a country ballad's resolution."
        ),
        "Sea Shanty" to listOf(
            "Compose an accordion-driven melody reminiscent of old sea shanties, evoking the spirit of sailors working in unison as they raise the sails and brave the waves.",
            "Write lyrics that tell the story of a daring voyage across the seven seas, filled with tales of adventure, hardship, and camaraderie among the crew.",
            "Craft a rousing chorus that captures the rhythm and cadence of traditional sea shanties, inviting listeners to join in the call-and-response chants of sailors at work.",
            "Develop a fiddle solo that weaves through the melody like a sailor's yarn, adding layers of emotion and depth to the tale of life on the open ocean.",
            "Structure a bridge that builds upon the themes of perseverance and resilience, leading to a triumphant climax that echoes the enduring spirit of seafaring folk."
        ),
        "Reggae" to listOf(
            "Compose a guitar strumming pattern that’s as relaxed and rhythmic as a beachside jam session in Jamaica.",
            "Craft a bass line that pulses like the heartbeat of reggae, deep and melodic, invoking the spirit of the island.",
            "Arrange a drum pattern that features the offbeat magic of reggae, capturing the genre's laid-back yet vibrant essence.",
            "Write a keyboard riff that brings sunshine to the melody, infusing the track with the uplifting vibes of reggae.",
            "Develop vocal lines that speak of unity and peace, resonating with the social consciousness at the heart of reggae."
        ),
        "Blues" to listOf(
            "Craft a guitar solo that weaves a tale of sorrow and redemption, embodying the soulful depths of the blues.",
            "Compose a drum rhythm that shuffles and grooves, laying down the gritty foundation of a blues juke joint.",
            "Develop a bass line that walks with the slow, deliberate steps of a soul carrying the blues.",
            "Write a harmonica piece that cries out with the raw emotion of life's trials, echoing the voices of blues legends.",
            "Create lyrics that narrate stories of struggle and resilience, set against the backdrop of the enduring twelve-bar blues progression."
        ),
        "Folk" to listOf(
            "Create a warm and inviting acoustic guitar intro that sets a storytelling atmosphere.",
            "Generate a melody that features vocal harmonies and evokes a sense of community.",
            "Provide a simple yet profound banjo riff that adds a rustic charm.",
            "Design a lyrical narrative that captures the essence of folk tales and personal journeys.",
            "Generate a cello line that adds depth and emotional resonance to the composition."
        ),
        "R&B" to listOf(
            "Create a smooth and sultry beat that sets the stage for heartfelt vocals.",
            "Generate a bass line that's both groove-heavy and subtle, supporting the vocal melody.",
            "Provide a keyboard arrangement that blends classic soul with modern R&B vibes.",
            "Design a vocal melody that showcases range and emotion, with intricate runs and harmonies.",
            "Generate a bridge that deepens the song's emotional impact, leading to a powerful climax."
        ),
        "Metal" to listOf(
            "Create an intense and fast-paced guitar riff that drives the song with aggression.",
            "Generate a drum pattern that features double bass pedal work for added heaviness.",
            "Provide a bass guitar line that matches the intensity of the guitars while maintaining clarity.",
            "Design a vocal line that ranges from powerful screams to melodic singing.",
            "Generate a breakdown that adds tension and release, culminating in a climactic return."
        ),
        "Indie" to listOf(
            "Create a quirky and catchy guitar riff that defines the song's unique character.",
            "Generate a drum beat that's both inventive and simplistic, supporting an indie vibe.",
            "Provide a bass line that's melodic and integral to the song's hook.",
            "Design a vocal melody that conveys authenticity and emotional honesty.",
            "Generate a synth or keyboard line that adds a layer of whimsy or melancholy."
        ),
        "World Music" to listOf(
            "Create a melody that incorporates traditional instruments from non-Western cultures.",
            "Generate a rhythm that blends different cultural beats into a cohesive global fusion.",
            "Provide an instrumental solo that showcases the unique timbre of a traditional instrument.",
            "Design a song structure that reflects the musical traditions of a specific culture.",
            "Generate vocals that either adhere to traditional styles or combine various cultural influences."
        ),
        "Accordion" to listOf(
            "Compose an accordion-driven melody reminiscent of old sea shanties, evoking the spirit of sailors working in unison as they raise the sails and brave the waves.",
            "Craft a lively accordion riff that captures the rollicking energy of a sea voyage, infusing the melody with the spirit of adventure and camaraderie."
        ),
        "Fiddle" to listOf(
            "Develop a fiddle solo that weaves through the melody like a sailor's yarn, adding layers of emotion and depth to the tale of life on the open ocean.",
            "Write a fiddle melody that dances atop the waves, echoing the joy and sorrow of sailors' tales told around the ship's deck under a starry sky."
        ),
        "Guitar" to listOf(
            "Compose a guitar intro with a heartfelt twang, setting the scene for a tale of high seas and salty air, where every chord resonates with the spirit of maritime adventure.",
            "Craft a guitar accompaniment that strums like the rhythmic pulse of ocean waves, carrying the melody forward with the steady determination of a sailor at the helm."
        ),
        "Banjo" to listOf(
            "Write a banjo riff that conjures images of sailors gathered 'round a flickering lantern, regaling one another with tales of distant lands and perilous journeys.",
            "Develop a lively banjo melody that gallops across the song like a swift ship cutting through the waves, infusing the music with the infectious energy of a sea shanty."
        ),
        "Mandolin" to listOf(
            "Craft a mandolin interlude that sails through the melody like a seagull skimming the ocean's surface, adding a touch of whimsy and charm to the song's maritime theme.",
            "Compose a haunting mandolin melody that echoes across the sea, evoking the longing and nostalgia of sailors yearning for home as they chart their course through uncharted waters."
        ),
        "Concertina" to listOf(
            "Develop a concertina riff that whirls and twirls like a gust of wind catching the sails, infusing the song with the spirited energy of a crew working in harmony.",
            "Write a concertina accompaniment that dances lightly across the melody, conjuring visions of sailors' feet tapping to the rhythm as they work together to navigate the treacherous sea."
        ),
        "Ambient" to listOf(
            "Create a soundscape that evokes a sense of space and serenity, using layered synthesizers.",
            "Generate a slow-evolving melody that emphasizes texture over rhythm.",
            "Provide a minimalist piano pattern that adds a touch of melody within the ambient wash.",
            "Design an atmospheric buildup that uses field recordings and natural sounds.",
            "Generate a composition that focuses on tranquility and the subtle interplay of ambient noises."
        )
    )
    var promptText by remember { mutableStateOf(TextFieldValue("")) }
    val isLoading by viewModel.isLoading.collectAsState()
    val models = listOf("musicgen-small", "musicgen-medium", "musicgen-melody")
    var selectedModel by remember { mutableStateOf(models.first()) }
    var selectedDuration by remember { mutableIntStateOf(listOf(5, 8 , 12, 15, 20, 25, 30).first()) }

    Surface(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        LazyColumn(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
            item {
                Text("Select a Genre", style = MaterialTheme.typography.headlineMedium)
            }
            item {
                GenreSelector(genres.keys.toList()) { genre ->
                    selectedGenre = genre
                }
            }
            if (selectedGenre != null) {
                item {
                    PromptsSelector(genres[selectedGenre] ?: listOf()) { selectedPrompt ->
                        promptText = TextFieldValue(selectedPrompt)
                    }
                }
            }
            item {
                MusicPromptInputField(promptText, onValueChange = { promptText = it })
            }
            item {
                DurationSelectionButton(selectedDuration) { selectedDuration = it; viewModel.setDuration(it.toFloat()) }
            }
            item {
                ModelSelectionButton(models, selectedModel) { selectedModel = it; viewModel.setSelectedModel("facebook/$it") }
            }
            item {
                GenerateMusicButton(isLoading) { viewModel.generateMusic(promptText.text, context) }
            }
            item {
                DownloadableView(viewModel = viewModel, context = context)
            }
        }
    }

}
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(UnstableApi::class)
@Composable
fun DownloadableView(viewModel: MusicPromptViewModel, context: Context) {
    val downloadableContent by viewModel.downloadableContents.collectAsState()
    val selectedItemsOrder by viewModel.selectedItemsOrder.collectAsState()

    LaunchedEffect(true) {
        viewModel.loadPersistedDownloads(context)
    }

    Column(modifier = Modifier.padding(12.dp)) {
        Button(onClick = {
            // Perform action on selectedItems
            // For example, viewModel.performActionOnSelectedItems()
        }) {
            Text("Perform Action on Selected Items")
        }

        Row {
            // Title Column Header
            Text(
                text = "Music Title",
                modifier = Modifier.weight(2f).align(Alignment.CenterVertically),
                style = MaterialTheme.typography.labelSmall
            )
            // Duration Column Header
            Text(
                text = "Duration",
                modifier = Modifier.weight(1f).align(Alignment.CenterVertically),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center
            )
            // Empty space for the button column
            Spacer(modifier = Modifier.weight(1f))
        }
        downloadableContent.forEach { content ->
            val index = selectedItemsOrder.indexOf(content.title) + 1 // Add 1 because index is 0-based
            val isSelected = index > 0
            val backgroundColor = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.background
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(backgroundColor)
                    .clickable { viewModel.toggleItemSelection(content.title) }
            ) {
                if (isSelected) {
                    // Show index or badge for selected items
                    Text(
                        text = "$index",
                        modifier = Modifier.padding(end = 8.dp),
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
                Text(
                    text = content.title,
                    modifier = Modifier.weight(2f),
                    style = MaterialTheme.typography.bodySmall
                )
                // Second column: Duration (without label, directly under the "Duration" header)
                Text(
                    text = formatDuration(content.duration),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
                // Third column: Action Button
                Box(modifier = Modifier.weight(1f)) {

                    when (content.status.value) {
                        MusicPromptViewModel.DownloadStatus.NOT_DOWNLOADED,
                        MusicPromptViewModel.DownloadStatus.DOWNLOADING -> {
                            DownloadButton(content, viewModel, context)
                        }
                        MusicPromptViewModel.DownloadStatus.DOWNLOADED -> {
                            PlayButton(content, viewModel, context)
                        }
                    }
                }
            }
        }
    }
}
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(UnstableApi::class) @Composable
fun DownloadButton(
    content: MusicPromptViewModel.DownloadableContent,
    viewModel: MusicPromptViewModel,
    context: Context
) {
    Button(
        onClick = {
            viewModel.downloadFile(context, content.downloadUrl, "${content.title}.wav", content)
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(if (content.status.value == MusicPromptViewModel.DownloadStatus.DOWNLOADING) "Downloading" else "Download")
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(UnstableApi::class) @Composable
fun PlayButton(
    content: MusicPromptViewModel.DownloadableContent,
    viewModel: MusicPromptViewModel,
    context: Context
) {
    Button(
        onClick = {
            content.filePath?.let { filePath ->
                viewModel.playAudioFromUri(context, Uri.parse(filePath))
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Play")
    }
}
// Utility function to format duration
fun formatDuration(durationMillis: Long): String {
    // Implement formatting based on whether you're using milliseconds or seconds
    // This is an example assuming milliseconds
    val seconds = (durationMillis / 1000) % 60
    val minutes = (durationMillis / (1000 * 60)) % 60
    return String.format("%02d:%02d", minutes, seconds)
}

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPromptInputField(value: TextFieldValue, onValueChange: (TextFieldValue) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Music Prompt") },
        singleLine = false,
        textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 18.sp), // Larger font size
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp), // Increase padding for a taller appearance
        colors = TextFieldDefaults.outlinedTextFieldColors(
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = ContentAlpha.high), // Brighter color when focused
            focusedLabelColor = MaterialTheme.colorScheme.primary.copy(alpha = ContentAlpha.high) // Label color when focused
        ),
        shape = RoundedCornerShape(16.dp) // More rounded corners for a modern look
    )
}


@Composable
fun DurationSelectionButton(selectedDuration: Int, onSelect: (Int) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    // Button that shows current selection and opens the dialog on click
    OutlinedButton(onClick = { showDialog = true }, modifier = Modifier.fillMaxWidth()) {
        Text("Duration: $selectedDuration seconds", style = MaterialTheme.typography.bodyLarge)
    }

    // Dialog for selecting duration
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select Duration") },
            text = {
                // Replace with your list of durations
                val durations = listOf(5, 10, 15, 20, 25, 30)
                Column {
                    durations.forEach { duration ->
                        TextButton(onClick = {
                            onSelect(duration)
                            showDialog = false
                        }) {
                            Text("$duration seconds")
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
fun GenerateMusicButton(isLoading: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth(), enabled = !isLoading) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        } else {
            Text("Generate Music")
        }
    }
}


@Composable
fun PromptsSelector(prompts: List<String>, onPromptSelected: (String) -> Unit) {
    // Horizontal carousel for prompts
    LazyRow(modifier = Modifier.padding(vertical = 8.dp)) {
        items(prompts) { prompt ->
            PromptCard(prompt = prompt, onPromptSelected = onPromptSelected)
        }
    }
}

@Composable
fun PromptCard(prompt: String, onPromptSelected: (String) -> Unit) {
    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { onPromptSelected(prompt) },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Elevation for shadow
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(prompt, style = MaterialTheme.typography.bodyMedium)
            // You can add more details or an image here
        }
    }
}

@Composable
fun GenreSelector(genres: List<String>, onGenreSelected: (String) -> Unit) {
    // Horizontal carousel for genres
    LazyRow(modifier = Modifier.padding(vertical = 8.dp)) {
        items(genres) { genre ->
            GenreCard(genre = genre, onGenreSelected = onGenreSelected)
        }
    }
}
@Composable
fun GenreCard(genre: String, onGenreSelected: (String) -> Unit) {
    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { onGenreSelected(genre) },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(genre, style = MaterialTheme.typography.bodyMedium)
            // Additional details or an image can be added here
        }
    }
}