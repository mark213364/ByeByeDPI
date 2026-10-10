package io.github.romanvht.byedpi.mods

import android.content.Context
import android.util.Log
import io.github.romanvht.byedpi.data.StrategyResult
import io.github.romanvht.byedpi.services.TestService
import io.github.romanvht.byedpi.utility.getPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import androidx.core.content.edit
import java.io.File

/**
 * Мод с генетическим алгоритмом подбора стратегий.
 *
 * Работает так:
 * 1. Запускает TestService на базовом наборе стратегий.
 * 2. Берёт топ-5 по successPercentage.
 * 3. Скрещивает и мутирует — получает новое поколение.
 * 4. Тестирует новое поколение.
 * 5. Повторяет 3–5 поколений.
 * 6. Применяет лучшую стратегию.
 */
class SmartAutoStrategyMod : Mod {

    override val id = "smart_auto_strategy"
    override val name = "Умный автоподбор"
    override val description = "Генетический алгоритм: комбинирует и мутирует лучшие стратегии"
    override val iconResId = android.R.drawable.ic_menu_manage

    private var isRunning = false

    companion object {
        private const val TAG = "SmartAutoStrategyMod"
        private const val GENERATIONS = 3
        private const val TOP_PARENTS = 5
        private const val POPULATION_SIZE = 10
    }

    override suspend fun onEnable(context: Context) {
        Log.i(TAG, "Умный автоподбор включён")
    }

    override suspend fun onDisable(context: Context) {
        Log.i(TAG, "Умный автоподбор выключен")
        isRunning = false
        if (TestService.isRunning) TestService.stop()
    }

    override suspend fun run(context: Context) {
        isRunning = true

        // Ждём, чтобы не мешать пользователю при запуске приложения
        delay(30_000)

        while (isRunning) {
            try {
                Log.i(TAG, "=== Новый цикл умного автоподбора ===")

                var bestStrategy: String? = null
                var bestSuccess = 0

                // === Поколение 0: базовые стратегии ===
                var currentPool = loadBaseStrategies(context)
                Log.i(TAG, "Базовых стратегий: ${currentPool.size}")

                for (generation in 0 until GENERATIONS) {
                    if (!isRunning) break

                    Log.i(TAG, "--- Поколение $generation (стратегий: ${currentPool.size}) ---")

                    val results = runTestAndGetResults(context, currentPool)

                    if (results.isEmpty()) {
                        Log.w(TAG, "Нет результатов теста, прерываю")
                        break
                    }

                    // Сортируем по successPercentage
                    val sorted = results
                        .filter { it.totalRequests > 0 }
                        .sortedByDescending { it.successPercentage }

                    if (sorted.isEmpty()) break

                    val top = sorted.first()
                    Log.i(TAG, "Лучшая в поколении $generation: ${top.command} (${top.successPercentage}%)")

                    if (top.successPercentage > bestSuccess) {
                        bestSuccess = top.successPercentage
                        bestStrategy = top.command
                    }

                    // Если 100% — дальше искать не нужно
                    if (bestSuccess >= 100) {
                        Log.i(TAG, "Достигнуто 100%, останавливаюсь")
                        break
                    }

                    // Берём топ-5 как родителей
                    val parents = sorted.take(TOP_PARENTS).map { it.command }
                    if (parents.isEmpty()) break

                    // Создаём следующее поколение
                    currentPool = StrategyMutator.buildNextGeneration(parents, POPULATION_SIZE)
                    Log.i(TAG, "Создано новое поколение: ${currentPool.size} стратегий")
                }

                // === Применяем лучшую ===
                if (bestStrategy != null && bestSuccess > 0) {
                    Log.i(TAG, "Применяю лучшую: $bestStrategy ($bestSuccess%)")
                    withContext(Dispatchers.Main) {
                        context.getPreferences().edit {
                            putString("byedpi_cmd_args", bestStrategy)
                        }
                    }
                } else {
                    Log.w(TAG, "Не удалось найти рабочую стратегию")
                }

                Log.i(TAG, "=== Цикл завершён, жду 3 часа ===")
                delay(3 * 60 * 60 * 1000)

            } catch (e: kotlinx.coroutines.CancellationException) {
                Log.i(TAG, "Цикл отменён")
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка: ${e.message}", e)
                delay(60 * 1000)
            }
        }
    }

    /**
     * Запускает TestService с заданным списком стратегий и возвращает результаты.
     */
    private suspend fun runTestAndGetResults(
        context: Context,
        strategies: List<String>
    ): List<StrategyResult> {
        // Записываем стратегии во временные настройки TestService
        context.getPreferences().edit {
            putBoolean("byedpi_proxytest_usercommands", true)
            putString("byedpi_proxytest_commands", strategies.joinToString("\n"))
        }

        // Запускаем тест
        TestService.loadResults(context)
        delay(500)

        if (!TestService.isRunning) {
            TestService.start(context)
            Log.i(TAG, "TestService запущен")
        }

        // Ждём завершения (до 15 минут)
        withTimeoutOrNull(15 * 60 * 1000) {
            TestService.awaitStopped()
        }

        val state = TestService.state.value
        return state.strategies
    }

    /**
     * Загружает базовые стратегии из assets/proxytest_strategies.list.
     */
    private fun loadBaseStrategies(context: Context): List<String> {
        return try {
            context.assets.open("proxytest_strategies.list")
                .bufferedReader()
                .use { it.readText() }
                .lines()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        } catch (e: Exception) {
            Log.e(TAG, "Не удалось загрузить базовые стратегии: ${e.message}", e)
            emptyList()
        }
    }
}
