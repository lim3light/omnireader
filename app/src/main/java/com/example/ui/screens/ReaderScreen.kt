package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.domain.Page
import com.example.ui.MangaViewModel
import com.example.ui.ReaderMode
import com.example.ui.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    viewModel: MangaViewModel,
    chapterId: String,
    sourceId: String,
    modifier: Modifier = Modifier
) {
    val activeChapter by viewModel.activeChapter.collectAsState()
    val pages by viewModel.readerPages.collectAsState()
    val readerMode by viewModel.readerMode.collectAsState()
    val currentPageIndex by viewModel.currentPageIndexInReader.collectAsState()

    var showMenuOverlays by remember { mutableStateOf(true) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // IMMERSIVE AMOLED DARK
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                showMenuOverlays = !showMenuOverlays
            }
            .testTag("reader_viewport")
    ) {
        if (pages.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Loading chapter pages safely...", color = Color.White)
                }
            }
        } else {
            // Main Reader Content based on Mode
            when (readerMode) {
                ReaderMode.VerticalWebtoon -> {
                    // LazyColumn scrolling top-down infinitely
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 120.dp)
                    ) {
                        itemsIndexed(pages, key = { idx, p -> "${p.index}_$idx" }) { index, page ->
                            LaunchedEffect(key1 = index) {
                                viewModel.updateReaderPageSelected(index)
                            }
                            AsyncImage(
                                model = page.imageUrl,
                                contentDescription = "Page ${index + 1}",
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                            )
                        }
                        item {
                            FinishedChapterCard(onBack = { 
                                val mId = activeChapter?.mangaId ?: ""
                                viewModel.navigateTo(Screen.MangaDetails(mId, sourceId))
                            })
                        }
                    }
                }
                ReaderMode.HorizontalHorizontal, ReaderMode.RightToLeft -> {
                    // Sliding navigation
                    val isRTL = readerMode == ReaderMode.RightToLeft
                    val displayedIndex = if (isRTL) (pages.size - 1 - currentPageIndex).coerceIn(0, pages.size - 1) else currentPageIndex
                    val activePage = pages[displayedIndex]

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            AsyncImage(
                                model = activePage.imageUrl,
                                contentDescription = "Page ${displayedIndex + 1}",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                            
                            // Left navigation swipe tap zone
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(0.3f)
                                    .align(Alignment.CenterStart)
                                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                                        if (isRTL) {
                                            if (currentPageIndex < pages.size - 1) {
                                                viewModel.updateReaderPageSelected(currentPageIndex + 1)
                                            }
                                        } else {
                                            if (currentPageIndex > 0) {
                                                viewModel.updateReaderPageSelected(currentPageIndex - 1)
                                            }
                                        }
                                    }
                            )

                            // Right navigation swipe tap zone
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(0.3f)
                                    .align(Alignment.CenterEnd)
                                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                                        if (isRTL) {
                                            if (currentPageIndex > 0) {
                                                viewModel.updateReaderPageSelected(currentPageIndex - 1)
                                            }
                                        } else {
                                            if (currentPageIndex < pages.size - 1) {
                                                viewModel.updateReaderPageSelected(currentPageIndex + 1)
                                            }
                                        }
                                    }
                            )
                        }

                        // Bottom page index tag slider row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { 
                                    if (currentPageIndex > 0) viewModel.updateReaderPageSelected(currentPageIndex - 1)
                                },
                                enabled = currentPageIndex > 0
                            ) {
                                Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Prev", tint = Color.White)
                            }

                            Text(
                                text = "Page ${currentPageIndex + 1} / ${pages.size}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            IconButton(
                                onClick = {
                                    if (currentPageIndex < pages.size - 1) {
                                        viewModel.updateReaderPageSelected(currentPageIndex + 1)
                                    }
                                },
                                enabled = currentPageIndex < pages.size - 1
                            ) {
                                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next", tint = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Top Overlay Menu (Back, Title, Reader Settings)
        if (showMenuOverlays) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(Color.Black.copy(alpha = 0.85f))
                    .padding(WindowInsets.statusBars.asPaddingValues())
                    .padding(vertical = 8.dp, horizontal = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                val mId = activeChapter?.mangaId ?: ""
                                viewModel.navigateTo(Screen.MangaDetails(mId, sourceId))
                            },
                            modifier = Modifier.testTag("reader_back")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Exit Reader",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = activeChapter?.name ?: "Reading Chapter",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "Engine Mode: ${readerMode.name}",
                                color = Color.LightGray,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .testTag("reader_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Reader Config",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }

    // Modal Sheet settings dialog
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Display Settings") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Reading Mode Direction",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    // Mode Options
                    ReaderModeRow(
                        mode = ReaderMode.VerticalWebtoon,
                        title = "WEBTOON (Infinite Vertical Scroll)",
                        desc = "Flowing pages sequentially top-down. Perfect for manhwa.",
                        active = readerMode,
                        onModeSelected = { viewModel.setReaderMode(it) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    ReaderModeRow(
                        mode = ReaderMode.HorizontalHorizontal,
                        title = "Left to Right (Comic book)",
                        desc = "Classic comic orientation. Swipe horizontally left to advance.",
                        active = readerMode,
                        onModeSelected = { viewModel.setReaderMode(it) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    ReaderModeRow(
                        mode = ReaderMode.RightToLeft,
                        title = "Right to Left (Japanese Manga)",
                        desc = "Classic manga orientation. Swipe right to advance.",
                        active = readerMode,
                        onModeSelected = { viewModel.setReaderMode(it) }
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showSettingsDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun ReaderModeRow(
    mode: ReaderMode,
    title: String,
    desc: String,
    active: ReaderMode,
    onModeSelected: (ReaderMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onModeSelected(mode) }
            .background(
                if (active == mode) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.Transparent
            )
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = active == mode,
            onClick = { onModeSelected(mode) }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
            Text(desc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun FinishedChapterCard(onBack: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "You've Finished This Chapter!",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Read progress has been synchronized to your local database and active trackers.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Return to Series Details")
            }
        }
    }
}
