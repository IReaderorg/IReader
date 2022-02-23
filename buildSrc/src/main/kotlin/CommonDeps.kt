import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies


fun Project.addCompose() {
    dependencies {
        add("implementation", Deps.Compose.compiler)
        add("implementation", Deps.Compose.foundation)
        add("implementation", Deps.Compose.activityCompose)
        add("implementation", Deps.Compose.ui)
        add("implementation", Deps.Compose.material)
        add("implementation", Deps.Compose.uiToolingPreview)
        add("implementation", Deps.Compose.viewModelCompose)
        add("implementation", Deps.Compose.icons)
        add("implementation", Deps.Compose.animations)
        add("implementation", Deps.Compose.navigation)
        add("implementation", Deps.Compose.hiltNavigationCompose)
        add("androidTestImplementation", Deps.Compose.ui_test_manifest)
        add("androidTestImplementation", Deps.Compose.testing)
        add("androidTestImplementation", Deps.Compose.ui_test_manifest)
        add("androidTestImplementation", Deps.Compose.composeTooling)
        add("androidTestImplementation", Deps.Compose.paging)

        add("implementation", Deps.Coil.coilCompose)


    }
}

fun Project.addAccompanist() {
    dependencies {
        add("implementation", Deps.Accompanist.systemUiController)
        add("implementation", Deps.Accompanist.webView)
        add("implementation", Deps.Compose.activityCompose)
        add("implementation", Deps.Accompanist.swipeRefresh)
        add("implementation", Deps.Accompanist.pager)
        add("implementation", Deps.Accompanist.pagerIndicator)
        add("implementation", Deps.Accompanist.insets)
        add("implementation", Deps.Accompanist.navAnimation)
        add("implementation", Deps.Accompanist.flowlayout)
        add("implementation", Deps.Accompanist.navMaterial)
    }
}

fun Project.addKtor() {
    dependencies {
        add("implementation", Deps.Ktor.core)
        add("implementation", Deps.Ktor.ktor_jsoup)
        add("implementation", Deps.Ktor.okhttp)
        add("implementation", Deps.Ktor.serialization)
    }
}

fun Project.addTesting() {
    dependencies {
        add("testImplementation", Deps.Testing.junit4)
        add("testImplementation", Deps.Testing.junitAndroidExt)
        add("testImplementation", Deps.Testing.truth)
        add("testImplementation", Deps.Testing.coroutines)
        add("testImplementation", Deps.Testing.composeUiTest)

        add("androidTestImplementation", Deps.Testing.junit4)
        add("androidTestImplementation", Deps.Testing.junitAndroidExt)
        add("androidTestImplementation", Deps.Testing.truth)
        add("androidTestImplementation", Deps.Testing.coroutines)
        add("androidTestImplementation", Deps.Testing.composeUiTest)
        add("androidTestImplementation", Deps.Testing.hiltTesting)

    }
}