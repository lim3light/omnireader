package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "manga")
data class MangaEntity(
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
    val coverUrl: String,
    val description: String,
    val status: String,
    val sourceId: String,
    val sourceName: String,
    val isFavorite: Boolean = false,
    val category: String = "Default",
    val lastReadChapterId: String? = null,
    val lastReadChapterName: String? = null,
    val lastReadTime: Long = 0L,
    val trackerAniListStatus: String? = null,
    val trackerMALStatus: String? = null
)

@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey val id: String,
    val mangaId: String,
    val name: String,
    val dateUpload: String,
    val chapterNumber: Float,
    val isRead: Boolean = false,
    val isDownloaded: Boolean = false,
    val lastPageRead: Int = 0,
    val downloadPath: String? = null
)

@Entity(tableName = "download_queue")
data class DownloadQueueEntity(
    @PrimaryKey val id: String, // same as chapterId
    val chapterId: String,
    val mangaId: String,
    val chapterName: String,
    val mangaTitle: String,
    val mangaCoverUrl: String,
    val status: String, // "Queued", "Downloading", "Completed", "Failed"
    val progress: Int = 0,
    val errorMessage: String? = null,
    val totalPages: Int = 0,
    val downloadedPages: Int = 0
)

@Entity(tableName = "extensions")
data class ExtensionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val version: String,
    val isActive: Boolean = true,
    val isInstalled: Boolean = false,
    val logoAsset: String? = null
)

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey val chapterId: String,
    val mangaId: String,
    val mangaTitle: String,
    val chapterName: String,
    val coverUrl: String,
    val readTime: Long
)
