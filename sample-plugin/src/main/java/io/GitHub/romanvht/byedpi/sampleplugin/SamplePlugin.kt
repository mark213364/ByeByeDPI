package io.github.romanvht.byedpi.sampleplugin

import android.content.Context
import android.util.Log
import io.github.romanvht.byedpi.api.StrategyPlugin

/**
 * Пример плагина.
 *
 * Он реализует интерфейс StrategyPlugin и возвращает
 * набор рекомендованных стратегий для тестирования.
 */
class SamplePlugin : StrategyPlugin {

    override val id: String = "sample_plugin"
    override val name: String = "Пример плагина"
    override val description: String = "Демонстрирует работу системы плагинов"
    override val version: String = "1.0.0"
    override val author: String = "mark213364"

    override fun onLoad(context: Context) {
        Log.i("SamplePlugin", "Плагин загружен")
    }

    override fun onEnable(context: Context) {
        Log.i("SamplePlugin", "Плагин включён")
    }

    override fun onDisable(context: Context) {
        Log.i("SamplePlugin", "Плагин выключен")
    }

    override fun getRecommendedStrategies(): List<String> {
        return listOf(
            "--split 1 --disorder 3+s",
            "--split 2 --disorder 5",
            "--fake -1 --ttl 8",
            "--split 1+s --oob"
        )
    }

    override fun onStrategyTested(strategy: String, success: Boolean, latencyMs: Long) {
        Log.i(
            "SamplePlugin",
            "Результат теста: $strategy — success=$success, latency=${latencyMs}ms"
        )
    }
}
