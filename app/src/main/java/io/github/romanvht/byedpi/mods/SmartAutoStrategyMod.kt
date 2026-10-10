package io.github.romanvht.byedpi.mods

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import io.github.romanvht.byedpi.data.StrategyResult
import io.github.romanvht.byedpi.services.TestService
import io.github.romanvht.byedpi.utility.getPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class SmartAutoStrategyMod : Mod {

    override val id = "smart_auto_strategy"
    override val name = "Умный автоподбор"
    override val description = "Комбинирует флаги лучших стратегий и перебирает варианты без дубликатов"
    override val iconResId = android.R.drawable.ic_menu_manage

    private var isRunning = false

    companion object {
        private const val TAG = "SmartAutoStrategyMod"
        private const val GENERATIONS = 4
        private const val TOP_PARENTS = 5
        private const val COMBINATIONS_PER_GEN = 20
        private const val BASE_LIMIT = 40
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
        delay(30_000)

        while (isRunning) {
            try {
                Log.i(TAG, "=== Новый цикл ===")

                var bestStrategy: String? = null
                var bestSuccess = 0

                val allBase = loadBaseStrategies(context)
                Log.i(TAG, "Загружено базовых: ${allBase.size}")

                var currentPool = StrategyMutator.deduplicate(allBase).take(BASE_LIMIT)
                Log.i(TAG, "Базовый пул: ${currentPool.size}")

                for (generation in 0 until GENERATIONS) {
                    if (!isRunning) break

                    Log.i(TAG, "--- Поколение $generation (стратегий: ${currentPool.size}) ---")

                    val results = runTestAndGetResults(context, currentPool)
                    if (results.isEmpty()) {
                        Log.w(TAG, "Нет результатов")
                        break
                    }

                    val sorted = results
                        .filter { it.totalRequests > 0 }
                        .sortedByDescending { it.successPercentage }

                    if (sorted.isEmpty()) break

                    val top = sorted.first()
                    Log.i(TAG, "Лучшая в поколении: ${top.command} (${top.successPercentage}%)")

                    if (top.successPercentage > bestSuccess) {
                        bestSuccess = top.successPercentage
                        bestStrategy = top.command
                    }

                    if (bestSuccess >= 100) {
                        Log.i(TAG, "Достигнуто 100%, стоп")
                        break
                    }

                    val parents = sorted.take(TOP_PARENTS).map { it.command }
                    if (parents.isEmpty()) break

                    val parsedParents = parents.map { StrategyMutator.parse(it) }
                    val valuePool = StrategyMutator.collectValues(allBase)
                    Log.i(TAG, "Групп: ${valuePool.size}, значений: ${valuePool.values.sumOf { it.size }}")

                    val newPool = mutableListOf<String>()

                    val combos = StrategyMutator.buildCombinations(
                        valuePool = valuePool,
                        maxCombinations = COMBINATIONS_PER_GEN / 2,
                        preferGroups = parsedParents.flatMap { it.groups.keys }.distinct()
                    )
                    newPool.addAll(combos)

                    while (newPool.size < COMBINATIONS_PER_GEN && parsedParents.size >= 2) {
                        val a = parsedParents.random()
                        val b = parsedParents.random()
                        val child = StrategyMutator.crossover(a, b)
                        newPool.add(child.toCommand())
                    }

                    currentPool = StrategyMutator.deduplicate(newPool)
                    Log.i(TAG, "Новое поколение: ${currentPool.size} (после дедупликации)")
                }

                if (bestStrategy != null && bestSuccess > 0) {
                    Log.i(TAG, "Применяю: $bestStrategy ($bestSuccess%)")
                    withContext(Dispatchers.Main) {
                        context.getPreferences().edit {
                            putString("byedpi_cmd_args", bestStrategy)
                        }
                    }
                } else {
                    Log.w(TAG, "Рабочих стратегий не найдено")
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

    private suspend fun runTestAndGetResults(
        context: Context,
        strategies: List<String>
    ): List<StrategyResult> {
        context.getPreferences().edit {
            putBoolean("byedpi_proxytest_usercommands", true)
            putString("byedpi_proxytest_commands", strategies.joinToString("\n"))
        }

        TestService.loadResults(context)
        delay(500)

        if (!TestService.isRunning) {
            TestService.start(context)
            Log.i(TAG, "TestService запущен")
        }

        withTimeoutOrNull(20 * 60 * 1000) {
            TestService.awaitStopped()
        }

        return TestService.state.value.strategies
    }

    private fun loadBaseStrategies(context: Context): List<String> {
        return try {
            context.assets.open("proxytest_strategies.list")
                .bufferedReader()
                .use { it.readText() }
                .lines()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        } catch (e: Exception) {
            Log.e(TAG, "Не удалось загрузить: ${e.message}", e)
            emptyList()
        }
    }
}
