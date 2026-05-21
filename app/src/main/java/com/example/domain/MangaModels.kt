package com.example.domain

data class Manga(
    val id: String,
    val title: String,
    val author: String,
    val coverUrl: String,
    val description: String,
    val status: String,
    val sourceId: String,
    val sourceName: String,
    var isFavorite: Boolean = false,
    var category: String = "Default",
    val trackerAniListStatus: String? = null,
    val trackerMALStatus: String? = null
)

data class MangaDetails(
    val id: String,
    val title: String,
    val author: String,
    val coverUrl: String,
    val description: String,
    val status: String,
    val genre: List<String>,
    val sourceId: String
)

data class Chapter(
    val id: String,
    val mangaId: String,
    val name: String,
    val dateUpload: String,
    val chapterNumber: Float,
    var isRead: Boolean = false,
    var isDownloaded: Boolean = false,
    var lastPageRead: Int = 0,
    var downloadPath: String? = null
)

data class Page(
    val index: Int,
    val imageUrl: String,
    val width: Int = 1200,
    val height: Int = 1800
)

interface SourcePlugin {
    val id: String
    val name: String
    val version: String
    val logoAsset: String? // Identifier for drawn drawable or placeholder
    
    suspend fun search(query: String): List<Manga>
    suspend fun getDetails(id: String): MangaDetails
    suspend fun getChapters(mangaId: String): List<Chapter>
    suspend fun getPages(chapterId: String): List<Page>
}
