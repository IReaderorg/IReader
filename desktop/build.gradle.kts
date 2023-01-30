plugins {
    id(kotlinx.plugins.kotlin.jvm.get().pluginId)
    id(libs.plugins.ksp.get().pluginId)
    id(libs.plugins.compose.get().pluginId)
    id(libs.plugins.kotlinter.get().pluginId)
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.uiTooling)
    implementation(compose.materialIconsExtended)
    implementation(compose.foundation)
    @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
    implementation(compose.material3)
    implementation(compose.animation)
    implementation(compose.animationGraphics)
    implementation(compose.runtime)
    implementation(compose.ui)
}


java {
    sourceCompatibility = ProjectConfig.desktopJvmTarget
    targetCompatibility = ProjectConfig.desktopJvmTarget
}
kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
    sourceSets.test {
        kotlin.srcDir("build/generated/ksp/test/kotlin")
    }
}

compose.desktop {
    application {
        mainClass = "ireader.desktop.MainKt"
    }

}
