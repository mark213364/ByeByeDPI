package io.github.romanvht.byedpi.mods

import kotlin.random.Random

/**
 * Генетические операции над стратегиями ciadpi.
 */
object StrategyMutator {

    /**
     * Скрещивает две стратегии: берёт часть флагов от первой, часть от второй.
     */
    fun crossover(a: String, b: String): String {
        val tokensA = a.split(" ").filter { it.isNotBlank() }
        val tokensB = b.split(" ").filter { it.isNotBlank() }

        if (tokensA.isEmpty() || tokensB.isEmpty()) return a

        val splitA = Random.nextInt(1, tokensA.size + 1)
        val splitB = Random.nextInt(1, tokensB.size + 1)

        val result = mutableListOf<String>()
        result.addAll(tokensA.take(splitA))

        // Добавляем флаги из B, которых нет в A
        tokensB.drop(splitB).forEach { flag ->
            if (!result.contains(flag)) result.add(flag)
        }

        return result.joinToString(" ")
    }

    /**
     * Мутация: случайно меняем один числовой параметр в стратегии.
     */
    fun mutate(strategy: String): String {
        val tokens = strategy.split(" ").toMutableList()
        if (tokens.isEmpty()) return strategy

        // Выбираем случайный токен
        val index = Random.nextInt(tokens.size)
        val token = tokens[index]

        // Пытаемся найти число и изменить его
        val mutated = mutateToken(token)
        if (mutated != null) {
            tokens[index] = mutated
        } else {
            // Если мутировать нечего — просто вернём оригинал
            return strategy
        }

        return tokens.joinToString(" ")
    }

    /**
     * Меняет числовой параметр в токене. Например, "-s3" → "-s5", "-d1:11" → "-d2:11".
     */
    private fun mutateToken(token: String): String? {
        // Паттерн: -<буква><число>, возможно с : или +
        val regex = Regex("^(-[A-Za-z])(\\d+)(.*)$")
        val match = regex.find(token) ?: return null

        val prefix = match.groupValues[1]
        val number = match.groupValues[2].toIntOrNull() ?: return null
        val suffix = match.groupValues[3]

        // Меняем число на ±1-3
        val delta = Random.nextInt(-3, 4)
        if (delta == 0) return null

        val newNumber = (number + delta).coerceAtLeast(1)
        return "$prefix$newNumber$suffix"
    }

    /**
     * Создаёт новое поколение на основе лучших стратегий.
     *
     * @param parents топ-N стратегий для скрещивания
     * @param populationSize сколько новых стратегий создать
     */
    fun buildNextGeneration(parents: List<String>, populationSize: Int): List<String> {
        if (parents.isEmpty()) return emptyList()
        if (parents.size == 1) {
            // только мутации
            return List(populationSize) { mutate(parents[0]) }
        }

        val result = mutableListOf<String>()

        // 30% — скрещивание
        val crossoverCount = (populationSize * 0.3).toInt()
        repeat(crossoverCount) {
            val a = parents.random()
            val b = parents.random()
            result.add(crossover(a, b))
        }

        // 70% — мутации
        while (result.size < populationSize) {
            val parent = parents.random()
            result.add(mutate(parent))
        }

        return result
    }
}
