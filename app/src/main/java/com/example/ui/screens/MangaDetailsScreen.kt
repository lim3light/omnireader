package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
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
import com.example.domain.Chapter
import com.example.domain.Manga
import com.example.data.database.DownloadQueueEntity
import com.example.ui.MangaViewModel
import com.example.ui.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangaDetailsScreen(
    viewModel: MangaViewModel,
    mangaId: String,
    sourceId: String,
    modifier: Modifier = Modifier
) {
    val favoriteManga by viewModel.favoriteManga.collectAsState()
    val isFavorite = favoriteManga.any { it.id == mangaId }
    val matchedFavoriteManga = favoriteManga.find { it.id == mangaId }
    
    val selectedMangaDetails by viewModel.selectedMangaDetails.collectAsState()
    val chapters by viewModel.mangaChapters.collectAsState()
    val isLoadingDetails by viewModel.isLoadingDetails.collectAsState()
    val downloadQueue by viewModel.downloadQueue.collectAsState()

    val malUsername by viewModel.myAnimeListUsername.collectAsState()
    val aniListUsername by viewModel.aniListUsername.collectAsState()

    var showTrackingDialogForMAL by remember { mutableStateOf(false) }
    var showTrackingDialogForAniList by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.Library) },
                        modifier = Modifier.testTag("details_back")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleFavoriteManga(mangaId, isFavorite) },
                        modifier = Modifier.testTag("details_favorite_toggle")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite Toggle",
                            tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        containerColor = Color.Transparent,
        modifier = modifier
    ) { innerPadding ->
        if (isLoadingDetails || selectedMangaDetails == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Fetching manga schema & dynamic chapters...")
                }
            }
        } else {
            val details = selectedMangaDetails!!
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("details_scroller"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Headline Panel: Cover and description
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AsyncImage(
                            model = details.coverUrl,
                            contentDescription = details.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(120.dp)
                                .aspectRatio(0.7f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = details.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Author: ${details.author}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(details.status) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("Source ID: ${details.sourceId}") }
                                )
                            }
                        }
                    }
                }

                // Description box
                item {
                    Text(
                        text = "Synopsis",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = details.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Genres Panel
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        details.genre.forEach { tag ->
                            AssistChip(
                                onClick = {},
                                label = { Text(tag) }
                            )
                        }
                    }
                }

                // Category management ribbon (if added in library)
                if (isFavorite) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "In Library Category",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = matchedFavoriteManga?.category ?: "Default",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Icon(imageVector = Icons.Default.Category, contentDescription = null)
                            }
                        }
                    }
                }

                // Tracking Panel Section
                item {
                    MangaTrackersPanel(
                        mangaId = mangaId,
                        malUsername = malUsername,
                        aniListUsername = aniListUsername,
                        trackerMALStatus = matchedFavoriteManga?.trackerMALStatus,
                        trackerAniListStatus = matchedFavoriteManga?.trackerAniListStatus,
                        onOpenMAL = { showTrackingDialogForMAL = true },
                        onOpenAniList = { showTrackingDialogForAniList = true }
                    )
                }

                // Chapters header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Chapters Available (${chapters.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(imageVector = Icons.Default.Sort, contentDescription = "Sort")
                    }
                }

                // Chapter List Items
                items(chapters, key = { it.id }) { chapter ->
                    val queueInfo = downloadQueue.find { it.chapterId == chapter.id }
                    ChapterRowItem(
                        chapter = chapter,
                        queueInfo = queueInfo,
                        onReadToggle = { viewModel.toggleChapterRead(chapter.id, chapter.isRead) },
                        onDownloadClick = { viewModel.downloadChapter(chapter.id, mangaId) },
                        onDeleteDownload = { viewModel.deleteDownloadedChapter(chapter.id) },
                        onChapterClick = { viewModel.openChapterInReader(chapter, sourceId) }
                    )
                }
            }
        }
    }

    // MAL Tracker choice popups
    if (showTrackingDialogForMAL) {
        TrackingEditPopup(
            trackerName = "MAL",
            isLoggedIn = malUsername != null,
            username = malUsername ?: "",
            currentStatus = matchedFavoriteManga?.trackerMALStatus,
            onLogin = { user -> viewModel.loginTracker("MAL", user) },
            onLogout = { viewModel.logoutTracker("MAL") },
            onSaveStatus = { stat -> viewModel.setMangaTrackerStatus(mangaId, "MAL", stat) },
            onDismiss = { showTrackingDialogForMAL = false }
        )
    }

    // AniList Tracker choice popups
    if (showTrackingDialogForAniList) {
        TrackingEditPopup(
            trackerName = "AniList",
            isLoggedIn = aniListUsername != null,
            username = aniListUsername ?: "",
            currentStatus = matchedFavoriteManga?.trackerAniListStatus,
            onLogin = { user -> viewModel.loginTracker("AniList", user) },
            onLogout = { viewModel.logoutTracker("AniList") },
            onSaveStatus = { stat -> viewModel.setMangaTrackerStatus(mangaId, "AniList", stat) },
            onDismiss = { showTrackingDialogForAniList = false }
        )
    }
}

@Composable
fun MangaTrackersPanel(
    mangaId: String,
    malUsername: String?,
    aniListUsername: String?,
    trackerMALStatus: String?,
    trackerAniListStatus: String?,
    onOpenMAL: () -> Unit,
    onOpenAniList: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Tracking Accounts Synchronizer",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // MAL Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenMAL() },
                    colors = CardDefaults.cardColors(
                        containerColor = if (malUsername != null) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.TrendingUp, contentDescription = "MAL")
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "MyAnimeList",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (malUsername != null) "Status: ${trackerMALStatus ?: "Planning"}" else "Tap to Connect",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // AniList Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenAniList() },
                    colors = CardDefaults.cardColors(
                        containerColor = if (aniListUsername != null) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.Analytics, contentDescription = "AniList")
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "AniList",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (aniListUsername != null) "Status: ${trackerAniListStatus ?: "Planning"}" else "Tap to Connect",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChapterRowItem(
    chapter: Chapter,
    queueInfo: DownloadQueueEntity?,
    onReadToggle: () -> Unit,
    onDownloadClick: () -> Unit,
    onDeleteDownload: () -> Unit,
    onChapterClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChapterClick() }
            .testTag("chapter_row_${chapter.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (chapter.isRead) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Read/Unread Icon and name
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onReadToggle,
                        modifier = Modifier.testTag("chapter_read_toggle_${chapter.id}")
                    ) {
                        Icon(
                            imageVector = if (chapter.isRead) Icons.Default.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                            contentDescription = "Read Status Indicator",
                            tint = if (chapter.isRead) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = chapter.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (chapter.isRead) FontWeight.Normal else FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Uploaded: ${chapter.dateUpload}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Download status panel trigger
                Box {
                    if (chapter.isDownloaded) {
                        IconButton(
                            onClick = onDeleteDownload,
                            modifier = Modifier.testTag("download_delete_${chapter.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.TaskAlt,
                                contentDescription = "Chapter Downloaded",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else if (queueInfo != null) {
                        // Busy queuing / downloading indicator
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = { queueInfo.progress / 100f },
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 3.dp
                            )
                            Text(
                                text = "${queueInfo.progress}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        IconButton(
                            onClick = onDownloadClick,
                            modifier = Modifier.testTag("download_chapter_${chapter.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Download,
                                contentDescription = "Download Chapter Offline"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrackingEditPopup(
    trackerName: String,
    isLoggedIn: Boolean,
    username: String,
    currentStatus: String?,
    onLogin: (String) -> Unit,
    onLogout: () -> Unit,
    onSaveStatus: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var inputUser by remember { mutableStateOf("") }
    val statuses = listOf("Reading", "Completed", "On Hold", "Dropped", "Plan to Read")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("$trackerName Integrator") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (!isLoggedIn) {
                    Text(
                        "Connect your external $trackerName profile to post reading chapters, mark progress, and sync favorites automatically.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    OutlinedTextField(
                        value = inputUser,
                        onValueChange = { inputUser = it },
                        label = { Text("Account Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        "Logged in as: $username",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        "Select Comic/Novel Progress status to sync:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    statuses.forEach { stat ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onSaveStatus(stat)
                                    onDismiss()
                                }
                                .padding(10.dp)
                        ) {
                            RadioButton(
                                selected = currentStatus == stat,
                                onClick = {
                                    onSaveStatus(stat)
                                    onDismiss()
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stat, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(
                        onClick = {
                            onLogout()
                            onDismiss()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Disconnect Account Link")
                    }
                }
            }
        },
        confirmButton = {
            if (!isLoggedIn) {
                Button(
                    onClick = {
                        if (inputUser.isNotBlank()) {
                            onLogin(inputUser)
                        }
                    },
                    enabled = inputUser.isNotBlank()
                ) {
                    Text("Authenticate")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
