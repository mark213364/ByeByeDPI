package io.github.romanvht.byedpi.api

import android.content.Context

interface StrategyPlugin {
    val id: String
    val name: String
    val description: String
    val version: String
    val author: String
        get() = "Unknown"

    fun onLoad(context: Context)
    fun onEnable(context: Context)
    fun onDisable(context: Context)
    fun getRecommendedStrategies(): List<String>
    fun onStrategyTested(strategy: String, success: Boolean, latencyMs: Long)
}
