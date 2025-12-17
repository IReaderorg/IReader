import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.util.Properties


plugins {
    id(kotlinx.plugins.kotlin.jvm.get().pluginId)
    id(kotlinx.plugins.ksp.get().pluginId)
    id(libs.plugins.jetbrainCompose.get().pluginId)
    alias(kotlinx.plugins.compose.compiler)
}

// Load local.properties for local development
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { stream -> load(stream) }
    }
}

// Load config.properties as fallback
val configProperties = Properties().apply {
    val configPropertiesFile = rootProject.file("config.properties")
    if (configPropertiesFile.exists()) {
        configPropertiesFile.inputStream().use { stream -> load(stream) }
    }
}

// Helper function to get property with fallback chain
fun getConfigProperty(envVar: String, propertyKey: String): String {
    return System.getenv(envVar)
        ?: localProperties.getProperty(propertyKey)
        ?: configProperties.getProperty(propertyKey)
        ?: project.findProperty(propertyKey) as? String
        ?: ""
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
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
    implementation(libs.kermit)
}

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
    sourceSets.test {
        kotlin.srcDir("build/generated/ksp/test/kotlin")
    }
}

// Configure the compose desktop application
compose.desktop {
    application {
        mainClass = "ireader.desktop.MainKt"
        
        // 7-Project Supabase configuration
        // Load from properties files with fallback chain: env vars -> local.properties -> config.properties
        // Environment variable names match the GitHub Actions workflow secrets
        val supabaseAuthUrl = getConfigProperty("SUPABASE_AUTH_URL", "supabase.auth.url")
        val supabaseAuthKey = getConfigProperty("SUPABASE_AUTH_KEY", "supabase.auth.key")
        val supabaseReadingUrl = getConfigProperty("SUPABASE_READING_URL", "supabase.reading.url")
        val supabaseReadingKey = getConfigProperty("SUPABASE_READING_KEY", "supabase.reading.key")
        val supabaseLibraryUrl = getConfigProperty("SUPABASE_LIBRARY_URL", "supabase.library.url")
        val supabaseLibraryKey = getConfigProperty("SUPABASE_LIBRARY_KEY", "supabase.library.key")
        val supabaseBookReviewsUrl = getConfigProperty("SUPABASE_BOOK_REVIEWS_URL", "supabase.book_reviews.url")
        val supabaseBookReviewsKey = getConfigProperty("SUPABASE_BOOK_REVIEWS_KEY", "supabase.book_reviews.key")
        val supabaseChapterReviewsUrl = getConfigProperty("SUPABASE_CHAPTER_REVIEWS_URL", "supabase.chapter_reviews.url")
        val supabaseChapterReviewsKey = getConfigProperty("SUPABASE_CHAPTER_REVIEWS_KEY", "supabase.chapter_reviews.key")
        val supabaseBadgesUrl = getConfigProperty("SUPABASE_BADGES_URL", "supabase.badges.url")
        val supabaseBadgesKey = getConfigProperty("SUPABASE_BADGES_KEY", "supabase.badges.key")
        val supabaseAnalyticsUrl = getConfigProperty("SUPABASE_ANALYTICS_URL", "supabase.analytics.url")
        val supabaseAnalyticsKey = getConfigProperty("SUPABASE_ANALYTICS_KEY", "supabase.analytics.key")
        
        jvmArgs += listOf(
            "-Xmx2G",  // Increase JVM memory
            "-noverify",  // Disable bytecode verification for dex2jar converted extensions
            // 7-Project Supabase JVM args - these are baked into the packaged app
            "-Dsupabase.auth.url=$supabaseAuthUrl",
            "-Dsupabase.auth.key=$supabaseAuthKey",
            "-Dsupabase.reading.url=$supabaseReadingUrl",
            "-Dsupabase.reading.key=$supabaseReadingKey",
            "-Dsupabase.library.url=$supabaseLibraryUrl",
            "-Dsupabase.library.key=$supabaseLibraryKey",
            "-Dsupabase.book_reviews.url=$supabaseBookReviewsUrl",
            "-Dsupabase.book_reviews.key=$supabaseBookReviewsKey",
            "-Dsupabase.chapter_reviews.url=$supabaseChapterReviewsUrl",
            "-Dsupabase.chapter_reviews.key=$supabaseChapterReviewsKey",
            "-Dsupabase.badges.url=$supabaseBadgesUrl",
            "-Dsupabase.badges.key=$supabaseBadgesKey",
            "-Dsupabase.analytics.url=$supabaseAnalyticsUrl",
            "-Dsupabase.analytics.key=$supabaseAnalyticsKey"
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
    tasks.findByName(taskName)?.dependsOn("createWindowsLauncher")
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
    tasks.findByName(taskName)?.dependsOn("createWindowsLauncher")
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
start javaw -Xmx2G -noverify -jar IReader.jar
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
abstract class VerifyNativeLibrariesTask : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val nativeDir: DirectoryProperty
    
    @TaskAction
    fun verify() {
        val nativeDirFile = nativeDir.get().asFile
        val platforms = listOf("windows-x64", "macos-x64", "macos-arm64", "linux-x64")
        val requiredLibs = mapOf(
            "windows-x64" to listOf("piper_jni.dll", "onnxruntime.dll"),
            "macos-x64" to listOf("libpiper_jni.dylib", "libonnxruntime.dylib"),
            "macos-arm64" to listOf("libpiper_jni.dylib", "libonnxruntime.dylib"),
            "linux-x64" to listOf("libpiper_jni.so", "libonnxruntime.so")
        )
        
        var allPresent = true
        platforms.forEach { platform ->
            val platformDir = nativeDirFile.resolve(platform)
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

tasks.register<VerifyNativeLibrariesTask>("verifyNativeLibraries") {
    group = "verification"
    description = "Verifies that Piper TTS native libraries are present in resources"
    nativeDir.set(project.layout.projectDirectory.dir("src/main/resources/native"))
}

// Run verification before packaging
tasks.whenTaskAdded {
    if (name.startsWith("package") || name.startsWith("createDistributable")) {
        dependsOn("verifyNativeLibraries")
    }
}

// Enable ZIP64 for uber JAR to support more than 65535 entries
tasks.withType<Jar> {
    isZip64 = true
}
