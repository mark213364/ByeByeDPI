package io.github.romanvht.byedpi.data

data class Configuration(
    val args: List<String>,
    val host: String,
    val port: Int,
    val dns: String = "",
    val ipv6: Boolean = false,
    val appListType: String = "disable",
    val apps: List<String> = emptyList(),

    // ===== НОВЫЕ ПОЛЯ ДЛЯ ОПТИМИЗАЦИИ =====
    /** Размер буфера recv/send для ciadpi. 0 = не задавать (используется дефолт 16384). */
    val bufferSize: Int = 0,
    /** Максимум соединений. 0 = не задавать (дефолт 512). */
    val maxConnections: Int = 0,
    /** TCP Fast Open. */
    val tcpFastOpen: Boolean = false,
    /** MTU для hev-socks5-tunnel. 0 = не задавать (дефолт 8500). */
    val tunnelMtu: Int = 0
)
