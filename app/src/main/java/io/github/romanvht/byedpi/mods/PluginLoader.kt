package io.github.romanvht.byedpi.mods

import android.content.Context
import android.util.Log
import dalvik.system.DexClassLoader
import io.github.romanvht.byedpi.api.StrategyPlugin
import java.io.File

/**
 * Загружает .dex-плагины из filesDir/plugins/ и превращает их в Mod.
 */
object PluginLoader {

    private const val TAG = "PluginLoader"
    private const val PLUGINS_DIR = "plugins"
    private const val API_CLASS = "io.github.romanvht.byedpi.api.StrategyPlugin"

    /**
     * Сканирует папку плагинов и возвращает список модов.
     */
    fun loadAll(context: Context): List<Mod> {
        val pluginsDir = File(context.filesDir, PLUGINS_DIR)
        if (!pluginsDir.exists()) {
            pluginsDir.mkdirs()
            return emptyList()
        }

        val result = mutableListOf<Mod>()

        pluginsDir.listFiles { file -> file.extension == "dex" }?.forEach { dexFile ->
            try {
                val mods = loadFromDex(context, dexFile)
                result.addAll(mods)
                Log.i(TAG, "Загружено модов из ${dexFile.name}: ${mods.size}")
            } catch (e: Exception) {
                Log.e(TAG, "Не удалось загрузить ${dexFile.name}: ${e.message}", e)
            }
        }

        return result
    }

    private fun loadFromDex(context: Context, dexFile: File): List<Mod> {
        val optimizedDir = File(context.codeCacheDir, "plugin_dex")
        if (!optimizedDir.exists()) optimizedDir.mkdirs()

        // DexClassLoader, который использует classLoader приложения как родителя,
        // чтобы плагин видел StrategyPlugin из API-модуля.
        val classLoader = DexClassLoader(
            dexFile.absolutePath,
            optimizedDir.absolutePath,
            null,
            context.classLoader
        )

        val pluginClass = Class.forName(API_CLASS, false, classLoader)

        // Ищем все .class-файлы в dex через DexFile
        val dexEntries = dalvik.system.DexFile(dexFile).entries()
        val mods = mutableListOf<Mod>()

        while (dexEntries.hasMoreElements()) {
            val className = dexEntries.nextElement()
            if (className == API_CLASS) continue

            try {
                val clazz = Class.forName(className, false, classLoader)
                if (pluginClass.isAssignableFrom(clazz) &&
                    !clazz.isInterface &&
                    !java.lang.reflect.Modifier.isAbstract(clazz.modifiers)) {

                    val instance = clazz.getDeclaredConstructor().newInstance() as StrategyPlugin
                    mods.add(DexModAdapter(instance))
                    Log.i(TAG, "Найден плагин: ${instance.name}")
                }
            } catch (_: ClassNotFoundException) {
                // не наш класс, пропускаем
            } catch (e: Exception) {
                Log.w(TAG, "Ошибка при загрузке класса $className: ${e.message}")
            }
        }

        return mods
    }
}
