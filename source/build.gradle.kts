apply {
    from("$rootDir/base-module.gradle")

}
dependencies {
    "implementation"(project(Modules.core))


    "implementation"(Moshi.moshi)
    "implementation"(Room.roomRuntime)
    "implementation"(Room.roomKtx)

    "implementation"(OkHttp.okHttp3)

    "implementation"(OkHttp.okHttp3Interceptor)
    "implementation"(OkHttp.okhttp3_doh)
    "implementation"(OkHttp.okio)
    "implementation"(Compose.paging)
    "implementation"(Jsoup.jsoup)


}