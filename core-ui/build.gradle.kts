apply {
    from("$rootDir/compose-module.gradle")
}
dependencies {
    "implementation"(project(Modules.core))
    "implementation"(Accompanist.systemUiController)

}