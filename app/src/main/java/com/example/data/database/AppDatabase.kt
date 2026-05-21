package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        MangaEntity::class,
        ChapterEntity::class,
        DownloadQueueEntity::class,
        ExtensionEntity::class,
        HistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mangaDao(): MangaDao
    abstract fun chapterDao(): ChapterDao
    abstract fun downloadQueueDao(): DownloadQueueDao
    abstract fun extensionDao(): ExtensionDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "omni_reader_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Prepopulate default extensions when DB is created
                        CoroutineScope(Dispatchers.IO).launch {
                            val extDao = getDatabase(context).extensionDao()
                            val defaultExtensions = listOf(
                                ExtensionEntity("mangadex", "MangaDex Official", "1.4.2", true, true, "mangadex"),
                                ExtensionEntity("webtoons", "WEBTOON Scraper", "2.1.0", true, true, "webtoon"),
                                ExtensionEntity("mangakakalot", "MangaKakalot Scraper", "1.0.5", true, false, "kakalot"),
                                ExtensionEntity("comick", "ComicK Extension", "1.2.0", true, false, "comick"),
                                ExtensionEntity("local", "Local Folder Reader", "1.0.0", true, true, "folder")
                            )
                            extDao.insertExtensions(defaultExtensions)
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
