plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.serialization")
    id(libs.plugins.sqldelight.get().pluginId)
    id("com.google.devtools.ksp")
    alias(libs.plugins.jetbrainCompose)
    alias(kotlinx.plugins.compose.compiler)
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
        compilations {
            all {
                compilerOptions.configure {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(ProjectConfig.androidJvmTarget.toString()))
                }
            }
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
    
    // iOS targets
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "data"
            isStatic = true
        }
    }

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
                
                // Kotlin Reflection - Required for Supabase inline reified functions
                implementation(kotlinx.reflect)
                
                // Supabase
                implementation(libs.bundles.supabase)
            }
        }
        
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.sqldelight.runtime)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
                implementation(project(Modules.coreApi))
                // REMOVED: napier - not used in tests
            }
        }
        
        androidMain {
            dependencies {
                // Platform-specific Ktor engines
                implementation(libs.ktor.okhttp)
                implementation(libs.ktor.core.android)
                
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
            }
        }
        
        val androidUnitTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.sqldelight.jvm)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
            }
        }

        val desktopMain by getting {
            kotlin.srcDir("./src/jvmMain/kotlin")
            dependencies {
                // Platform-specific Ktor engine
                implementation(libs.ktor.okhttp)
                
                implementation(libs.sqldelight.jvm)
                implementation(libs.apk.parser)

                implementation(libs.dex2jar.translator)
                implementation(libs.dex2jar.tools)
            }
        }
        
        val desktopTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.sqldelight.jvm)
            }
        }
        
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain.get())
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
                // Platform-specific Ktor engine
                implementation("io.ktor:ktor-client-darwin:3.3.2")
                
                implementation(libs.sqldelight.native)
            }
        }
        
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by creating {
            dependsOn(commonTest.get())
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }

    }

}
dependencies {

    implementation(libs.sqldelight.android.paging)
}

android {
    namespace = "ireader.data"
    compileSdk = ProjectConfig.compileSdk

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    

    
    sourceSets {
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
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
    androidComponents.onVariants { variant ->
        val name = variant.name
        sourceSets {
            getByName(name).kotlin.srcDir("${layout.buildDirectory.get().asFile.absolutePath}/generated/ksp/${name}/kotlin")
        }
    }
    compileOptions {
        sourceCompatibility = ProjectConfig.androidJvmTarget
        targetCompatibility = ProjectConfig.androidJvmTarget
    }
    buildFeatures {
        buildConfig = true
    }

}
sqldelight {
    databases {
        create("Database") {
            packageName.set("ir.kazemcodes.infinityreader")
            schemaOutputDirectory = file("src/commonMain/sqldelight/databases")
            dialect("app.cash.sqldelight:sqlite-3-24-dialect:2.0.2")
            verifyMigrations = true
            migrationOutputDirectory = file("src/commonMain/sqldelight/migrations")
            deriveSchemaFromMigrations = false
            srcDirs(file("src/commonMain/sqldelight"))
            verifyMigrations.set(System.getenv("CI") != null)
        }
    }
}



