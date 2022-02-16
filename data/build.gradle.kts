apply {
    from("$rootDir/base-module.gradle")
}
dependencies {

    "implementation"(project(Modules.source))
    "implementation"(project(Modules.core))
    "implementation"(project(Modules.domain))
    "implementation"(Room.roomRuntime)
    "kapt"(Room.roomCompiler)
    "implementation"(Room.roomKtx)
    "implementation"(Room.roomPaging)
    "implementation"(Moshi.moshi)
    "implementation"(Moshi.moshiKotlin)
    "implementation"(Jsoup.jsoup)
    "implementation"(OkHttp.okHttp3Interceptor)

    /** Coroutine **/
    "implementation"(Coroutines.core)
    "implementation"(Coroutines.android)

    /** Retrofit **/
    "implementation"(Retrofit.retrofit)
    "implementation"(Retrofit.moshiConverter)
    "implementation"(Testing.truth)

}