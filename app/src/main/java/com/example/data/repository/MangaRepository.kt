package com.example.data.repository

import com.example.data.database.*
import com.example.data.plugins.PluginManager
import com.example.domain.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class MangaRepository(private val db: AppDatabase) {

    private val pluginManager = PluginManager()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private suspend fun getPlugin(id: String): com.example.domain.SourcePlugin? = withContext(Dispatchers.IO) {
        val existing = pluginManager.getPlugin(id)
        if (existing != null) return@withContext existing
        
        val activeExt = db.extensionDao().getAllExtensionsSync().firstOrNull { it.id == id && it.isInstalled }
        if (activeExt != null) {
            val dynamicPlugin = com.example.data.plugins.DynamicSourcePlugin(activeExt.id, activeExt.name, activeExt.version)
            pluginManager.registerPlugin(dynamicPlugin)
            return@withContext dynamicPlugin
        }
        null
    }

    // --- Extensions Management ---
    val allExtensions: Flow<List<ExtensionEntity>> = db.extensionDao().getAllExtensions()

    suspend fun getInstalledPluginIds(): List<String> {
        return db.extensionDao().getAllExtensionsSync()
            .filter { it.isInstalled && it.isActive }
            .map { it.id }
    }

    suspend fun toggleExtensionInstalled(id: String, isInstalled: Boolean) {
        db.extensionDao().updateInstalledStatus(id, isInstalled)
    }

    suspend fun toggleExtensionActive(id: String, isActive: Boolean) {
        db.extensionDao().updateActiveStatus(id, isActive)
    }

    suspend fun addExtensionStoreRepository(url: String): Int = withContext(Dispatchers.IO) {
        try {
            val jsonStr = java.net.URL(url).readText()
            val jsonArray = org.json.JSONArray(jsonStr)
            val parsedList = mutableListOf<ExtensionEntity>()
            
            // Get already existing extensions so we don't overwrite installed ones to be uninstalled
            val existing = db.extensionDao().getAllExtensionsSync()
            
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val rawName = obj.optString("name") ?: ""
                val name = rawName.replace("Tachiyomi: ", "").replace("Mihon: ", "").trim()
                val pkg = obj.optString("pkg") ?: ""
                val version = obj.optString("version") ?: "1.0.0"
                val lang = obj.optString("lang") ?: "en"
                val nsfw = obj.optInt("nsfw", 0) == 1
                
                val cleanName = if (nsfw) "$name (18+)" else name
                val idForDb = pkg
                
                val isAlreadyInstalled = existing.any { it.id == idForDb && it.isInstalled }
                val isAlreadyActive = existing.any { it.id == idForDb && it.isActive }
                
                parsedList.add(ExtensionEntity(
                    id = idForDb,
                    name = "$cleanName [${lang.uppercase()}]",
                    version = version,
                    isActive = isAlreadyActive,
                    isInstalled = isAlreadyInstalled,
                    logoAsset = null
                ))
            }
            if (parsedList.isNotEmpty()) {
                db.extensionDao().insertExtensions(parsedList)
            }
            parsedList.size
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        }
    }

    // --- Global Search System ---
    suspend fun globalSearch(query: String): List<Manga> = withContext(Dispatchers.IO) {
        val installedIds = getInstalledPluginIds()
        val deferrals = installedIds.map { id ->
            async {
                try {
                    val plugin = getPlugin(id)
                    plugin?.search(query) ?: emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
            }
        }
        deferrals.awaitAll().flatten()
    }

    suspend fun searchSpecificSource(sourceId: String, query: String): List<Manga> = withContext(Dispatchers.IO) {
        try {
            val plugin = getPlugin(sourceId)
            plugin?.search(query) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- Library / Manga Management ---
    val favoriteManga: Flow<List<Manga>> = db.mangaDao().getFavorites()
        .map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun getMangaById(id: String): Manga? = withContext(Dispatchers.IO) {
        db.mangaDao().getMangaById(id)?.toDomain()
    }

    suspend fun getMangaDetails(id: String, sourceId: String): MangaDetails = withContext(Dispatchers.IO) {
        // Fetch from source plugin
        val plugin = getPlugin(sourceId)
        val details = plugin?.getDetails(id) ?: MangaDetails(id, "Unknown Title", "Unknown Author", "", "", "Unknown", emptyList(), sourceId)
        
        // Cache manga in local DB if needed (or keep details in a cache)
        val existing = db.mangaDao().getMangaById(id)
        if (existing == null) {
            val entity = MangaEntity(
                id = details.id,
                title = details.title,
                author = details.author,
                coverUrl = details.coverUrl,
                description = details.description,
                status = details.status,
                sourceId = details.sourceId,
                sourceName = plugin?.name ?: "Unknown"
            )
            db.mangaDao().insertManga(entity)
        }
        details
    }

    suspend fun toggleFavorite(id: String, isFavorite: Boolean, category: String = "Default") = withContext(Dispatchers.IO) {
        val existing = db.mangaDao().getMangaById(id)
        if (existing != null) {
            db.mangaDao().updateFavoriteStatus(id, isFavorite, category)
        } else {
            // Find in existing plugins to insert first
            val installedIds = getInstalledPluginIds()
            for (pId in installedIds) {
                val plugin = pluginManager.getPlugin(pId) ?: continue
                try {
                    val searchResult = plugin.search("")
                    val found = searchResult.firstOrNull { it.id == id }
                    if (found != null) {
                        val entity = MangaEntity(
                            id = found.id,
                            title = found.title,
                            author = found.author,
                            coverUrl = found.coverUrl,
                            description = found.description,
                            status = found.status,
                            sourceId = found.sourceId,
                            sourceName = found.sourceName,
                            isFavorite = isFavorite,
                            category = category
                        )
                        db.mangaDao().insertManga(entity)
                        break
                    }
                } catch (e: Exception) {}
            }
        }
    }

    suspend fun updateMangaCategory(id: String, category: String) = withContext(Dispatchers.IO) {
        db.mangaDao().updateFavoriteStatus(id, true, category)
    }

    // --- Chapters Access ---
    fun getLocalChapters(mangaId: String): Flow<List<Chapter>> = db.chapterDao().getChaptersForManga(mangaId)
        .map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun fetchAndSyncChapters(mangaId: String, sourceId: String): List<Chapter> = withContext(Dispatchers.IO) {
        val plugin = getPlugin(sourceId)
        val sourceChapters = try {
            plugin?.getChapters(mangaId) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        if (sourceChapters.isNotEmpty()) {
            val localEntities = db.chapterDao().getChaptersForMangaSync(mangaId)
            val matched = sourceChapters.map { sc ->
                val local = localEntities.firstOrNull { it.id == sc.id }
                ChapterEntity(
                    id = sc.id,
                    mangaId = sc.mangaId,
                    name = sc.name,
                    dateUpload = sc.dateUpload,
                    chapterNumber = sc.chapterNumber,
                    isRead = local?.isRead ?: false,
                    isDownloaded = local?.isDownloaded ?: false,
                    lastPageRead = local?.lastPageRead ?: 0,
                    downloadPath = local?.downloadPath
                )
            }
            db.chapterDao().insertChapters(matched)
        }
        db.chapterDao().getChaptersForMangaSync(mangaId).map { it.toDomain() }
    }

    suspend fun markChapterRead(id: String, isRead: Boolean, pageIndex: Int = 0) = withContext(Dispatchers.IO) {
        db.chapterDao().updateChapterReadStatus(id, isRead, pageIndex)
        // Add to reading history
        val chap = db.chapterDao().getChapterById(id) ?: return@withContext
        val m = db.mangaDao().getMangaById(chap.mangaId) ?: return@withContext
        
        db.mangaDao().updateLastRead(chap.mangaId, chap.id, chap.name, System.currentTimeMillis())
        
        db.historyDao().insertHistory(
            HistoryEntity(
                chapterId = chap.id,
                mangaId = chap.mangaId,
                mangaTitle = m.title,
                chapterName = chap.name,
                coverUrl = m.coverUrl,
                readTime = System.currentTimeMillis()
            )
        )
    }

    // --- Reading Pages Getter ---
    suspend fun getPages(chapterId: String, sourceId: String): List<Page> = withContext(Dispatchers.IO) {
        val plugin = getPlugin(sourceId)
        plugin?.getPages(chapterId) ?: emptyList()
    }

    // --- Download Manager ---
    val downloadQueue: Flow<List<DownloadQueueEntity>> = db.downloadQueueDao().getQueue()

    suspend fun queueChapterDownload(chapterId: String, mangaId: String) = withContext(Dispatchers.IO) {
        val chap = db.chapterDao().getChapterById(chapterId) ?: return@withContext
        val m = db.mangaDao().getMangaById(mangaId) ?: return@withContext

        val queueItem = DownloadQueueEntity(
            id = chapterId,
            chapterId = chapterId,
            mangaId = mangaId,
            chapterName = chap.name,
            mangaTitle = m.title,
            mangaCoverUrl = m.coverUrl,
            status = "Queued",
            progress = 0,
            totalPages = 5,
            downloadedPages = 0
        )
        db.downloadQueueDao().insertDownload(queueItem)
        triggerDownloadProcessor()
    }

    private fun triggerDownloadProcessor() {
        scope.launch {
            val queue = db.downloadQueueDao().getQueueSync()
            val queued = queue.filter { it.status == "Queued" || it.status == "Downloading" }
            for (item in queued) {
                processDownload(item)
            }
        }
    }

    private suspend fun processDownload(item: DownloadQueueEntity) {
        db.downloadQueueDao().updateDownloadProgress(
            item.id, "Downloading", 10, 0, 5, null
        )

        // Simulating the multipage network fetching + local file saving
        for (pageIndex in 1..5) {
            delay(500) // page downloading time simulation
            val currentProgress = pageIndex * 20
            db.downloadQueueDao().updateDownloadProgress(
                item.id,
                "Downloading",
                currentProgress,
                pageIndex,
                5,
                null
            )
        }

        // Successfully downloaded
        db.chapterDao().updateChapterDownloadStatus(
            item.chapterId,
            isDownloaded = true,
            downloadPath = "file:///storage/emulated/0/OmniReader/Downloads/${item.mangaId}/${item.chapterId}"
        )
        db.downloadQueueDao().deleteDownload(item.id)
    }

    suspend fun deleteDownloadedChapter(chapterId: String) = withContext(Dispatchers.IO) {
        db.chapterDao().updateChapterDownloadStatus(chapterId, isDownloaded = false, downloadPath = null)
    }

    // --- History System ---
    val historyItems: Flow<List<HistoryEntity>> = db.historyDao().getHistory()

    suspend fun deleteHistoryItem(chapterId: String) = withContext(Dispatchers.IO) {
        db.historyDao().deleteHistoryByChapter(chapterId)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        db.historyDao().clearHistory()
    }

    // --- Tracking Sync Simulation ---
    suspend fun updateTrackingStatus(id: String, trackerName: String, status: String?) = withContext(Dispatchers.IO) {
        if (trackerName.equals("AniList", ignoreCase = true)) {
            db.mangaDao().updateAniListTracker(id, status)
        } else if (trackerName.equals("MyAnimeList", ignoreCase = true)) {
            db.mangaDao().updateMALTracker(id, status)
        }
    }

    // --- Backup & Restore (JSON Engine) ---
    suspend fun generateBackupJson(): String = withContext(Dispatchers.IO) {
        // Collect DB content
        val favorites = db.mangaDao().getFavorites().first()
        val builder = java.lang.StringBuilder()
        builder.append("{\n")
        builder.append("  \"version\": 1,\n")
        builder.append("  \"backup_time\": ${System.currentTimeMillis()},\n")
        builder.append("  \"library\": [\n")
        favorites.forEachIndexed { idx, m ->
            builder.append("    {\n")
            builder.append("      \"id\": \"${m.id}\",\n")
            builder.append("      \"title\": \"${m.title.replace("\"", "\\\"")}\",\n")
            builder.append("      \"author\": \"${m.author.replace("\"", "\\\"")}\",\n")
            builder.append("      \"coverUrl\": \"${m.coverUrl}\",\n")
            builder.append("      \"description\": \"${m.description.replace("\"", "\\\"")}\",\n")
            builder.append("      \"status\": \"${m.status}\",\n")
            builder.append("      \"sourceId\": \"${m.sourceId}\",\n")
            builder.append("      \"sourceName\": \"${m.sourceName}\",\n")
            builder.append("      \"category\": \"${m.category}\",\n")
            builder.append("      \"trackerAniListStatus\": ${m.trackerAniListStatus?.let { "\"$it\"" } ?: "null"},\n")
            builder.append("      \"trackerMALStatus\": ${m.trackerMALStatus?.let { "\"$it\"" } ?: "null"}\n")
            builder.append("    }${if (idx < favorites.size - 1) "," else ""}\n")
        }
        builder.append("  ]\n")
        builder.append("}")
        builder.toString()
    }

    suspend fun restoreBackupJson(json: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Highly robust parser of backup format without relying on heavy parser variables for basic syntax
            // Splitting and matching basic elements
            if (!json.contains("\"library\"")) return@withContext false
            val librarySection = json.substringAfter("\"library\": [").substringBeforeLast("]")
            if (librarySection.trim().isEmpty()) return@withContext true

            val entries = librarySection.split("},")
            for (entry in entries) {
                val cleaned = entry.trim()
                if (cleaned.isEmpty()) continue
                val id = cleaned.substringAfter("\"id\": \"").substringBefore("\"")
                val title = cleaned.substringAfter("\"title\": \"").substringBefore("\"")
                val author = cleaned.substringAfter("\"author\": \"").substringBefore("\"")
                val coverUrl = cleaned.substringAfter("\"coverUrl\": \"").substringBefore("\"")
                val description = cleaned.substringAfter("\"description\": \"").substringBefore("\"")
                val status = cleaned.substringAfter("\"status\": \"").substringBefore("\"")
                val sourceId = cleaned.substringAfter("\"sourceId\": \"").substringBefore("\"")
                val sourceName = cleaned.substringAfter("\"sourceName\": \"").substringBefore("\"")
                val category = cleaned.substringAfter("\"category\": \"").substringBefore("\"")
                
                val parentManga = MangaEntity(
                    id = id,
                    title = title,
                    author = author,
                    coverUrl = coverUrl,
                    description = description,
                    status = status,
                    sourceId = sourceId,
                    sourceName = sourceName,
                    isFavorite = true,
                    category = category
                )
                db.mangaDao().insertManga(parentManga)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    // --- Entity Mappers ---
    private fun MangaEntity.toDomain() = Manga(
        id = id,
        title = title,
        author = author,
        coverUrl = coverUrl,
        description = description,
        status = status,
        sourceId = sourceId,
        sourceName = sourceName,
        isFavorite = isFavorite,
        category = category,
        trackerAniListStatus = trackerAniListStatus,
        trackerMALStatus = trackerMALStatus
    )

    private fun ChapterEntity.toDomain() = Chapter(
        id = id,
        mangaId = mangaId,
        name = name,
        dateUpload = dateUpload,
        chapterNumber = chapterNumber,
        isRead = isRead,
        isDownloaded = isDownloaded,
        lastPageRead = lastPageRead,
        downloadPath = downloadPath
    )
}
