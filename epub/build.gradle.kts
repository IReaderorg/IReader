plugins {
    id("com.android.library")
    id("kotlin-android")

}

dependencies {
    implementation("nl.siegmann.epublib:epublib-core:3.1") {
        exclude(group="org.slf4j")
        exclude(group="xmlpull")
    }
    implementation("org.slf4j:slf4j-android:1.7.25")

}