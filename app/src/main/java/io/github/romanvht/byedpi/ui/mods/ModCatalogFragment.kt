package io.github.romanvht.byedpi.ui.mods

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import io.github.romanvht.byedpi.mods.ModRepository
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

class ModCatalogFragment : PreferenceFragmentCompat() {

    companion object {
        private const val TAG = "ModCatalogFragment"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val context = requireContext()
        val screen = preferenceManager.createPreferenceScreen(context)

        val loading = Preference(context).apply {
            title = "Загрузка каталога..."
            summary = "Подключение к GitHub"
        }
        screen.addPreference(loading)
        preferenceScreen = screen

        // Загружаем список модов в фоне
        viewLifecycleOwner.lifecycleScope.launch {
            val mods = ModRepository.fetchMods()
            withContext(Dispatchers.Main) {
                if (!isAdded) return@withContext

                val newScreen = preferenceManager.createPreferenceScreen(context)

                if (mods.isEmpty()) {
                    val empty = Preference(context).apply {
                        title = "Каталог пуст"
                        summary = "Не удалось загрузить manifest.json или в нём нет модов"
                    }
                    newScreen.addPreference(empty)
                } else {
                    mods.forEach { mod ->
                        val pref = Preference(context).apply {
                            title = mod.name
                            summary = "${mod.description}\nВерсия: ${mod.version} • Автор: ${mod.author}"
                            setOnPreferenceClickListener {
                                installMod(mod)
                                true
                            }
                        }
                        newScreen.addPreference(pref)
                    }
                }

                preferenceScreen = newScreen
            }
        }
    }

    private fun installMod(mod: ModRepository.ModInfo) {
        val context = requireContext()

        Toast.makeText(context, "Скачиваю ${mod.name}...", Toast.LENGTH_SHORT).show()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val url = ModRepository.getDownloadUrl(mod)
                val pluginsDir = File(context.filesDir, "plugins")
                if (!pluginsDir.exists()) pluginsDir.mkdirs()

                val targetFile = File(pluginsDir, mod.file)

                withContext(Dispatchers.IO) {
                    URL(url).openStream().use { input ->
                        targetFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Мод ${mod.name} установлен. Перезапустите приложение, чтобы он появился в списке.",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.i(TAG, "Мод установлен: ${targetFile.absolutePath}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка установки мода: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
