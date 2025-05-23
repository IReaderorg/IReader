plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.serialization")
    id(libs.plugins.sqldelight.get().pluginId)
    id("com.google.devtools.ksp")
    alias(kotlinx.plugins.compose.compiler)
}

kotlin {
    androidTarget {
        compilations {
            all {
                kotlinOptions.jvmTarget = ProjectConfig.androidJvmTarget.toString()
            }
        }
    }
    jvm("desktop") {
        compilations {
            all {
                kotlinOptions.jvmTarget = ProjectConfig.desktopJvmTarget.toString()
            }
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
                implementation(libs.requerySqlite)
                api(libs.koin.core)
                api(libs.jsoup)
                api(kotlinx.datetime)
                api(libs.bundles.ireader)
            }
        }
        androidMain {
            dependencies {
                implementation(androidx.core)

                implementation(libs.sqldelight.android)
                implementation(libs.androidSqlite)
            }
        }

        val desktopMain by getting {
            kotlin.srcDir("./src/jvmMain/kotlin")
            dependencies {
                implementation(libs.sqldelight.jvm)
                implementation(libs.apk.parser)

                implementation(libs.dex2jar.translator)
                implementation(libs.dex2jar.tools)
            }
        }

    }

}
dependencies {

    implementation(libs.sqldelight.android.paging)

    testImplementation(test.bundles.common)
    debugImplementation(libs.androidSqlite)
    androidTestImplementation(test.bundles.common)
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
    packagingOptions {
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
            getByName(name).kotlin.srcDir("${buildDir.absolutePath}/generated/ksp/${name}/kotlin")
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
            deriveSchemaFromMigrations = true
            srcDirs(file("src/commonMain/sqldelight"))
            verifyMigrations.set(System.getenv("CI") != null)
        }
    }
}