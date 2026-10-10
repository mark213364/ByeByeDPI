package io.github.romanvht.byedpi.mods

import android.content.Context
import io.github.romanvht.byedpi.api.StrategyPlugin
import kotlinx.coroutines.delay

/**
 * Обёртка над StrategyPlugin, чтобы он вёл себя как обычный Mod.
 */
class DexModAdapter(private val plugin: StrategyPlugin) : Mod {

    override val id: String = "dex_${plugin.id}"
    override val name: String = plugin.name
    override val description: String = plugin.description
    override val iconResId: Int = android.R.drawable.ic_menu_manage

    private var isRunning = false

    override suspend fun onEnable(context: Context) {
        plugin.onLoad(context)
        plugin.onEnable(context)
    }

    override suspend fun onDisable(context: Context) {
        plugin.onDisable(context)
        isRunning = false
    }

    override suspend fun run(context: Context) {
        isRunning = true
        // Плагин не имеет собственного цикла — просто ждём,
        // пока его не выключат. Сами рекомендации можно получить
        // через plugin.getRecommendedStrategies().
        while (isRunning) {
            delay(60_000)
        }
    }

    /** Возвращает стратегии, которые рекомендует плагин. */
    fun getRecommendedStrategies(): List<String> = plugin.getRecommendedStrategies()

    /** Сообщает плагину о результате теста стратегии. */
    fun reportTest(strategy: String, success: Boolean, latencyMs: Long) {
        plugin.onStrategyTested(strategy, success, latencyMs)
    }
}
