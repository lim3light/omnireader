package com.example.data.plugins

import com.example.domain.SourcePlugin

class PluginManager {
    private val allPlugins = mutableMapOf<String, SourcePlugin>(
        "mangadex" to MangaDexPlugin(),
        "webtoons" to WebtoonPlugin(),
        "mangakakalot" to MangaKakalotPlugin(),
        "comick" to ComicKPlugin(),
        "local" to LocalPlugin()
    )

    fun registerPlugin(plugin: SourcePlugin) {
        allPlugins[plugin.id] = plugin
    }

    fun getPlugin(id: String): SourcePlugin? {
        return allPlugins[id]
    }

    fun getAllAvailable(): List<SourcePlugin> {
        return allPlugins.values.toList()
    }
}
