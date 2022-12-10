import org.gradle.kotlin.dsl.support.delegates.DependencyHandlerDelegate


fun DependencyHandlerDelegate.setupKSP(dependencyNotation: Any) {
    listOf(
        "kspDesktop",
        "kspAndroid",
    ).forEach {
        add(it, dependencyNotation)
    }
}