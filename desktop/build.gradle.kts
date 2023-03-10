plugins {
    id(kotlinx.plugins.kotlin.jvm.get().pluginId)
    id(libs.plugins.ksp.get().pluginId)
    id(libs.plugins.compose.get().pluginId)
    id(libs.plugins.kotlinter.get().pluginId)
}

dependencies {
    implementation(project(Modules.coreApi))
    implementation(project(Modules.data))

    implementation(project(Modules.domain))

    implementation(project(Modules.presentation))
    implementation(project(Modules.commonResources))
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
    implementation(libs.kodein.core)
    implementation(libs.kodein.compose)
    implementation(libs.voyager.navigator)
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

compose {
    desktop {
    application {
        mainClass = "ireader.desktop.MainKt"
        nativeDistributions {
            targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg, org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi, org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb)
            packageName = "IReader"
            version = "1.0.0"
        }
    }

}
}