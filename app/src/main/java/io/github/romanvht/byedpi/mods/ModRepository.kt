package io.github.romanvht.byedpi.mods

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

/**
 * Репозиторий модов — работает с GitHub.
 */
object ModRepository {

    private const val TAG = "ModRepository"

    // Твой репозиторий и путь к папке mods
    private const val REPO = "mark213364/ByeByeDPI"
    private const val MODS_PATH = "mods"

    private const val MANIFEST_URL =
        "https://raw.githubusercontent.com/$REPO/master/$MODS_PATH/manifest.json"

    private const val MODS_BASE_URL =
        "https://raw.githubusercontent.com/$REPO/master/$MODS_PATH"

    /**
     * Описание одного мода из manifest.json.
     */
    data class ModInfo(
        val id: String,
        val name: String,
        val description: String,
        val version: String,
        val author: String,
        val file: String,
        val size: Long = 0
    )

    /**
     * Загружает manifest.json с GitHub и возвращает список модов.
     */
    suspend fun fetchMods(): List<ModInfo> = withContext(Dispatchers.IO) {
        try {
            val json = URL(MANIFEST_URL).readText()
            val root = JSONObject(json)
            val modsArray = root.getJSONArray("mods")
            parseMods(modsArray)
        } catch (e: Exception) {
            Log.e(TAG, "Не удалось загрузить manifest.json: ${e.message}", e)
            emptyList()
        }
    }

    private fun parseMods(array: JSONArray): List<ModInfo> {
        val result = mutableListOf<ModInfo>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            result.add(
                ModInfo(
                    id = obj.optString("id", "unknown"),
                    name = obj.optString("name", "Без названия"),
                    description = obj.optString("description", ""),
                    version = obj.optString("version", "1.0"),
                    author = obj.optString("author", "Unknown"),
                    file = obj.getString("file"),
                    size = obj.optLong("size", 0L)
                )
            )
        }
        return result
    }

    /**
     * Возвращает прямую ссылку на .dex-файл мода.
     */
    fun getDownloadUrl(mod: ModInfo): String = "$MODS_BASE_URL/${mod.file}"
}
