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
    val tabIndex = remember(installedExtensions, selectedSourceId) {
        val idx = if (selectedSourceId == null) 0 else installedExtensions.indexOfFirst { it.id == selectedSourceId } + 1
        if (idx in 0..installedExtensions.size) idx else 0
    }

    var selectedIndexState by remember { mutableStateOf(0) }
    LaunchedEffect(tabIndex, installedExtensions.size) {
        if (tabIndex < 1 + installedExtensions.size) {
            selectedIndexState = tabIndex
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Source Quick Filters
        ScrollableTabRow(
            selectedTabIndex = selectedIndexState.coerceIn(0, installedExtensions.size),
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
    var extensionSearchQuery by remember { mutableStateOf("") }
    
    val filteredExtensions = remember(extensionsList, extensionSearchQuery) {
        if (extensionSearchQuery.isBlank()) extensionsList
        else extensionsList.filter { it.name.contains(extensionSearchQuery, ignoreCase = true) }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            var repoUrl by remember { mutableStateOf("https://raw.githubusercontent.com/keiyoushi/extensions/repo/index.min.json") }
            val isSyncing by viewModel.isSyncingRepo.collectAsState()
            val syncMessage by viewModel.repoSyncMessage.collectAsState()

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "External Extension Repository",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Paste a Tachiyomi/Mihon compatible index.min.json repository link to load hundreds of scraper sources dynamically.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = repoUrl,
                        onValueChange = { repoUrl = it },
                        label = { Text("Repository JSON URL") },
                        enabled = !isSyncing,
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("repo_url_input")
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSyncing) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Syncing...", style = MaterialTheme.typography.bodySmall)
                            }
                        } else {
                            Row {
                                SuggestionChip(
                                    onClick = { repoUrl = "https://raw.githubusercontent.com/keiyoushi/extensions/repo/index.min.json" },
                                    label = { Text("Keiyoushi (Default)") }
                                )
                            }
                        }
                        
                        Button(
                            onClick = { viewModel.syncExtensionRepository(repoUrl) },
                            enabled = !isSyncing && repoUrl.isNotBlank(),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("sync_repo_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sync")
                        }
                    }

                    syncMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { viewModel.clearRepoSyncMessage() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = extensionSearchQuery,
                onValueChange = { extensionSearchQuery = it },
                placeholder = { Text("Filter extensions by name...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (extensionSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { extensionSearchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("extension_store_search_input")
            )
        }

        item {
            val titleText = if (extensionSearchQuery.isNotBlank()) {
                "Found ${filteredExtensions.size} Matches"
            } else {
                "Available Source Extensions (${filteredExtensions.size})"
            }
            Text(
                text = titleText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        items(filteredExtensions, key = { it.id }) { ext ->
            ListItem(
                headlineContent = {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            ext.name, 
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        SuggestionChip(
                            onClick = {},
                            label = { Text("v${ext.version}", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.height(20.dp)
                        )
                    }
                },
                supportingContent = {
                    Text(if (ext.isInstalled) "Installed & Ready" else "Available from central repository")
                },
                leadingContent = {
                    Surface(
                        color = when {
                            ext.id == "mangadex" -> Color(0xFFE67E22)
                            ext.id == "webtoons" -> Color(0xFF2ECC71)
                            ext.id == "mangakakalot" -> Color(0xFF3498DB)
                            ext.id == "comick" -> Color(0xFF9B59B6)
                            ext.name.lowercase().contains("18+") -> Color(0xFFE74C3C)
                            else -> Color(0xFF7F8C8D)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = ext.name.replace("Tachiyomi: ", "").take(1).uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                },
                trailingContent = {
                    if (ext.isInstalled) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
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
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
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
