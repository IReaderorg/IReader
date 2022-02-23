plugins {
    id("com.android.library")
    id("module-plugin")
}
android {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

addKtor()

dependencies {
    implementation(project(Modules.core))
    implementation(Deps.Moshi.moshi)
    implementation(Deps.Jsoup.jsoup)
}
