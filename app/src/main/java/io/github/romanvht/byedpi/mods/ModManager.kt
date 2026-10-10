package io.github.romanvht.byedpi.mods

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

object ModManager {
    private const val PREFS_NAME = "mods_state"

    private var prefs: SharedPreferences? = null
    private val mods = mutableListOf<Mod>()
    private val runningJobs = mutableMapOf<String, Job>()
    private val scope = CoroutineScope(Dispatchers.IO)
    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        registerBuiltinMods()

        mods.forEach { mod ->
            if (isEnabled(mod.id)) {
                scope.launch {
                    mod.onEnable(context.applicationContext)
                    val job = launch { mod.run(context.applicationContext) }
                    runningJobs[mod.id] = job
                }
            }
        }
        initialized = true
    }

    private fun registerBuiltinMods() {
        mods.clear()
        mods.add(AutoStrategyMod())
    }

    fun getAllMods(): List<Mod> = mods

    fun isEnabled(modId: String): Boolean = prefs?.getBoolean(modId, false) ?: false

    fun setEnabled(context: Context, mod: Mod, enabled: Boolean) {
        prefs?.edit()?.putBoolean(mod.id, enabled)?.apply()

        if (enabled) {
            scope.launch {
                mod.onEnable(context.applicationContext)
                val job = launch { mod.run(context.applicationContext) }
                runningJobs[mod.id] = job
            }
        } else {
            runningJobs[mod.id]?.cancel()
            runningJobs.remove(mod.id)
            scope.launch { mod.onDisable(context.applicationContext) }
        }
    }
}
