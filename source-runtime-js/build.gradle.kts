plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    js(IR) {
        browser {
            webpackTask {
                mainOutputFileName = "runtime.js"
                output.libraryTarget = "commonjs2"
            }
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
        binaries.executable()
        
        // Generate TypeScript declarations
        generateTypeScriptDefinitions()
    }
    
    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(project(":source-api"))
                implementation(kotlinx.coroutines.core)
                implementation(kotlinx.serialization.json)
                implementation(libs.ktor.core)
                implementation(libs.ktor.client.js)
                implementation(libs.ksoup)
            }
        }
        
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}
