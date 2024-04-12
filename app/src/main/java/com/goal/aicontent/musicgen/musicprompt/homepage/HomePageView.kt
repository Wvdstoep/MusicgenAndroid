package com.goal.aicontent.musicgen.musicprompt.homepage

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.goal.aicontent.HomePageViewModel
import com.goal.aicontent.R
import com.goal.aicontent.drawer.DrawerContent
import com.goal.aicontent.functions.TaskItem
import com.goal.aicontent.musicgen.musicprompt.CarouselBox
import com.goal.aicontent.musicgen.musicprompt.MediaCarousel
import com.goal.aicontent.navigation.BottomSheetContent

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun HomePageViewWrapper(viewModel: HomePageViewModel, navController: NavHostController) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
    )

    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetContent = { BottomSheetContent(navController, bottomSheetScaffoldState) }, // Define your bottom sheet content here
        sheetPeekHeight = 30.dp,
        topBar = {
        }
    ) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            scrimColor = Color.Transparent,
            drawerContent = {
                if (drawerState.isOpen) {
                    DrawerContent(navController, drawerState) // Define your drawer content here
                }
            }
        ) {
            Scaffold { paddingValues ->
                HomePageView(modifier = Modifier.padding(paddingValues))
            }
        }
    }
}
@Composable
fun HomePageView(modifier: Modifier = Modifier) {
    val homePageViewModel: HomePageViewModel = viewModel()
    val tasks by homePageViewModel.items.collectAsState()
    val categories by homePageViewModel.categories.collectAsState() // Ensure this state exists in your ViewModel
    val context = LocalContext.current
    val searchQuery = remember { mutableStateOf("") }
    val searchResults = remember { mutableStateOf(listOf<TaskItem>()) }
    val showCategoryView = remember { mutableStateOf(false) }
    val currentCategory = remember { mutableStateOf("") }



    if (showCategoryView.value) {
        CategoryItemsView(categoryName = currentCategory.value, homePageViewModel = homePageViewModel, onBackClicked = { showCategoryView.value = false } )
    } else {
        LazyColumn(contentPadding = PaddingValues()) {
            item {
                CustomSearchBar(
                    searchQuery = searchQuery,
                    onQueryChanged = { query ->
                        searchQuery.value = query
                        searchResults.value = homePageViewModel.searchItems(query)
                    }
                )
            }

            // Displaying search results or featured items
            if (searchQuery.value.isNotEmpty()) {
                items(searchResults.value) { taskItem ->
                    SearchResultItem(
                        taskItem = taskItem,
                        onPlayClick = { url -> homePageViewModel.playAudioFromUrl(context, url) },
                        onFavoriteToggle = { itemId -> // Accepts the ID as a String
                            homePageViewModel.toggleFavorite(context, itemId)
                        }
                    )
                }
            } else {
                item {
                    Text(
                        text = "Featured Items",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        color = Color.Black,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineMedium.copy(letterSpacing = 0.15.sp)
                    )

                    MediaCarousel(
                        list = tasks,
                        onItemClicked = { taskItem ->
                            taskItem.downloadUrl?.let { homePageViewModel.playAudioFromUrl(context, it) }
                        }
                    )
                }
                // Correct use of item for CategoriesGrid
                item {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)) {
                        CategoriesGrid(
                            categories = categories,
                            onCategoryClick = { categoryName ->
                                currentCategory.value = categoryName
                                homePageViewModel.fetchItemsForCategory(categoryName)
                                showCategoryView.value = true // Switch view to show category items
                            }
                        )
                    }
                }
                items(tasks) { taskItem ->
                    CarouselBox(
                        item = taskItem,
                        onPlayClick = { item ->
                            item.downloadUrl?.let { url -> homePageViewModel.playAudioFromUrl(context, url) }
                        }
                    )
                }
            }
        }
    }
}
@Composable
fun CategoryItemsView(categoryName: String, homePageViewModel: HomePageViewModel, onBackClicked: () -> Unit) {
    val items by homePageViewModel.items.collectAsState()
    val context = LocalContext.current
Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClicked) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Go back")
            }
            Spacer(modifier = Modifier.width(8.dp)) // Add some space between the icon and text
            Text(
                text = "Items in $categoryName",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )

        }
        LazyColumn {
            items(items) { item ->
                // Add padding around the CarouselBox for spacing
                CarouselBox(
                    item = item,
                    onPlayClick = {
                        item.downloadUrl?.let { url -> homePageViewModel.playAudioFromUrl(context, url) }
                    }
                )
                // Add a divider after each item except the last one
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), thickness = 1.dp)
            }
        }
    }
}
@Composable
fun CategoriesGrid(categories: List<HomePageViewModel.InstrumentCategory>, onCategoryClick: (String) -> Unit) {
    LazyHorizontalGrid(rows = GridCells.Fixed(2), contentPadding = PaddingValues(16.dp)) {
        items(categories) { category ->
            CategoryCard(category, onCategoryClick)
        }
    }
}


@Composable
fun CategoryCard(category: HomePageViewModel.InstrumentCategory, onClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .clickable { onClick(category.name) }
            .width(200.dp)
            .height(200.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box {
            // Image as the background
            Image(
                painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(category.imageUrl)
                        .crossfade(true)
                        .placeholder(R.drawable.ic_crown)
                        .error(R.drawable.ic_crown)
                        .build(),
                    placeholder = painterResource(R.drawable.ic_crown),
                    error = painterResource(R.drawable.ic_crown)
                ),
                contentDescription = "Category Image",
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Gradient Shadow Box at the bottom of the image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .align(Alignment.BottomStart)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            ) {
                // Text overlay
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.White, fontSize = 16.sp),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(horizontal = 16.dp)
                )
            }
        }
    }
}