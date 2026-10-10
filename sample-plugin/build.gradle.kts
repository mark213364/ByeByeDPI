plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "io.github.romanvht.byedpi.sampleplugin"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // API доступен только на этапе компиляции — в .dex плагина он не попадёт,
    // потому что уже есть в основном приложении.
    compileOnly(project(":plugin-api"))
}
