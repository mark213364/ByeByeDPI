package io.github.romanvht.byedpi.mods

import android.content.Context
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class AutoStrategyMod : Mod {
    override val id = "auto_strategy"
    override val name = "Автоподбор стратегий"
    override val description = "Автоматически тестирует стратегии и выбирает лучшую"
    override val iconResId = android.R.drawable.ic_menu_manage

    private var isRunning = false

    override suspend fun onEnable(context: Context) {
        Log.i("AutoStrategyMod", "Мод включён")
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "AutoStrategyMod: мод включён", Toast.LENGTH_LONG).show()
        }
    }

    override suspend fun onDisable(context: Context) {
        Log.i("AutoStrategyMod", "Мод выключен")
        isRunning = false
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "AutoStrategyMod: мод выключен", Toast.LENGTH_SHORT).show()
        }
    }

    override suspend fun run(context: Context) {
        isRunning = true
        while (isRunning) {
            try {
                Log.i("AutoStrategyMod", "Запуск цикла тестирования стратегий...")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "AutoStrategyMod: начало цикла", Toast.LENGTH_SHORT).show()
                }

                // TODO: здесь будет реальная логика:
                // 1. Получить список стратегий (флаги для ciadpi)
                // 2. Для каждой запустить ciadpi на локальном порту
                // 3. Проверить доступ к тестовому URL через SOCKS5
                // 4. Замерить latency
                // 5. Сохранить лучшую стратегию в SharedPreferences

                val strategies = listOf(
                    "--split 1 --disorder 3+s",
                    "--split 2 --disorder 5",
                    "--fake -1 --ttl 8",
                    "--split 1+s --oob"
                )

                for (strategy in strategies) {
                    if (!isRunning) break
                    Log.i("AutoStrategyMod", "Тестирую стратегию: $strategy")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Тест: $strategy", Toast.LENGTH_SHORT).show()
                    }
                    delay(2000)
                }

                Log.i("AutoStrategyMod", "Цикл завершён, жду час...")
                delay(60 * 60 * 1000) // раз в час
            } catch (e: Exception) {
                Log.e("AutoStrategyMod", "Ошибка в цикле: ${e.message}", e)
                delay(60 * 1000) // при ошибке — пауза 1 минута
            }
        }
    }
}
