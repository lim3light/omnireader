package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MangaDao {
    @Query("SELECT * FROM manga WHERE isFavorite = 1")
    fun getFavorites(): Flow<List<MangaEntity>>

    @Query("SELECT * FROM manga WHERE id = :id LIMIT 1")
    suspend fun getMangaById(id: String): MangaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManga(manga: MangaEntity)

    @Update
    suspend fun updateManga(manga: MangaEntity)

    @Query("UPDATE manga SET isFavorite = :isFavorite, category = :category WHERE id = :id")
    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean, category: String)

    @Query("UPDATE manga SET trackerAniListStatus = :status WHERE id = :id")
    suspend fun updateAniListTracker(id: String, status: String?)

    @Query("UPDATE manga SET trackerMALStatus = :status WHERE id = :id")
    suspend fun updateMALTracker(id: String, status: String?)

    @Query("UPDATE manga SET lastReadChapterId = :chapterId, lastReadChapterName = :chapterName, lastReadTime = :time WHERE id = :mangaId")
    suspend fun updateLastRead(mangaId: String, chapterId: String, chapterName: String, time: Long)
}

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters WHERE mangaId = :mangaId ORDER BY chapterNumber ASC")
    fun getChaptersForManga(mangaId: String): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE mangaId = :mangaId ORDER BY chapterNumber ASC")
    suspend fun getChaptersForMangaSync(mangaId: String): List<ChapterEntity>

    @Query("SELECT * FROM chapters WHERE id = :id LIMIT 1")
    suspend fun getChapterById(id: String): ChapterEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Update
    suspend fun updateChapter(chapter: ChapterEntity)

    @Query("UPDATE chapters SET isRead = :isRead, lastPageRead = :pageIndex WHERE id = :id")
    suspend fun updateChapterReadStatus(id: String, isRead: Boolean, pageIndex: Int)

    @Query("UPDATE chapters SET isDownloaded = :isDownloaded, downloadPath = :downloadPath WHERE id = :id")
    suspend fun updateChapterDownloadStatus(id: String, isDownloaded: Boolean, downloadPath: String?)
}

@Dao
interface DownloadQueueDao {
    @Query("SELECT * FROM download_queue ORDER BY status DESC")
    fun getQueue(): Flow<List<DownloadQueueEntity>>

    @Query("SELECT * FROM download_queue")
    suspend fun getQueueSync(): List<DownloadQueueEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadQueueEntity)

    @Query("UPDATE download_queue SET status = :status, progress = :progress, downloadedPages = :downloaded, totalPages = :total, errorMessage = :error WHERE id = :id")
    suspend fun updateDownloadProgress(id: String, status: String, progress: Int, downloaded: Int, total: Int, error: String?)

    @Query("DELETE FROM download_queue WHERE id = :id")
    suspend fun deleteDownload(id: String)

    @Query("DELETE FROM download_queue")
    suspend fun clearAll()
}

@Dao
interface ExtensionDao {
    @Query("SELECT * FROM extensions")
    fun getAllExtensions(): Flow<List<ExtensionEntity>>

    @Query("SELECT * FROM extensions")
    suspend fun getAllExtensionsSync(): List<ExtensionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExtensions(extensions: List<ExtensionEntity>)

    @Query("UPDATE extensions SET isInstalled = :isInstalled WHERE id = :id")
    suspend fun updateInstalledStatus(id: String, isInstalled: Boolean)

    @Query("UPDATE extensions SET isActive = :isActive WHERE id = :id")
    suspend fun updateActiveStatus(id: String, isActive: Boolean)
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY readTime DESC")
    fun getHistory(): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity)

    @Query("DELETE FROM history WHERE chapterId = :id")
    suspend fun deleteHistoryByChapter(id: String)

    @Query("DELETE FROM history")
    suspend fun clearHistory()
}
