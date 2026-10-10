package io.github.romanvht.byedpi.mods

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

object ModManager {
    private const val PREFS_NAME = "mods_state"

    private lateinit var prefs: SharedPreferences
    private val mods = mutableListOf<Mod>()
    private val runningJobs = mutableMapOf<String, Job>()
    private val scope = CoroutineScope(Dispatchers.IO)

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        registerBuiltinMods()
        // Восстанавливаем состояние включённых модов
        mods.forEach { mod ->
            if (isEnabled(mod.id)) {
                scope.launch { mod.onEnable(context) }
            }
        }
    }

    private fun registerBuiltinMods() {
        // Здесь регистрируем все доступные моды
        mods.add(AutoStrategyMod())
        // mods.add(TelegramBotMod())
        // mods.add(SpeedTestMod())
    }

    fun getAllMods(): List<Mod> = mods

    fun isEnabled(modId: String): Boolean = prefs.getBoolean(modId, false)

    fun setEnabled(context: Context, mod: Mod, enabled: Boolean) {
        prefs.edit().putBoolean(mod.id, enabled).apply()

        if (enabled) {
            scope.launch {
                mod.onEnable(context)
                val job = launch { mod.run(context) }
                runningJobs[mod.id] = job
            }
        } else {
            runningJobs[mod.id]?.cancel()
            runningJobs.remove(mod.id)
            scope.launch { mod.onDisable(context) }
        }
    }
}
