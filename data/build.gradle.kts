plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    kotlin("plugin.serialization")
    id(libs.plugins.sqldelight.get().pluginId)
    alias(libs.plugins.jetbrainCompose)
    alias(kotlinx.plugins.compose.compiler)
    id(libs.plugins.buildkonfig.get().pluginId)
}

kotlin {
    androidLibrary {
        namespace = "ireader.data"
        compileSdk = ProjectConfig.compileSdk
        minSdk = ProjectConfig.minSdk
        
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(ProjectConfig.androidJvmTarget.toString()))
        }
        
        packaging {
            resources.excludes.addAll(
                listOf(
                    "LICENSE.txt",
                    "META-INF/LICENSE",
                    "META-INF/LICENSE.txt",
                    "META-INF/README.md",
                    "META-INF/AL2.0",
                    "META-INF/LGPL2.1",
                    "**/attach_hotspot_windows.dll",
                    "META-INF/licenses/ASM",
                    "META-INF/*",
                    "META-INF/gradle/incremental.annotation.processors"
                )
            )
        }
    }
    
    jvm("desktop") {
        compilations {
            all {
                compilerOptions.configure {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(ProjectConfig.desktopJvmTarget.toString()))
                }
            }
        }
    }
    
//    // iOS targets
//    listOf(
//        iosX64(),
//        iosArm64(),
//        iosSimulatorArm64()
//    ).forEach { iosTarget ->
//        iosTarget.binaries.framework {
//            baseName = "data"
//            isStatic = true
//        }
//    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(Modules.coreApi))
                implementation(project(Modules.sourceApi))
                implementation(project(Modules.domain))
                implementation(project(Modules.commonResources))
                implementation(kotlinx.serialization.json)
                implementation(libs.sqldelight.runtime)
                implementation(libs.sqldelight.coroutines)
                implementation(compose.runtime)
                implementation(compose.components.resources)

                api(libs.koin.core)
                api(libs.ksoup)
                api(kotlinx.datetime)
                // Common Ktor dependencies (platform-agnostic)
                api(libs.ktor.core)
                api(libs.ktor.core.cio)
                api(libs.ktor.contentNegotiation)
                api(libs.ktor.contentNegotiation.kotlinx)
                
                // Ktor WebSocket for sync data transfer (client-side only in common)
                implementation("io.ktor:ktor-client-websockets:${libs.versions.ktor.get()}")
                implementation("io.ktor:ktor-serialization-kotlinx-json:${libs.versions.ktor.get()}")
                
                // Ktor compression for network optimization (Phase 10.2.1)
                implementation("io.ktor:ktor-client-encoding:${libs.versions.ktor.get()}")
                // Note: ktor-server-* dependencies moved to platform-specific source sets (JVM-only)
                
                // Kotlin Reflection - Required for Supabase inline reified functions
                implementation(kotlinx.reflect)
                
                // Supabase
                implementation(libs.bundles.supabase)
                
                // Note: BouncyCastle moved to platform-specific source sets (JVM-only)
            }
        }
        
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.sqldelight.runtime)
                implementation(libs.coroutines.test)
                implementation(libs.test.truth)
                implementation(libs.mock)
                implementation(project(Modules.coreApi))
                // REMOVED: napier - not used in tests
            }
        }
        
        androidMain {
            dependencies {
                // Platform-specific Ktor engines
                implementation(libs.ktor.okhttp)
                implementation(libs.ktor.core.android)
                
                // SQLDelight Android-specific dependencies
                implementation(libs.sqldelight.android.paging)
                
                // Ktor server dependencies (JVM-only, not available on iOS)
                implementation("io.ktor:ktor-server-core:${libs.versions.ktor.get()}")
                implementation("io.ktor:ktor-server-netty:${libs.versions.ktor.get()}") // Changed from CIO to Netty
                implementation("io.ktor:ktor-server-websockets:${libs.versions.ktor.get()}")
                implementation("io.ktor:ktor-server-compression:${libs.versions.ktor.get()}")
                
                // APK parser (for Tsundoku icon extraction from local APKs)
                implementation(libs.apk.parser)
                
                implementation(androidx.core)
                implementation(libs.requerySqlite)
                implementation(libs.sqldelight.android)
                implementation(libs.androidSqlite)
                implementation("androidx.biometric:biometric:1.1.0")
                
                // Encrypted SharedPreferences for secure token storage
                implementation("androidx.security:security-crypto:1.1.0-alpha06")
                
                // Google Sign-In for Google Drive OAuth
                implementation("com.google.android.gms:play-services-auth:21.0.0")
                implementation("com.google.api-client:google-api-client-android:2.2.0") {
                    exclude(group = "org.apache.httpcomponents")
                }
                implementation("com.google.apis:google-api-services-drive:v3-rev20231128-2.0.0") {
                    exclude(group = "org.apache.httpcomponents")
                }
                
                // BouncyCastle for X.509 certificate generation (Task 9.2.2) - Android
                implementation("org.bouncycastle:bcprov-jdk18on:1.86")
                implementation("org.bouncycastle:bcpkix-jdk18on:1.86")
                
                // android-compat for KoinInjektBridge code compilation
                compileOnly(project(":android-compat"))
                
                // Injekt (required by Tsundoku extensions)
                implementation("com.github.mihonapp:injekt:91edab2317")
            }
        }
        
        val desktopMain by getting {
            kotlin.srcDir("./src/jvmMain/kotlin")
            dependencies {
                // android-compat provides: Android stubs, source-api, Injekt bridge, OkHttp, Jsoup, RxJava
                implementation(project(":android-compat"))
                
                // Injekt (required by Tsundoku desktop loader)
                implementation("com.github.mihonapp:injekt:91edab2317")
                
                // Platform-specific Ktor engine
                implementation(libs.ktor.okhttp)
                
                // Ktor server dependencies (JVM-only, not available on iOS)
                implementation("io.ktor:ktor-server-core:${libs.versions.ktor.get()}")
                implementation("io.ktor:ktor-server-netty:${libs.versions.ktor.get()}") // Changed from CIO to Netty
                implementation("io.ktor:ktor-server-websockets:${libs.versions.ktor.get()}")
                implementation("io.ktor:ktor-server-compression:${libs.versions.ktor.get()}")
                
                implementation(libs.sqldelight.jvm)
                implementation(libs.apk.parser)

                implementation(libs.dex2jar.translator)
                implementation(libs.dex2jar.tools)
                
                // JmDNS for mDNS service discovery on Desktop
                implementation("org.jmdns:jmdns:3.6.4")
                
                // BouncyCastle for X.509 certificate generation (Task 9.2.2) - Desktop
                implementation("org.bouncycastle:bcprov-jdk18on:1.86")
                implementation("org.bouncycastle:bcpkix-jdk18on:1.86")
            }
        }
        
        val desktopTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.sqldelight.jvm)
            }
        }
        
//        val iosX64Main by getting
//        val iosArm64Main by getting
//        val iosSimulatorArm64Main by getting
//        val iosMain by creating {
//            dependsOn(commonMain.get())
//            iosX64Main.dependsOn(this)
//            iosArm64Main.dependsOn(this)
//            iosSimulatorArm64Main.dependsOn(this)
//            dependencies {
//                // Platform-specific Ktor engine
//                implementation("io.ktor:ktor-client-darwin:3.3.2")
//
//                implementation(libs.sqldelight.native)
//            }
//        }
//
//        val iosX64Test by getting
//        val iosArm64Test by getting
//        val iosSimulatorArm64Test by getting
//        val iosTest by creating {
//            dependsOn(commonTest.get())
//            iosX64Test.dependsOn(this)
//            iosArm64Test.dependsOn(this)
//            iosSimulatorArm64Test.dependsOn(this)
//        }

    }

}

sqldelight {
    databases {
        create("Database") {
            packageName.set("ir.kazemcodes.infinityreader")
            schemaOutputDirectory = file("src/commonMain/sqldelight/databases")
            dialect("app.cash.sqldelight:sqlite-3-24-dialect:${libs.versions.sqldelight.get()}")
            verifyMigrations = false
            migrationOutputDirectory = file("src/commonMain/sqldelight/migrations")
            deriveSchemaFromMigrations = false
            srcDirs(file("src/commonMain/sqldelight"))
        }
    }
}




buildkonfig {
    packageName = "ireader.data"
    exposeObjectWithName = "BuildConfig"
    
    defaultConfigs {
        // Debug flag - true for debug builds, false for release
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN, "DEBUG", "${!gradle.startParameter.taskNames.any { it.contains("release", ignoreCase = true) }}")
    }
}
