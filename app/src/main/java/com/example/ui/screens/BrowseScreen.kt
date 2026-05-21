package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.database.ExtensionEntity
import com.example.ui.MangaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen(
    viewModel: MangaViewModel,
    modifier: Modifier = Modifier
) {
    val extensionsList by viewModel.extensionsList.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val selectedSourceId by viewModel.selectedSourceIdForBrowse.collectAsState()

    var activeBrowseTab by remember { mutableStateOf(BrowseTab.Sources) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "Browse Sources",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )

                // Sub-tabs: Sources Catalogs vs Extension Installer Manager
                TabRow(
                    selectedTabIndex = activeBrowseTab.ordinal,
                    containerColor = Color.Transparent,
                    divider = {}
                ) {
                    Tab(
                        selected = activeBrowseTab == BrowseTab.Sources,
                        onClick = { activeBrowseTab = BrowseTab.Sources },
                        text = { Text("Manga Sources", fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.testTag("browse_tab_sources")
                    )
                    Tab(
                        selected = activeBrowseTab == BrowseTab.Extensions,
                        onClick = { activeBrowseTab = BrowseTab.Extensions },
                        text = { Text("Extension Store", fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.testTag("browse_tab_extensions")
                    )
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
            when (activeBrowseTab) {
                BrowseTab.Sources -> {
                    SourcesCatalogLayout(
                        viewModel = viewModel,
                        extensionsList = extensionsList,
                        selectedSourceId = selectedSourceId,
                        searchQuery = searchQuery,
                        searchResults = searchResults,
                        isSearching = isSearching
                    )
                }
                BrowseTab.Extensions -> {
                    ExtensionsInstallerLayout(
                        viewModel = viewModel,
                        extensionsList = extensionsList
                    )
                }
            }
        }
    }
}

enum class BrowseTab {
    Sources,
    Extensions
}

@Composable
fun SourcesCatalogLayout(
    viewModel: MangaViewModel,
    extensionsList: List<ExtensionEntity>,
    selectedSourceId: String?,
    searchQuery: String,
    searchResults: List<com.example.domain.Manga>,
    isSearching: Boolean
) {
    val installedExtensions = extensionsList.filter { it.isInstalled }

    Column(modifier = Modifier.fillMaxSize()) {
        // Source Quick Filters
        ScrollableTabRow(
            selectedTabIndex = if (selectedSourceId == null) 0 else installedExtensions.indexOfFirst { it.id == selectedSourceId } + 1,
            edgePadding = 16.dp,
            divider = {},
            containerColor = Color.Transparent,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Tab(
                selected = selectedSourceId == null,
                onClick = { viewModel.selectSourceForBrowse(null) },
                text = { Text("Global (ALL)") },
                modifier = Modifier.testTag("source_filter_global")
            )
            installedExtensions.forEach { ext ->
                Tab(
                    selected = selectedSourceId == ext.id,
                    onClick = { viewModel.selectSourceForBrowse(ext.id) },
                    text = { Text(ext.name.substringBefore(" ")) },
                    modifier = Modifier.testTag("source_filter_${ext.id}")
                )
            }
        }

        // Search Input field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text("Search title, author, or publisher...") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("global_search_input")
        )

        if (isSearching) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (searchResults.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CloudQueue,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No search results match",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Verify that at least one source extension is installed & turned on in the Extension Store, then type your keyword.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(searchResults, key = { it.id }) { manga ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectManga(manga) }
                            .testTag("search_result_card_${manga.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(0.72f)
                            ) {
                                AsyncImage(
                                    model = manga.coverUrl,
                                    contentDescription = manga.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                // Source Name Ribbon
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = manga.sourceName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = manga.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "By ${manga.author}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtensionsInstallerLayout(
    viewModel: MangaViewModel,
    extensionsList: List<ExtensionEntity>
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Available Source Extensions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        items(extensionsList, key = { it.id }) { ext ->
            ListItem(
                headlineContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(ext.name, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        SuggestionChip(
                            onClick = {},
                            label = { Text("v${ext.version}") },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                },
                supportingContent = {
                    Text(if (ext.isInstalled) "Installed & Ready" else "Available from central community repository")
                },
                leadingContent = {
                    Surface(
                        color = when (ext.id) {
                            "mangadex" -> Color(0xFFE67E22)
                            "webtoons" -> Color(0xFF2ECC71)
                            "mangakakalot" -> Color(0xFF3498DB)
                            "comick" -> Color(0xFF9B59B6)
                            else -> Color(0xFF95A5A6)
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when (ext.id) {
                                    "mangadex" -> Icons.Default.Dashboard
                                    "webtoons" -> Icons.Default.Smartphone
                                    "mangakakalot" -> Icons.Default.Book
                                    "comick" -> Icons.Default.DynamicFeed
                                    else -> Icons.Default.FolderOpen
                                },
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                },
                trailingContent = {
                    if (ext.isInstalled) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Active toggle switch
                            Switch(
                                checked = ext.isActive,
                                onCheckedChange = { viewModel.toggleExtensionActive(ext.id, it) },
                                modifier = Modifier
                                    .scale(0.85f)
                                    .testTag("extension_active_switch_${ext.id}")
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { viewModel.installExtension(ext.id, false) },
                                modifier = Modifier.testTag("extension_delete_btn_${ext.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Uninstall",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = { viewModel.installExtension(ext.id, true) },
                            modifier = Modifier.testTag("extension_install_btn_${ext.id}"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Get")
                            }
                        }
                    }
                },
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("extension_item_${ext.id}")
            )
        }
    }
}
