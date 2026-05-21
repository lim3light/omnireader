package com.example.data.plugins

import com.example.domain.*
import kotlinx.coroutines.delay

class DynamicSourcePlugin(
    override val id: String,
    override val name: String,
    override val version: String,
    override val logoAsset: String? = null
) : SourcePlugin {

    private val cleanName = name
        .replace("Tachiyomi: ", "")
        .replace("Mihon: ", "")
        .replace("Extension", "")
        .replace("Scraper", "")
        .replace("[ALL]", "")
        .replace("[EN]", "")
        .replace("[ES]", "")
        .replace("[FR]", "")
        .replace("[ID]", "")
        .replace("[JA]", "")
        .replace("[KO]", "")
        .replace("[RU]", "")
        .replace("[ZH]", "")
        .trim()

    private val defaultTitles = listOf(
        Manga(
            id = "${id}-dyn-1",
            title = "Solo leveling in $cleanName",
            author = "Keiyoushi Creator",
            coverUrl = "https://images.unsplash.com/photo-1626544827763-d516dce335e2?w=500&q=80",
            description = "A phenomenal action-fantasy series populated dynamically for the $name repository extension. Our protagonist must explore the depth of the $cleanName network to reclaim his true power.",
            status = "Ongoing",
            sourceId = id,
            sourceName = name
        ),
        Manga(
            id = "${id}-dyn-2",
            title = "The Legendary Master of $cleanName",
            author = "Mihon Artist",
            coverUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=500&q=80",
            description = "Waking up in an unknown timeline, a young gamer realizes he has been reincarnated inside $cleanName. Armed with modern knowledge and custom skills, he starts his rise to the apex.",
            status = "Ongoing",
            sourceId = id,
            sourceName = name
        ),
        Manga(
            id = "${id}-dyn-3",
            title = "Chronicles of the $cleanName Sect",
            author = "Eastern Sage",
            coverUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=500&q=80",
            description = "A grand Murim story based in the legendary peaks of $cleanName. When a betrayed master re-emerges fifty years later, the martial world is thrown into unmatched turbulence.",
            status = "Completed",
            sourceId = id,
            sourceName = name
        )
    )

    override suspend fun search(query: String): List<Manga> {
        delay(400) // Simulate network delay
        if (query.isBlank()) return defaultTitles

        val matchedDefault = defaultTitles.filter { 
            it.title.contains(query, ignoreCase = true) or it.author.contains(query, ignoreCase = true) 
        }

        // Dynamically spawn search-specific matching titles to make queries incredibly fun!
        val dynamicSearch = listOf(
            Manga(
                id = "${id}-search-1",
                title = query.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } + " [Dynamic]",
                author = "Keiyoushi Scraper",
                coverUrl = "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=500&q=80",
                description = "An interactive story spawned dynamically for the search term '$query' on the installed $name extension.",
                status = "Ongoing",
                sourceId = id,
                sourceName = name
            ),
            Manga(
                id = "${id}-search-2",
                title = "Rise of the " + query.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                author = "Mihon Scraper",
                coverUrl = "https://images.unsplash.com/photo-1541701494587-cb58502866ab?w=500&q=80",
                description = "Legendary forces collide after the activation of '$query' in the magical boundaries of $cleanName.",
                status = "Ongoing",
                sourceId = id,
                sourceName = name
            )
        )

        return dynamicSearch + matchedDefault
    }

    override suspend fun getDetails(id: String): MangaDetails {
        delay(300)
        // Check if it's one of default ones or dynamically created search titles
        val m = defaultTitles.firstOrNull { it.id == id } 
            ?: if (id.endsWith("-search-1") || id.endsWith("-search-2")) {
                // Return dynamic details based on ID
                Manga(
                    id = id,
                    title = "Adventure of the Search Query",
                    author = "Keiyoushi Scraper",
                    coverUrl = "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=500&q=80",
                    description = "Dynamic content loaded from extension $name database.",
                    status = "Ongoing",
                    sourceId = this.id,
                    sourceName = name
                )
            } else {
                defaultTitles[0]
            }

        return MangaDetails(
            id = m.id,
            title = m.title,
            author = m.author,
            coverUrl = m.coverUrl,
            description = m.description,
            status = m.status,
            genre = listOf("Action", "Adventure", "Fantasy", "Dynamic", "Keiyoushi"),
            sourceId = this.id
        )
    }

    override suspend fun getChapters(mangaId: String): List<Chapter> {
        delay(300)
        return listOf(
            Chapter("${mangaId}-c1", mangaId, "Chapter 1: Rise of the $cleanName", "May 10, 2026", 1f),
            Chapter("${mangaId}-c2", mangaId, "Chapter 2: Tactical Awakening", "May 12, 2026", 2f),
            Chapter("${mangaId}-c3", mangaId, "Chapter 3: Guardian of $cleanName", "May 15, 2026", 3f),
            Chapter("${mangaId}-c4", mangaId, "Chapter 4: Decisive Showdown", "May 18, 2026", 4f),
            Chapter("${mangaId}-c5", mangaId, "Chapter 5: Absolute Domain Expansion", "May 20, 2026", 5f)
        )
    }

    override suspend fun getPages(chapterId: String): List<Page> {
        delay(400)
        // High quality thematic images based on index
        return listOf(
            Page(0, "https://images.unsplash.com/photo-1541512416146-3cf58d6b27cc?w=800&q=85"),
            Page(1, "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=800&q=85"),
            Page(2, "https://images.unsplash.com/photo-1501854140801-50d01698950b?w=800&q=85"),
            Page(3, "https://images.unsplash.com/photo-1447752875215-b2761acb3c5d?w=800&q=85"),
            Page(4, "https://images.unsplash.com/photo-1472214222541-d510753a4907?w=800&q=85")
        )
    }
}
