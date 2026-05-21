package com.example.data.plugins

import com.example.domain.*
import kotlinx.coroutines.delay

class MangaDexPlugin : SourcePlugin {
    override val id = "mangadex"
    override val name = "MangaDex Official"
    override val version = "1.4.2"
    override val logoAsset = "mangadex"

    private val titles = listOf(
        Manga(
            id = "md-1",
            title = "Frieren: Beyond Journey's End",
            author = "Kanehito Yamada",
            coverUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=500&q=80",
            description = "Elf mage Frieren and her courageous fellow adventurers have defeated the Demon King and brought peace to the land. But Frieren will long outlive the rest of her former party... How will she learn to appreciate the meaning of human life?",
            status = "Ongoing",
            sourceId = id,
            sourceName = name
        ),
        Manga(
            id = "md-2",
            title = "Chainsaw Man",
            author = "Tatsuki Fujimoto",
            coverUrl = "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=500&q=80",
            description = "Denji has a simple dream—to live a happy and peaceful life, spending time with a girl he likes. This is a far cry from reality, however, as Denji is forced by the yakuza into killing devils in order to pay off his crushing debts.",
            status = "Ongoing",
            sourceId = id,
            sourceName = name
        ),
        Manga(
            id = "md-3",
            title = "Demon Slayer",
            author = "Koyoharu Gotouge",
            coverUrl = "https://images.unsplash.com/photo-1541701494587-cb58502866ab?w=500&q=80",
            description = "In Taisho-era Japan, Tanjirou Kamado is a kindhearted boy who makes a living selling charcoal. His peaceful life is shattered when a demon slaughters his entire family, save for his younger sister, Nezuko, who is turned into a demon herself.",
            status = "Completed",
            sourceId = id,
            sourceName = name
        ),
        Manga(
            id = "md-4",
            title = "Oshi no Ko",
            author = "Aka Akasaka",
            coverUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=500&q=80",
            description = "Dr. Goro, a gynecologist in a countryside town, is a massive fan of the rising 16-year-old idol Ai Hoshino. But when the starlet arrives at his hospital pregnant, his quiet life turns completely upside down when he's reincarnated as her child!",
            status = "Ongoing",
            sourceId = id,
            sourceName = name
        )
    )

    override suspend fun search(query: String): List<Manga> {
        delay(400) // Simulate network delay
        if (query.isBlank()) return titles
        return titles.filter { it.title.contains(query, ignoreCase = true) || it.author.contains(query, ignoreCase = true) }
    }

    override suspend fun getDetails(id: String): MangaDetails {
        delay(300)
        val m = titles.firstOrNull { it.id == id } ?: titles[0]
        return MangaDetails(
            id = m.id,
            title = m.title,
            author = m.author,
            coverUrl = m.coverUrl,
            description = m.description,
            status = m.status,
            genre = listOf("Fantasy", "Adventure", "Drama", "Shounen"),
            sourceId = this.id
        )
    }

    override suspend fun getChapters(mangaId: String): List<Chapter> {
        delay(300)
        return listOf(
            Chapter("${mangaId}-c1", mangaId, "Chapter 1: The Journey Begins", "May 10, 2026", 1f),
            Chapter("${mangaId}-c2", mangaId, "Chapter 2: Master of Magic", "May 12, 2026", 2f),
            Chapter("${mangaId}-c3", mangaId, "Chapter 3: Blue Orchids", "May 15, 2026", 3f),
            Chapter("${mangaId}-c4", mangaId, "Chapter 4: Echoes of the Past", "May 18, 2026", 4f),
            Chapter("${mangaId}-c5", mangaId, "Chapter 5: A New Companion", "May 20, 2026", 5f)
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

class WebtoonPlugin : SourcePlugin {
    override val id = "webtoons"
    override val name = "WEBTOON Scraper"
    override val version = "2.1.0"
    override val logoAsset = "webtoon"

    private val titles = listOf(
        Manga(
            id = "wt-1",
            title = "Tower of God",
            author = "SIU",
            coverUrl = "https://images.unsplash.com/photo-1626544827763-d516dce335e2?w=500&q=80",
            description = "What do you desire? Money and wealth? Honor and pride? Authority and power? Revenge? Or something that transcends them all? Whatever you desire—it's here, at the peak of the Tower.",
            status = "Ongoing",
            sourceId = id,
            sourceName = name
        ),
        Manga(
            id = "wt-2",
            title = "The Boxer",
            author = "JH",
            coverUrl = "https://images.unsplash.com/photo-1517438476312-12d7a040916d?w=500&q=80",
            description = "Do you have the raw talent? Do you have the drive? Meet Yu, a 17-year-old boy whose life is an empty shell. But when he meets legendary boxing coach K, his dormant monstrous talent is unleashed to conquer the boxing world.",
            status = "Completed",
            sourceId = id,
            sourceName = name
        ),
        Manga(
            id = "wt-3",
            title = "Lore Olympus",
            author = "Rachel Smythe",
            coverUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=500&q=80",
            description = "Witness what the gods do after dark. A modern, stylish, and romantic retelling of one of mythology's greatest stories—the taking of Persephone—by Eisner-winning creator Rachel Smythe.",
            status = "Ongoing",
            sourceId = id,
            sourceName = name
        )
    )

    override suspend fun search(query: String): List<Manga> {
        delay(400)
        if (query.isBlank()) return titles
        return titles.filter { it.title.contains(query, ignoreCase = true) || it.author.contains(query, ignoreCase = true) }
    }

    override suspend fun getDetails(id: String): MangaDetails {
        delay(300)
        val m = titles.firstOrNull { it.id == id } ?: titles[0]
        return MangaDetails(
            id = m.id,
            title = m.title,
            author = m.author,
            coverUrl = m.coverUrl,
            description = m.description,
            status = m.status,
            genre = listOf("Webtoon", "Action", "Romance", "Fantasy"),
            sourceId = this.id
        )
    }

    override suspend fun getChapters(mangaId: String): List<Chapter> {
        delay(300)
        return listOf(
            Chapter("${mangaId}-c1", mangaId, "Episode 1: The Doors of the Floor", "Jan 12, 2026", 1f),
            Chapter("${mangaId}-c2", mangaId, "Episode 2: Test of Courage", "Jan 15, 2026", 2f),
            Chapter("${mangaId}-c3", mangaId, "Episode 3: Crown Game Entrance", "Jan 19, 2026", 3f),
            Chapter("${mangaId}-c4", mangaId, "Episode 4: Black March Unleashed", "Jan 22, 2026", 4f),
            Chapter("${mangaId}-c5", mangaId, "Episode 5: Moving Upwards", "Jan 26, 2026", 5f)
        )
    }

    override suspend fun getPages(chapterId: String): List<Page> {
        delay(400)
        // Tall flowing images suitable for webtoon format scrolling
        return listOf(
            Page(0, "https://images.unsplash.com/photo-1579783928621-7a13d66a62d1?w=800&q=85", 800, 1600),
            Page(1, "https://images.unsplash.com/photo-1513364776144-60967b0f800f?w=800&q=85", 800, 1600),
            Page(2, "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&q=85", 800, 1600),
            Page(3, "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800&q=85", 800, 1600),
            Page(4, "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=800&q=85", 800, 1600),
            Page(5, "https://images.unsplash.com/photo-1508739773434-c26b3d09e071?w=800&q=85", 800, 1600)
        )
    }
}

class MangaKakalotPlugin : SourcePlugin {
    override val id = "mangakakalot"
    override val name = "MangaKakalot Scraper"
    override val version = "1.0.5"
    override val logoAsset = "kakalot"

    private val titles = listOf(
        Manga(
            id = "mk-1",
            title = "Solo Leveling",
            author = "Chugong",
            coverUrl = "https://images.unsplash.com/photo-1618336753974-aae8e04506aa?w=500&q=80",
            description = "In a world where Hunters must battle deadly monsters to protect mankind, Sung Jinwoo, the weakest hunter of all mankind, finds himself in a struggle for survival in a double dungeon. Can he level up to become the absolute strongest?",
            status = "Completed",
            sourceId = id,
            sourceName = name
        ),
        Manga(
            id = "mk-2",
            title = "Vagabond",
            author = "Takehiko Inoue",
            coverUrl = "https://images.unsplash.com/photo-1520038410233-7141be7e6f97?w=500&q=80",
            description = "Growing up in late 16th-century Sengoku-era Japan, Shinmen Takezou is shunned by the local villagers as a devil child. He runs away with his friend to join the Toyotomi army, embarking on an epic journey to prove himself 'Unrivaled Under Heaven'.",
            status = "Ongoing",
            sourceId = id,
            sourceName = name
        )
    )

    override suspend fun search(query: String): List<Manga> {
        delay(400)
        if (query.isBlank()) return titles
        return titles.filter { it.title.contains(query, ignoreCase = true) || it.author.contains(query, ignoreCase = true) }
    }

    override suspend fun getDetails(id: String): MangaDetails {
        delay(300)
        val m = titles.firstOrNull { it.id == id } ?: titles[0]
        return MangaDetails(
            id = m.id,
            title = m.title,
            author = m.author,
            coverUrl = m.coverUrl,
            description = m.description,
            status = m.status,
            genre = listOf("Action", "Adventure", "Martial Arts", "Seinen"),
            sourceId = this.id
        )
    }

    override suspend fun getChapters(mangaId: String): List<Chapter> {
        delay(300)
        return listOf(
            Chapter("${mangaId}-c1", mangaId, "Chapter 1: The D-Rank Raid", "Feb 05, 2026", 1f),
            Chapter("${mangaId}-c2", mangaId, "Chapter 2: Double Dungeon", "Feb 08, 2026", 2f),
            Chapter("${mangaId}-c3", mangaId, "Chapter 3: The Secret Quest", "Feb 12, 2026", 3f),
            Chapter("${mangaId}-c4", mangaId, "Chapter 4: Leveling Up Alone", "Feb 16, 2026", 4f),
            Chapter("${mangaId}-c5", mangaId, "Chapter 5: Instant Dungeon Key", "Feb 20, 2026", 5f)
        )
    }

    override suspend fun getPages(chapterId: String): List<Page> {
        delay(400)
        return listOf(
            Page(0, "https://images.unsplash.com/photo-1618336753974-aae8e04506aa?w=800&q=85"),
            Page(1, "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=800&q=85"),
            Page(2, "https://images.unsplash.com/photo-1541701494587-cb58502866ab?w=800&q=85"),
            Page(3, "https://images.unsplash.com/photo-1626544827763-d516dce335e2?w=800&q=85"),
            Page(4, "https://images.unsplash.com/photo-1517438476312-12d7a040916d?w=800&q=85")
        )
    }
}

class ComicKPlugin : SourcePlugin {
    override val id = "comick"
    override val name = "ComicK Extension"
    override val version = "1.2.0"
    override val logoAsset = "comick"

    private val titles = listOf(
        Manga(
            id = "ck-1",
            title = "Kingdom",
            author = "Yasuhisa Hara",
            coverUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=500&q=80",
            description = "During the Warring States period of ancient China, Xin and Piao are war-orphans who dream of becoming the world's greatest generals. When an event separates them and places Xin in the service of Ying Zheng, their journey begins.",
            status = "Ongoing",
            sourceId = id,
            sourceName = name
        ),
        Manga(
            id = "ck-2",
            title = "Berserk",
            author = "Kentaro Miura",
            coverUrl = "https://images.unsplash.com/photo-1518005020951-eccb494ad742?w=500&q=80",
            description = "Guts, known as the Black Swordsman, seeks sanctuary from the demonic forces that pursue him and his woman, and vengeance against the man who branded him as an unholy sacrifice.",
            status = "Ongoing",
            sourceId = id,
            sourceName = name
        )
    )

    override suspend fun search(query: String): List<Manga> {
        delay(400)
        if (query.isBlank()) return titles
        return titles.filter { it.title.contains(query, ignoreCase = true) || it.author.contains(query, ignoreCase = true) }
    }

    override suspend fun getDetails(id: String): MangaDetails {
        delay(300)
        val m = titles.firstOrNull { it.id == id } ?: titles[0]
        return MangaDetails(
            id = m.id,
            title = m.title,
            author = m.author,
            coverUrl = m.coverUrl,
            description = m.description,
            status = m.status,
            genre = listOf("Dark Fantasy", "Action", "Tragedy", "Seinen"),
            sourceId = this.id
        )
    }

    override suspend fun getChapters(mangaId: String): List<Chapter> {
        delay(300)
        return listOf(
            Chapter("${mangaId}-c1", mangaId, "Chapter 1: The Black Swordsman", "Mar 01, 2026", 1f),
            Chapter("${mangaId}-c2", mangaId, "Chapter 2: Brand of Sacrifice", "Mar 05, 2026", 2f),
            Chapter("${mangaId}-c3", mangaId, "Chapter 3: Golden Age", "Mar 10, 2026", 3f),
            Chapter("${mangaId}-c4", mangaId, "Chapter 4: Band of the Hawk", "Mar 15, 2026", 4f),
            Chapter("${mangaId}-c5", mangaId, "Chapter 5: Nosferatu Zodd", "Mar 20, 2026", 5f)
        )
    }

    override suspend fun getPages(chapterId: String): List<Page> {
        delay(400)
        return listOf(
            Page(0, "https://images.unsplash.com/photo-1518005020951-eccb494ad742?w=800&q=85"),
            Page(1, "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=800&q=85"),
            Page(2, "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=800&q=85"),
            Page(3, "https://images.unsplash.com/photo-1626544827763-d516dce335e2?w=800&q=85"),
            Page(4, "https://images.unsplash.com/photo-1541701494587-cb58502866ab?w=800&q=85")
        )
    }
}

class LocalPlugin : SourcePlugin {
    override val id = "local"
    override val name = "Local Folder Reader"
    override val version = "1.0.0"
    override val logoAsset = "folder"

    private val titles = listOf(
        Manga(
            id = "loc-1",
            title = "My Local Adventure Booklet [Local]",
            author = "Me",
            coverUrl = "https://images.unsplash.com/photo-1488190211105-8b0e65b80b4e?w=500&q=80",
            description = "A scanned collection of local sketch comic panels and notes stored locally. Fully accessible without active network connections.",
            status = "Local",
            sourceId = id,
            sourceName = name
        ),
        Manga(
            id = "loc-2",
            title = "Webtoon Sketchbook Draft [Local]",
            author = "Studio X",
            coverUrl = "https://images.unsplash.com/photo-1460661419201-fd4cecdf8a8b?w=500&q=80",
            description = "A vertical long scroll local draft. Used for testing webtoon continuous scroll engine.",
            status = "Local",
            sourceId = id,
            sourceName = name
        )
    )

    override suspend fun search(query: String): List<Manga> {
        if (query.isBlank()) return titles
        return titles.filter { it.title.contains(query, ignoreCase = true) || it.author.contains(query, ignoreCase = true) }
    }

    override suspend fun getDetails(id: String): MangaDetails {
        val m = titles.firstOrNull { it.id == id } ?: titles[0]
        return MangaDetails(
            id = m.id,
            title = m.title,
            author = m.author,
            coverUrl = m.coverUrl,
            description = m.description,
            status = m.status,
            genre = listOf("Sketch", "Local", "Webtoon"),
            sourceId = this.id
        )
    }

    override suspend fun getChapters(mangaId: String): List<Chapter> {
        return listOf(
            Chapter("${mangaId}-c1", mangaId, "Chapter 1: Local Sketches", "Local Store", 1f, isDownloaded = true, downloadPath = "local_cache/chap1"),
            Chapter("${mangaId}-c2", mangaId, "Chapter 2: Layout Drafts", "Local Store", 2f, isDownloaded = true, downloadPath = "local_cache/chap2")
        )
    }

    override suspend fun getPages(chapterId: String): List<Page> {
        // Beautiful, highly reliable design sketches that are fully cached and render dynamically
        return listOf(
            Page(0, "https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=800&q=85"),
            Page(1, "https://images.unsplash.com/photo-1506880018603-83d5b814b5a6?w=800&q=85"),
            Page(2, "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=800&q=85"),
            Page(3, "https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?w=800&q=85")
        )
    }
}
