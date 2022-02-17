apply {
    from("$rootDir/base-module.gradle")
}
dependencies {
    "implementation"(Room.roomRuntime)
    "implementation"(Compose.ui)
    "implementation"(Retrofit.retrofit)
    "implementation"(Moshi.moshi)
    "implementation"(Moshi.moshiKotlin)
    "implementation"(Retrofit.moshiConverter)
    "implementation"(Jsoup.jsoup)
    "implementation"(Datastore.datastore)
    "implementation"(Worker.runtimeKtx)
    "implementation"(DaggerHilt.worker)
    "implementation"(Coil.coilCompose)
    "implementation"(kotlin("stdlib"))
    "implementation"(Ktor.core)
    "implementation"(Ktor.serialization)
    "implementation"(Ktor.okhttp)
}
