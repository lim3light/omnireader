package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.HistoryToggleOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ui.MangaViewModel
import com.example.ui.Screen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: MangaViewModel,
    modifier: Modifier = Modifier
) {
    val historyItems by viewModel.historyItems.collectAsState()
    val sdf = remember { SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recent History", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    if (historyItems.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.clearHistory() },
                            modifier = Modifier.testTag("clear_history_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear All History")
                        }
                    }
                }
            )
        },
        containerColor = Color.Transparent,
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (historyItems.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.HistoryToggleOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "History is clean",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Chapters you read across catalogs will appear here indicating last page metrics.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(historyItems, key = { it.chapterId }) { item ->
                        ListItem(
                            headlineContent = {
                                Text(item.mangaTitle, fontWeight = FontWeight.Bold, maxLines = 1)
                            },
                            supportingContent = {
                                Column {
                                    Text(item.chapterName, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Read on: ${sdf.format(Date(item.readTime))}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            leadingContent = {
                                AsyncImage(
                                    model = item.coverUrl,
                                    contentDescription = item.mangaTitle,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                )
                            },
                            trailingContent = {
                                IconButton(
                                    onClick = { viewModel.deleteHistoryItem(item.chapterId) },
                                    modifier = Modifier.testTag("delete_his_${item.chapterId}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete from History",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                    )
                                }
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    // Resume reading instantly
                                    // Try fetching manga matching Id
                                    viewModel.favoriteManga.value.find { it.id == item.mangaId }?.let { m ->
                                        viewModel.selectManga(m)
                                    }
                                }
                                .testTag("history_item_${item.chapterId}")
                        )
                    }
                }
            }
        }
    }
}
