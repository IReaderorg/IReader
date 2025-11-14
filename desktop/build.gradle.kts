import org.jetbrains.compose.desktop.application.dsl.TargetFormat


plugins {
    id(kotlinx.plugins.kotlin.jvm.get().pluginId)
    id(kotlinx.plugins.ksp.get().pluginId)
    id(libs.plugins.jetbrainCompose.get().pluginId)
    id("dev.icerock.mobile.multiplatform-resources")
    alias(kotlinx.plugins.compose.compiler)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(24))
    }
}

dependencies {
    implementation(project(Modules.coreApi))
    implementation(project(Modules.sourceApi))
    implementation(project(Modules.data))

    implementation(project(Modules.domain))

    implementation(project(Modules.presentation))
    implementation(project(Modules.commonResources))
    implementation(compose.desktop.currentOs)
    // Removed duplicate compose dependencies - already provided by presentation module
    // Only keeping desktop-specific dependencies
    implementation(libs.napier)
    implementation(libs.voyager.navigator)

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
    into(project.layout.buildDirectory.get().asFile.resolve("resources/main/drawable"))
    
    doLast {
        logger.lifecycle("Copied drawable resources to desktop build")
        // List all copied files to verify
        project.layout.buildDirectory.get().asFile.resolve("resources/main/drawable").listFiles()?.forEach {
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
        
        // Supabase configuration - Multi-endpoint support
        // Load from environment variables or system properties
        val supabaseUrl = System.getenv("SUPABASE_URL") ?: System.getProperty("supabase.url", "")
        val supabaseAnonKey = System.getenv("SUPABASE_ANON_KEY") ?: System.getProperty("supabase.anon.key", "")
        val supabaseBooksUrl = System.getenv("SUPABASE_BOOKS_URL") ?: System.getProperty("supabase.books.url", "")
        val supabaseBooksKey = System.getenv("SUPABASE_BOOKS_KEY") ?: System.getProperty("supabase.books.key", "")
        val supabaseProgressUrl = System.getenv("SUPABASE_PROGRESS_URL") ?: System.getProperty("supabase.progress.url", "")
        val supabaseProgressKey = System.getenv("SUPABASE_PROGRESS_KEY") ?: System.getProperty("supabase.progress.key", "")
        
        jvmArgs += listOf(
            "-Xmx2G",  // Increase JVM memory
            "-Dsupabase.url=$supabaseUrl",
            "-Dsupabase.anon.key=$supabaseAnonKey",
            "-Dsupabase.books.url=$supabaseBooksUrl",
            "-Dsupabase.books.key=$supabaseBooksKey",
            "-Dsupabase.progress.url=$supabaseProgressUrl",
            "-Dsupabase.progress.key=$supabaseProgressKey"
        )
        
        nativeDistributions {
            // Include native libraries in the distribution
            // This ensures Piper TTS native libraries are packaged with the application
            appResourcesRootDir.set(project.layout.projectDirectory.dir("src/main/resources"))
            
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
            
            // Specify required modules instead of including all modules
            // This prevents jlink errors with Java 24
            modules(
                "java.base",
                "java.desktop",
                "java.logging",
                "java.naming",
                "java.net.http",
                "java.prefs",
                "java.sql",
                "java.xml",
                "jdk.crypto.ec",
                "jdk.unsupported"
            )
            
            description = "IReader"
            copyright = "Mozilla Public License v2.0"
            vendor = "kazemcodes"
            
            // Set proper package version (max 3 components for native distributions)
            packageVersion = ProjectConfig.versionName

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
    into(project.layout.buildDirectory.get().asFile.resolve("compose/binaries/main"))
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
        project.layout.buildDirectory.get().asFile.resolve("compose/binaries/main").mkdirs()
        project.layout.buildDirectory.get().asFile.resolve("compose/binaries/main/IReader_launcher.bat").writeText(content)
    }
}

// Add a task to verify and repair database
tasks.register<JavaExec>("verifyDatabase") {
    group = "database"
    description = "Verifies and repairs the IReader database"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("ireader.desktop.DatabaseVerifier")
}

// Task to verify native libraries are present
tasks.register("verifyNativeLibraries") {
    group = "verification"
    description = "Verifies that Piper TTS native libraries are present in resources"
    
    doLast {
        val nativeDir = project.file("src/main/resources/native")
        val platforms = listOf("windows-x64", "macos-x64", "macos-arm64", "linux-x64")
        val requiredLibs = mapOf(
            "windows-x64" to listOf("piper_jni.dll", "onnxruntime.dll"),
            "macos-x64" to listOf("libpiper_jni.dylib", "libonnxruntime.dylib"),
            "macos-arm64" to listOf("libpiper_jni.dylib", "libonnxruntime.dylib"),
            "linux-x64" to listOf("libpiper_jni.so", "libonnxruntime.so")
        )
        
        var allPresent = true
        platforms.forEach { platform ->
            val platformDir = nativeDir.resolve(platform)
            logger.lifecycle("Checking $platform:")
            requiredLibs[platform]?.forEach { lib ->
                val libFile = platformDir.resolve(lib)
                if (libFile.exists()) {
                    logger.lifecycle("  ✓ $lib (${libFile.length()} bytes)")
                } else {
                    logger.warn("  ✗ $lib (missing)")
                    allPresent = false
                }
            }
        }
        
        if (!allPresent) {
            logger.warn("")
            logger.warn("WARNING: Some native libraries are missing.")
            logger.warn("Piper TTS will fall back to simulation mode if libraries are not available.")
            logger.warn("See desktop/src/main/resources/native/README.md for instructions.")
        } else {
            logger.lifecycle("")
            logger.lifecycle("✓ All native libraries are present")
        }
    }
}

// Run verification before packaging
tasks.whenTaskAdded {
    if (name.startsWith("package") || name.startsWith("createDistributable")) {
        dependsOn("verifyNativeLibraries")
    }
}
