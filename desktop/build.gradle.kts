import org.jetbrains.compose.desktop.application.dsl.TargetFormat


plugins {
    id(kotlinx.plugins.kotlin.jvm.get().pluginId)
    id(kotlinx.plugins.ksp.get().pluginId)
    id(libs.plugins.jetbrainCompose.get().pluginId)
    id("dev.icerock.mobile.multiplatform-resources")
    alias(kotlinx.plugins.compose.compiler)
}

dependencies {
    implementation(project(Modules.coreApi))
    implementation(project(Modules.sourceApi))
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
    implementation(libs.koin.core)
    implementation(libs.koin.compose)
    implementation(libs.voyager.navigator)
    implementation(kotlinx.coroutines.core)
    implementation(libs.napier)
    implementation(libs.coil.network.ktor)
    implementation(libs.coil.compose)
    
    // Add explicit MOKO resources dependency
    implementation(libs.moko.core)
}

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
        kotlin.srcDir(project.rootProject.file("i18n/build/generated/moko-resources/jvmMain/res/"))
    }
    sourceSets.test {
        kotlin.srcDir("build/generated/ksp/test/kotlin")
    }
}

// Add task dependency to ensure resources are generated before the desktop app builds
tasks.named("compileKotlin") {
    dependsOn(":i18n:generateMokoResources")
}

// Add task to copy drawable resources
tasks.register<Copy>("copyIconResources") {
    group = "resources"
    description = "Copies all drawable resources for desktop to ensure icons are available"
    
    // Source directories
    from(project.rootProject.file("i18n/src/commonMain/moko-resources/drawable"))
    into(project.buildDir.resolve("resources/main/drawable"))
    
    doLast {
        logger.lifecycle("Copied drawable resources to desktop build")
        // List all copied files to verify
        project.buildDir.resolve("resources/main/drawable").listFiles()?.forEach {
            logger.lifecycle("  - ${it.name}")
        }
    }
}

// Configure the compose desktop application
compose.desktop {
    application {
        // Copy resources before any tasks run
        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
            dependsOn("copyIconResources")
        }
        
        // Configure any additional dependencies here
        tasks.withType<JavaExec>().configureEach {
            dependsOn("copyIconResources")
        }
        
        // The rest of your application configuration stays the same
        mainClass = "ireader.desktop.MainKt"
        jvmArgs += listOf("-Xmx2G")  // Increase JVM memory
        
        nativeDistributions {
            // Package a JRE with the application
            includeAllModules = true
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
            
            // Package the application with its own JRE
            packageName = if (!isPreview) {
                "IReader"
            } else {
                "IReader-Preview"
            }
            
            // Ensure JRE is included
            includeAllModules = true
            
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

            // Windows-specific configuration for packaged JRE
            windows {
                dirChooser = true
                upgradeUuid = if (!isPreview) {
                    "B2ED947E-81E4-4258-8388-2B1EDF5E0A30"
                } else {
                    "7869504A-DB4D-45E8-AC6C-60C0360EA2F0"
                }
                shortcut = true
                menu = true
                iconFile.set(rootProject.file("desktop/src/main/resources/icon.ico"))
                menuGroup = "IReader"
                // Add this to help users with JRE issues
                console = true
            }
            macOS {
                bundleID = "ireaderorg.ireader"
                packageName = rootProject.name
                iconFile.set(rootProject.file("desktop/src/main/resources/icon.icns"))
            }
            linux {
                iconFile.set(rootProject.file("desktop/src/main/resources/icon.png"))
            }
        }
    }
}

val isPreview: Boolean
    get() = project.hasProperty("preview")
val previewCode: String
    get() = project.properties["preview"]?.toString()?.trim('"') ?: 0.toString()

// Add dependencies for package tasks
listOf(
    "packageDeb", 
    "packageDmg", 
    "packageExe", 
    "packageMsi", 
    "packageRpm",
    "packageDistributionForCurrentOS", 
    "packageUberJarForCurrentOS"
).forEach { taskName ->
    tasks.findByName(taskName)?.dependsOn("copyIconResources", "createWindowsLauncher")
}

// For release variants
listOf(
    "packageReleaseDeb", 
    "packageReleaseDmg", 
    "packageReleaseExe", 
    "packageReleaseMsi", 
    "packageReleaseRpm",
    "packageReleaseDistributionForCurrentOS", 
    "packageReleaseUberJarForCurrentOS"
).forEach { taskName ->
    tasks.findByName(taskName)?.dependsOn("copyIconResources", "createWindowsLauncher")
}

// Ensure resources are included in the distribution
tasks.whenTaskAdded {
    if (name.startsWith("package") || name.startsWith("dist") || name.contains("Resources")) {
        dependsOn("copyIconResources")
    }
}

// Make sure the WindowsLauncher is created before packaging
tasks.whenTaskAdded {
    if (name.startsWith("package") || name.startsWith("dist")) {
        dependsOn("createWindowsLauncher")
    }
}

// Create a batch file for Windows users to help with JRE troubleshooting
tasks.register<Copy>("createWindowsLauncher") {
    group = "build"
    description = "Creates a Windows batch file to check Java installation and launch the app"
    
    from(project.projectDir.resolve("src/main/resources/launch_template.bat"))
    into(project.buildDir.resolve("compose/binaries/main"))
    rename { "IReader_launcher.bat" }
    
    // Create batch file content with Java checks
    val content = """
@echo off
echo Checking Java installation...
java -version 2>NUL
if %ERRORLEVEL% NEQ 0 (
    echo Java is not installed or not in the PATH.
    echo Please install Java 17 or later from https://adoptium.net/
    echo or use the standalone version of IReader with packaged JRE.
    pause
    exit /b 1
)

echo Starting IReader...
start javaw -Xmx2G -jar IReader.jar
exit /b 0
"""
    
    doFirst {
        project.buildDir.resolve("compose/binaries/main").mkdirs()
        project.buildDir.resolve("compose/binaries/main/IReader_launcher.bat").writeText(content)
    }
}

// Add a task to verify and repair database
tasks.register<JavaExec>("verifyDatabase") {
    group = "database"
    description = "Verifies and repairs the IReader database"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("ireader.desktop.DatabaseVerifier")
}
