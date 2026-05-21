package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.DownloadQueueEntity
import com.example.data.database.ExtensionEntity
import com.example.data.database.HistoryEntity
import com.example.data.repository.MangaRepository
import com.example.domain.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MangaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = MangaRepository(db)

    // --- State: Active Screens & Navigation ---
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Library)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    // --- State: Amoled Theme Mode ---
    private val _isAmoledMode = MutableStateFlow(true)
    val isAmoledMode: StateFlow<Boolean> = _isAmoledMode.asStateFlow()

    fun setAmoledMode(enabled: Boolean) {
        _isAmoledMode.value = enabled
    }

    // --- State: Extensions/Plugins ---
    val extensionsList: StateFlow<List<ExtensionEntity>> = repository.allExtensions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun installExtension(id: String, install: Boolean) {
        viewModelScope.launch {
            repository.toggleExtensionInstalled(id, install)
        }
    }

    fun toggleExtensionActive(id: String, active: Boolean) {
        viewModelScope.launch {
            repository.toggleExtensionActive(id, active)
        }
    }

    // --- State: Global and Source Search ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Manga>>(emptyList())
    val searchResults: StateFlow<List<Manga>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _selectedSourceIdForBrowse = MutableStateFlow<String?>("mangadex")
    val selectedSourceIdForBrowse: StateFlow<String?> = _selectedSourceIdForBrowse.asStateFlow()

    fun selectSourceForBrowse(id: String?) {
        _selectedSourceIdForBrowse.value = id
        performSearch(_searchQuery.value)
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        performSearch(query)
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            _isSearching.value = true
            val sourceId = _selectedSourceIdForBrowse.value
            val list = if (sourceId == null) {
                repository.globalSearch(query) // across all installed
            } else {
                repository.searchSpecificSource(sourceId, query) // specific source
            }
            _searchResults.value = list
            _isSearching.value = false
        }
    }

    // --- State: Library ---
    val favoriteManga: StateFlow<List<Manga>> = repository.favoriteManga
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _libraryCategories = MutableStateFlow(listOf("Reading", "Favorites", "Completed", "On Hold", "Default"))
    val libraryCategories: StateFlow<List<String>> = _libraryCategories.asStateFlow()

    private val _selectedCategoryTab = MutableStateFlow("Reading")
    val selectedCategoryTab: StateFlow<String> = _selectedCategoryTab.asStateFlow()

    fun setCategoryTab(category: String) {
        _selectedCategoryTab.value = category
    }

    fun addCategory(categoryName: String) {
        if (categoryName.isNotBlank() && !_libraryCategories.value.contains(categoryName)) {
            _libraryCategories.value = _libraryCategories.value + categoryName
        }
    }

    fun toggleFavoriteManga(mangaId: String, currentFav: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(mangaId, !currentFav, _selectedCategoryTab.value)
        }
    }

    fun changeMangaCategory(mangaId: String, category: String) {
        viewModelScope.launch {
            repository.updateMangaCategory(mangaId, category)
        }
    }

    // --- State: Detail View ---
    private val _selectedManga = MutableStateFlow<Manga?>(null)
    val selectedManga: StateFlow<Manga?> = _selectedManga.asStateFlow()

    private val _selectedMangaDetails = MutableStateFlow<MangaDetails?>(null)
    val selectedMangaDetails: StateFlow<MangaDetails?> = _selectedMangaDetails.asStateFlow()

    private val _mangaChapters = MutableStateFlow<List<Chapter>>(emptyList())
    val mangaChapters: StateFlow<List<Chapter>> = _mangaChapters.asStateFlow()

    private val _isLoadingDetails = MutableStateFlow(false)
    val isLoadingDetails: StateFlow<Boolean> = _isLoadingDetails.asStateFlow()

    fun selectManga(manga: Manga) {
        _selectedManga.value = manga
        _selectedMangaDetails.value = null
        _mangaChapters.value = emptyList()
        _currentScreen.value = Screen.MangaDetails(manga.id, manga.sourceId)
        loadMangaDetailsAndChapters(manga.id, manga.sourceId)
    }

    fun loadMangaDetailsAndChapters(mangaId: String, sourceId: String) {
        viewModelScope.launch {
            _isLoadingDetails.value = true
            try {
                val details = repository.getMangaDetails(mangaId, sourceId)
                _selectedMangaDetails.value = details
                
                // Sync chapters
                val chap = repository.fetchAndSyncChapters(mangaId, sourceId)
                _mangaChapters.value = chap
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoadingDetails.value = false
            }
        }
        
        // Setup observer for local chapter read/download status changes
        viewModelScope.launch {
            repository.getLocalChapters(mangaId).collect {
                _mangaChapters.value = it
            }
        }
    }

    fun toggleChapterRead(chapterId: String, currentRead: Boolean) {
        viewModelScope.launch {
            repository.markChapterRead(chapterId, !currentRead, 0)
        }
    }

    // --- State: Active Reader Screen ---
    private val _activeChapter = MutableStateFlow<Chapter?>(null)
    val activeChapter: StateFlow<Chapter?> = _activeChapter.asStateFlow()

    private val _readerPages = MutableStateFlow<List<Page>>(emptyList())
    val readerPages: StateFlow<List<Page>> = _readerPages.asStateFlow()

    private val _readerMode = MutableStateFlow(ReaderMode.VerticalWebtoon) // Vertical, LTR, RTL
    val readerMode: StateFlow<ReaderMode> = _readerMode.asStateFlow()

    private val _currentPageIndexInReader = MutableStateFlow(0)
    val currentPageIndexInReader: StateFlow<Int> = _currentPageIndexInReader.asStateFlow()

    fun setReaderMode(mode: ReaderMode) {
        _readerMode.value = mode
    }

    fun openChapterInReader(chapter: Chapter, mangaSourceId: String) {
        _activeChapter.value = chapter
        _readerPages.value = emptyList()
        _currentPageIndexInReader.value = chapter.lastPageRead
        _currentScreen.value = Screen.ReaderView(chapter.id, mangaSourceId)
        
        viewModelScope.launch {
            val pages = repository.getPages(chapter.id, mangaSourceId)
            _readerPages.value = pages
        }
    }

    fun updateReaderPageSelected(index: Int) {
        _currentPageIndexInReader.value = index
        val chap = _activeChapter.value ?: return
        viewModelScope.launch {
            repository.markChapterRead(chap.id, isRead = index >= _readerPages.value.size - 1, pageIndex = index)
        }
    }

    // --- State: Download Manager ---
    val downloadQueue: StateFlow<List<DownloadQueueEntity>> = repository.downloadQueue
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun downloadChapter(chapterId: String, mangaId: String) {
        viewModelScope.launch {
            repository.queueChapterDownload(chapterId, mangaId)
        }
    }

    fun deleteDownloadedChapter(chapterId: String) {
        viewModelScope.launch {
            repository.deleteDownloadedChapter(chapterId)
        }
    }

    // --- State: History System ---
    val historyItems: StateFlow<List<HistoryEntity>> = repository.historyItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteHistoryItem(chapterId: String) {
        viewModelScope.launch {
            repository.deleteHistoryItem(chapterId)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // --- State: Tracker Simulated Integration ---
    private val _myAnimeListUsername = MutableStateFlow<String?>(null)
    val myAnimeListUsername: StateFlow<String?> = _myAnimeListUsername.asStateFlow()

    private val _aniListUsername = MutableStateFlow<String?>(null)
    val aniListUsername: StateFlow<String?> = _aniListUsername.asStateFlow()

    fun loginTracker(trackerName: String, username: String) {
        if (trackerName == "MAL") {
            _myAnimeListUsername.value = username
        } else {
            _aniListUsername.value = username
        }
    }

    fun logoutTracker(trackerName: String) {
        if (trackerName == "MAL") {
            _myAnimeListUsername.value = null
        } else {
            _aniListUsername.value = null
        }
    }

    fun setMangaTrackerStatus(mangaId: String, trackerName: String, status: String?) {
        viewModelScope.launch {
            repository.updateTrackingStatus(mangaId, trackerName, status)
            // Reload details to reflect new status
            _selectedManga.value?.let {
                val updated = repository.getMangaById(mangaId)
                if (updated != null) {
                    _selectedManga.value = updated
                }
            }
        }
    }

    // --- State: Backup Manager States ---
    private val _backupStatusMessage = MutableStateFlow<String?>(null)
    val backupStatusMessage: StateFlow<String?> = _backupStatusMessage.asStateFlow()

    fun createBackup(onBackupGenerated: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val json = repository.generateBackupJson()
                onBackupGenerated(json)
                _backupStatusMessage.value = "Backup successfully exported with ${favoriteManga.value.size} library titles!"
            } catch (e: Exception) {
                _backupStatusMessage.value = "Failed to create backup: ${e.message}"
            }
        }
    }

    fun restoreBackup(json: String) {
        viewModelScope.launch {
            try {
                val success = repository.restoreBackupJson(json)
                if (success) {
                    _backupStatusMessage.value = "Backup restored successfully! Check your updated library categories."
                } else {
                    _backupStatusMessage.value = "Failed to parse backup. Check formatting."
                }
            } catch (e: Exception) {
                _backupStatusMessage.value = "Restore failed: ${e.message}"
            }
        }
    }

    fun dismissBackupStatus() {
        _backupStatusMessage.value = null
    }
}

sealed class Screen {
    object Library : Screen()
    object Browse : Screen()
    object History : Screen()
    object Downloads : Screen()
    object Settings : Screen()
    data class MangaDetails(val mangaId: String, val sourceId: String) : Screen()
    data class ReaderView(val chapterId: String, val sourceId: String) : Screen()
}

enum class ReaderMode {
    HorizontalHorizontal,
    VerticalWebtoon,
    RightToLeft
}
