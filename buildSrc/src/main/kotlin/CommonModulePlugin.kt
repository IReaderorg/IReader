import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

class CommonModulePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // apply plugin common to all projects
        project.plugins.apply("kotlin-android")
        project.plugins.apply("kotlinx-serialization")
        project.plugins.apply("kotlin-kapt")

        // configure the android block
        val androidExtensions = project.extensions.getByName("android")
        if (androidExtensions is BaseExtension) {
            androidExtensions.apply {
                compileSdkVersion(31)
                buildToolsVersion("30.0.3")


//                project.tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).configureEach {
//                    kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
//                }
                project.tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile>()
                    .configureEach {
                        kotlinOptions {
                            jvmTarget = JavaVersion.VERSION_1_8.toString()
                            freeCompilerArgs = freeCompilerArgs + listOf(
                                "-Xjvm-default=compatibility",
                                "-Xopt-in=kotlin.RequiresOptIn"
                            )
                        }
                    }

                packagingOptions {
                    resources.excludes.addAll(listOf(
                        "LICENSE.txt",
                        "META-INF/LICENSE",
                        "META-INF/LICENSE.txt",
                        "META-INF/README.md",
                        "META-INF/AL2.0",
                        "META-INF/LGPL2.1",
                        "**/attach_hotspot_windows.dll",
                        "META-INF/licenses/ASM",
                        "META-INF/gradle/incremental.annotation.processors"
                    ))
                }
                testOptions {
                    unitTests.isReturnDefaultValues = true
                }
                defaultConfig {
                    minSdk = ProjectConfig.minSdk
                    targetSdk = ProjectConfig.targetSdk
                    versionCode(ProjectConfig.ConfigVersionCode)
                    versionName(ProjectConfig.ConfigVersionName)


                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

                    vectorDrawables {
                        useSupportLibrary = true
                    }
                }
                compileOptions {
                    isCoreLibraryDesugaringEnabled = true
                    sourceCompatibility(JavaVersion.VERSION_11)
                    targetCompatibility(JavaVersion.VERSION_11)
                }

                when (this) {
                    is LibraryExtension -> {
                        defaultConfig {
                            // apply the pro guard rules for library
                            consumerProguardFiles("consumer-rules.pro")
                        }
                    }

                    is AppExtension -> {
                        buildTypes {
                            getByName("release") {
                                isMinifyEnabled = false
                                proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"),
                                    "proguard-rules.pro")
                            }
                        }
                    }
                }

            }

        }


        // dependencies common to all projects
        project.dependencies {
            add("implementation", Deps.AndroidX.coreKtx)
            add("implementation", Deps.AndroidX.coreKtx)
            add("implementation", Deps.AndroidX.appCompat)
            add("implementation", Deps.AndroidX.material)
            add("implementation", Deps.AndroidX.activity)
            add("implementation", Deps.Timber.timber)


            add("testImplementation", Deps.Testing.junit4)
            add("testImplementation", Deps.Testing.junitAndroidExt)
            add("testImplementation", Deps.Testing.truth)
            add("testImplementation", Deps.Testing.coroutines)
            add("testImplementation", Deps.Testing.composeUiTest)



            add("androidTestImplementation", Deps.Testing.junit4)
            add("androidTestImplementation", Deps.Testing.junitAndroidExt)
            add("androidTestImplementation", Deps.Testing.truth)
            add("androidTestImplementation", Deps.Testing.coroutines)

            add("implementation", Deps.Compose.runtime)

        }
    }
}
