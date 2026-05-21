package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.domain.Manga
import com.example.ui.MangaViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    viewModel: MangaViewModel,
    modifier: Modifier = Modifier
) {
    val favoriteManga by viewModel.favoriteManga.collectAsState()
    val categories by viewModel.libraryCategories.collectAsState()
    val selectedCategory by viewModel.selectedCategoryTab.collectAsState()
    
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    
    var mangaToMoveCategory by remember { mutableStateOf<Manga?>(null) }
    var showCategorySelectorDialog by remember { mutableStateOf(false) }

    val filteredManga = favoriteManga.filter { it.category == selectedCategory }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "My Library",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    actions = {
                        IconButton(
                            onClick = { showAddCategoryDialog = true },
                            modifier = Modifier.testTag("add_category_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Category")
                        }
                    }
                )
                
                // Categories Tab Bar
                ScrollableTabRow(
                    selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
                    edgePadding = 16.dp,
                    divider = {},
                    containerColor = Color.Transparent
                ) {
                    categories.forEach { category ->
                        Tab(
                            selected = selectedCategory == category,
                            onClick = { viewModel.setCategoryTab(category) },
                            text = { 
                                Text(
                                    text = category,
                                    fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal
                                ) 
                            },
                            modifier = Modifier.testTag("category_tab_$category")
                        )
                    }
                }
            }
        },
        containerColor = Color.Transparent,
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (filteredManga.isEmpty()) {
                // Empty state helper
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No titles in this category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Browse active source extensions to add your favorite manga here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 110.dp),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredManga, key = { it.id }) { manga ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.68f)
                                .clip(RoundedCornerShape(12.dp))
                                .combinedClickable(
                                    onClick = { viewModel.selectManga(manga) },
                                    onLongClick = {
                                        mangaToMoveCategory = manga
                                        showCategorySelectorDialog = true
                                    }
                                )
                                .testTag("library_manga_card_${manga.id}"),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            )
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = manga.coverUrl,
                                    contentDescription = manga.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                
                                // Source & Title Gradient overlay at the bottom
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.BottomCenter)
                                        .background(
                                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                                            )
                                        )
                                        .padding(8.dp)
                                ) {
                                    Column {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = manga.sourceName.substringBefore(" "),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = manga.title,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Add Category
    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Create New Category") },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("Category Name") },
                    singleLine = true,
                    modifier = Modifier.testTag("add_category_text_field")
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.addCategory(newCategoryName)
                        newCategoryName = ""
                        showAddCategoryDialog = false
                    },
                    modifier = Modifier.testTag("add_category_confirm")
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: Category Selector for Manga
    if (showCategorySelectorDialog && mangaToMoveCategory != null) {
        val selectedMangaToMove = mangaToMoveCategory!!
        AlertDialog(
            onDismissRequest = { showCategorySelectorDialog = false },
            title = { Text("Move Category") },
            text = {
                Column {
                    Text(
                        text = "Move \"${selectedMangaToMove.title}\" to:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    categories.forEach { cat ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .combinedClickable(
                                    onClick = {
                                        viewModel.changeMangaCategory(selectedMangaToMove.id, cat)
                                        showCategorySelectorDialog = false
                                        mangaToMoveCategory = null
                                    }
                                )
                                .padding(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                tint = if (selectedMangaToMove.category == cat) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (selectedMangaToMove.category == cat) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            viewModel.toggleFavoriteManga(selectedMangaToMove.id, true)
                            showCategorySelectorDialog = false
                            mangaToMoveCategory = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Remove from Library")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCategorySelectorDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
