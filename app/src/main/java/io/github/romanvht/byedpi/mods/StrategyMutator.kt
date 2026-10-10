package io.github.romanvht.byedpi.mods

import kotlin.random.Random

/**
 * Разбирает стратегии на группы флагов, строит комбинации,
 * скрещивает по позициям и удаляет дубликаты.
 */
object StrategyMutator {

    /**
     * Одна стратегия в виде набора групп.
     * Например: "d" -> "-d1", "s" -> "-s3+s", "a" -> "-a1".
     */
    data class ParsedStrategy(val groups: Map<String, String>) {
        fun toCommand(): String = groups.values.joinToString(" ")
    }

    /**
     * Парсит стратегию в набор групп.
     * -d1 → группа "d"
     * -s3+s → группа "s"
     * -As → группа "As"
     */
    fun parse(strategy: String): ParsedStrategy {
        val tokens = strategy.split(" ").filter { it.isNotBlank() }
        val groups = mutableMapOf<String, String>()
        for (token in tokens) {
            val key = extractGroupKey(token) ?: continue
            groups[key] = token
        }
        return ParsedStrategy(groups)
    }

    private fun extractGroupKey(token: String): String? {
        if (!token.startsWith("-")) return null
        val match = Regex("^-([A-Za-z]+)").find(token) ?: return null
        return match.groupValues[1]
    }

    /**
     * Собирает все уникальные значения по каждой группе.
     */
    fun collectValues(strategies: List<String>): Map<String, List<String>> {
        val values = mutableMapOf<String, MutableSet<String>>()
        for (strategy in strategies) {
            val parsed = parse(strategy)
            for ((key, value) in parsed.groups) {
                values.getOrPut(key) { mutableSetOf() }.add(value)
            }
        }
        return values.mapValues { it.value.toList() }
    }

    /**
     * Строит комбинации: по одному значению из каждой группы.
     * Дедуплицирует результат.
     */
    fun buildCombinations(
        valuePool: Map<String, List<String>>,
        maxCombinations: Int = 200,
        preferGroups: List<String> = emptyList()
    ): List<String> {
        if (valuePool.isEmpty()) return emptyList()

        val groupKeys = valuePool.keys.sortedWith(
            compareBy(
                { if (it in preferGroups) 0 else 1 },
                { it }
            )
        )

        val result = mutableListOf<String>()
        val seen = mutableSetOf<String>()

        var attempts = 0
        while (result.size < maxCombinations && attempts < maxCombinations * 20) {
            attempts++
            val combo = mutableMapOf<String, String>()
            for (key in groupKeys) {
                val values = valuePool[key] ?: continue
                if (values.isEmpty()) continue
                combo[key] = values.random()
            }
            val command = groupKeys.mapNotNull { combo[it] }.joinToString(" ")
            if (command.isNotBlank() && seen.add(command)) {
                result.add(command)
            }
        }
        return result
    }

    /**
     * Покоординатное скрещивание двух родителей.
     */
    fun crossover(a: ParsedStrategy, b: ParsedStrategy): ParsedStrategy {
        val allKeys = (a.groups.keys + b.groups.keys).distinct()
        val child = mutableMapOf<String, String>()

        for (key in allKeys) {
            val valueA = a.groups[key]
            val valueB = b.groups[key]
            val chosen = when {
                valueA != null && valueB != null -> if (Random.nextBoolean()) valueA else valueB
                valueA != null -> valueA
                valueB != null -> valueB
                else -> null
            }
            if (chosen != null) child[key] = chosen
        }

        return ParsedStrategy(child)
    }

    /**
     * Удаляет дубликаты в списке стратегий.
     */
    fun deduplicate(strategies: List<String>): List<String> {
        val seen = mutableSetOf<String>()
        return strategies.map { it.trim() }.filter { it.isNotEmpty() && seen.add(it) }
    }
}
