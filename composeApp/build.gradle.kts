import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)

    // 1. ADD SERIALIZATION PLUGIN
    // NOTE: The version (2.1.0) should match your Kotlin version.
    // If you are using Kotlin 2.0.20, change this to "2.0.20"
    kotlin("plugin.serialization") version "2.1.0"
}

kotlin {
    jvm()

    js {
        browser()
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        // Use stable version 3.0.1
        val ktorVersion = "3.0.1"

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)

            // Ktor Core & WebSockets
            implementation("io.ktor:ktor-client-core:$ktorVersion")
            implementation("io.ktor:ktor-client-websockets:$ktorVersion")

            // 2. ADD SERIALIZATION JSON LIBRARY
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)

            // JVM Engine
            implementation("io.ktor:ktor-client-cio:$ktorVersion")
        }

        jsMain.dependencies {
            // JS Engine
            implementation("io.ktor:ktor-client-js:$ktorVersion")
        }

        @OptIn(ExperimentalWasmDsl::class)
        wasmJsMain.dependencies {
            // Wasm Engine (uses the JS bridge internally in Ktor 3.x)
            implementation("io.ktor:ktor-client-js:$ktorVersion")
        }
    }
}

compose.desktop {
    application {
        mainClass = "org.tabletennis.project.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.tabletennis.project"
            packageVersion = "1.0.0"
        }
    }
}