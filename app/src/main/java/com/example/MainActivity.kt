package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.example.ui.MangaViewModel
import com.example.ui.Screen
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MangaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            val isAmoled by viewModel.isAmoledMode.collectAsState()
            
            MyApplicationTheme(darkTheme = isAmoled) {
                val currentScreen by viewModel.currentScreen.collectAsState()
                
                // Pure AMOLED background or typical theme surface
                val rootBg = if (isAmoled) Color.Black else MaterialTheme.colorScheme.background

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(rootBg),
                    color = rootBg
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = rootBg,
                        bottomBar = {
                            // Only show bottom navigation on major roots, hide in immersive reader / detail views
                            if (currentScreen is Screen.Library || 
                                currentScreen is Screen.Browse || 
                                currentScreen is Screen.History || 
                                currentScreen is Screen.Downloads || 
                                currentScreen is Screen.Settings) {
                                
                                BottomNavigationBar(
                                    currentScreen = currentScreen,
                                    onNavigate = { viewModel.navigateTo(it) }
                                )
                            }
                        }
                    ) { innerPadding ->
                        
                        // Handle native back presses correctly to prevent instant app closing
                        BackHandler(enabled = currentScreen !is Screen.Library) {
                            when (currentScreen) {
                                is Screen.ReaderView -> {
                                    val actCh = viewModel.activeChapter.value
                                    if (actCh != null) {
                                        viewModel.navigateTo(Screen.MangaDetails(actCh.mangaId, (currentScreen as Screen.ReaderView).sourceId))
                                    } else {
                                        viewModel.navigateTo(Screen.Library)
                                    }
                                }
                                is Screen.MangaDetails -> {
                                    viewModel.navigateTo(Screen.Browse)
                                }
                                else -> {
                                    viewModel.navigateTo(Screen.Library)
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (currentScreen) {
                                is Screen.Library -> LibraryScreen(viewModel = viewModel)
                                is Screen.Browse -> BrowseScreen(viewModel = viewModel)
                                is Screen.History -> HistoryScreen(viewModel = viewModel)
                                is Screen.Downloads -> DownloadsScreen(viewModel = viewModel)
                                is Screen.Settings -> SettingsScreen(viewModel = viewModel)
                                is Screen.MangaDetails -> {
                                    MangaDetailsScreen(
                                        viewModel = viewModel,
                                        mangaId = (currentScreen as Screen.MangaDetails).mangaId,
                                        sourceId = (currentScreen as Screen.MangaDetails).sourceId
                                    )
                                }
                                is Screen.ReaderView -> {
                                    ReaderScreen(
                                        viewModel = viewModel,
                                        chapterId = (currentScreen as Screen.ReaderView).chapterId,
                                        sourceId = (currentScreen as Screen.ReaderView).sourceId
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

@Composable
fun BottomNavigationBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bottom_nav_bar")
    ) {
        NavigationBarItem(
            selected = currentScreen is Screen.Library,
            onClick = { onNavigate(Screen.Library) },
            icon = {
                Icon(
                    imageVector = if (currentScreen is Screen.Library) Icons.Filled.CollectionsBookmark else Icons.Outlined.CollectionsBookmark,
                    contentDescription = "Library"
                )
            },
            label = { Text("Library") },
            modifier = Modifier.testTag("nav_library")
        )
        
        NavigationBarItem(
            selected = currentScreen is Screen.Browse,
            onClick = { onNavigate(Screen.Browse) },
            icon = {
                Icon(
                    imageVector = if (currentScreen is Screen.Browse) Icons.Filled.Search else Icons.Outlined.Search,
                    contentDescription = "Browse"
                )
            },
            label = { Text("Browse") },
            modifier = Modifier.testTag("nav_browse")
        )

        NavigationBarItem(
            selected = currentScreen is Screen.History,
            onClick = { onNavigate(Screen.History) },
            icon = {
                Icon(
                    imageVector = if (currentScreen is Screen.History) Icons.Filled.History else Icons.Outlined.History,
                    contentDescription = "History"
                )
            },
            label = { Text("History") },
            modifier = Modifier.testTag("nav_history")
        )

        NavigationBarItem(
            selected = currentScreen is Screen.Downloads,
            onClick = { onNavigate(Screen.Downloads) },
            icon = {
                Icon(
                    imageVector = if (currentScreen is Screen.Downloads) Icons.Filled.Downloading else Icons.Outlined.FileDownload,
                    contentDescription = "Downloads"
                )
            },
            label = { Text("Downloads") },
            modifier = Modifier.testTag("nav_downloads")
        )

        NavigationBarItem(
            selected = currentScreen is Screen.Settings,
            onClick = { onNavigate(Screen.Settings) },
            icon = {
                Icon(
                    imageVector = if (currentScreen is Screen.Settings) Icons.Filled.Settings else Icons.Outlined.Settings,
                    contentDescription = "Settings"
                )
            },
            label = { Text("Settings") },
            modifier = Modifier.testTag("nav_settings")
        )
    }
}
