object Testing {
    const val junit4 = "junit:junit:4.13.2"
    const val extJunit = "androidx.test.ext:junit:1.1.3"
    const val espresso = "androidx.test.espresso:espresso-core:3.5.0-alpha03"

    private const val junitAndroidExtVersion = "1.1.4-alpha03"
    const val junitAndroidExt = "androidx.test.ext:junit:$junitAndroidExtVersion"

    private const val coroutinesTestVersion = "1.5.1"
    const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesTestVersion"

    private const val truthVersion = "1.1.3"
    const val truth = "com.google.truth:truth:$truthVersion"


    const val composeUiTest = "androidx.compose.ui:ui-test-junit4:${Compose.composeVersion}"

    const val hiltTesting = "com.google.dagger:hilt-android-testing:${DaggerHilt.version}"

    private const val testRunnerVersion = "1.4.0"
    const val testRunner = "androidx.test:runner:$testRunnerVersion"
}