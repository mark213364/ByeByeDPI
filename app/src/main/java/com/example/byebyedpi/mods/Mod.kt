package com.example.byebyedpi.mods

import android.content.Context

interface Mod {
    /** Уникальный ID мода */
    val id: String

    /** Имя для отображения */
    val name: String

    /** Краткое описание */
    val description: String

    /** Иконка (ресурс) */
    val iconResId: Int

    /** Есть ли у мода настройки */
    val hasSettings: Boolean
        get() = false

    /** Вызывается при включении мода */
    suspend fun onEnable(context: Context)

    /** Вызывается при выключении мода */
    suspend fun onDisable(context: Context)

    /** Основная логика мода (запускается, пока он включён) */
    suspend fun run(context: Context)

    /** Открыть экран настроек мода (если hasSettings = true) */
    fun openSettings(context: Context) {}
}
