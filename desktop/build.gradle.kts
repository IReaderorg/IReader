
import org.gradle.jvm.tasks.Jar
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id(kotlinx.plugins.kotlin.jvm.get().pluginId)
    id(libs.plugins.ksp.get().pluginId)
    id(libs.plugins.jetbrainCompose.get().pluginId)
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

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = ProjectConfig.desktopJvmTarget.toString()
            freeCompilerArgs = listOf(
                    "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi"
            )
        }
    }
    withType<Jar> {
        exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    }
}

val isPreview: Boolean
    get() = project.hasProperty("preview")
val previewCode: String
    get() = project.properties["preview"]?.toString()?.trim('"') ?: 0.toString()
compose.desktop {
    application {
        mainClass = "ireader.desktop.MainKt"
        nativeDistributions {
            targetFormats(
                    // Windows
                    TargetFormat.Msi,
                    TargetFormat.Exe,
                    // Linux
                    TargetFormat.Deb,
                    TargetFormat.Rpm,
                    // MacOS
                    TargetFormat.Dmg
            )
            modules(
                    "java.compiler",
                    "java.instrument",
                    "java.management",
                    "java.naming",
                    "java.prefs",
                    "java.rmi",
                    "java.scripting",
                    "java.sql",
                    "jdk.crypto.ec",
                    "jdk.unsupported"
            )

            packageName = if (!isPreview) {
                "IReader"
            } else {
                "IReader-Preview"
            }
            description = "IReader"
            copyright = "Mozilla Public License v2.0"
            vendor = "kazemcodes"
            if (isPreview) {
                packageVersion = "${version.toString().substringBeforeLast('.')}.$previewCode"
            }

            args(project.projectDir.absolutePath)
            buildTypes.release.proguard {
                version.set(libs.versions.proguard.get())
                configurationFiles.from("proguard-rules.pro")
            }

            windows {
                dirChooser = true
                upgradeUuid = if (!isPreview) {
                    "B2ED947E-81E4-4258-8388-2B1EDF5E0A30"
                } else {
                    "7869504A-DB4D-45E8-AC6C-60C0360EA2F0"
                }
                shortcut = true
                menu = true
                iconFile.set(rootProject.file("resources/icon.ico"))
                menuGroup = "IReader"
            }
            macOS {
                bundleID = "ireaderorg.ireader"
                packageName = rootProject.name
                iconFile.set(rootProject.file("resources/icon.icns"))
            }
            linux {
                iconFile.set(rootProject.file("resources/icon.png"))
            }
        }
    }
}