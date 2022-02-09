apply {
    from("$rootDir/base-module.gradle")
}
plugins {
    id("kotlinx-serialization")
}
dependencies {
    "implementation"(project(Modules.core))
    "implementation"(project(Modules.coreUi))
    "implementation"(project(Modules.source))

    "implementation"(Compose.ui)
    "implementation"(Compose.navigation)
    "implementation"(Worker.runtimeKtx)
    "implementation"(DaggerHilt.worker)
    "implementation"(Coil.coilCompose)

    /** Room **/
    "implementation"(Room.roomRuntime)
    "kapt"(Room.roomCompiler)
    "implementation"(Room.roomKtx)
    "implementation"(Room.roomPaging)

    "implementation"(FlowPreferences.flowPreferences)


    /** Coroutine **/
    "implementation"(Coroutines.core)
    "implementation"(Coroutines.android)

    /** Retrofit **/
    "implementation"(Retrofit.retrofit)
    "implementation"(Retrofit.moshiConverter)

    "implementation"(OkHttp.okHttp3)

    "implementation"(OkHttp.okHttp3Interceptor)
    "implementation"(OkHttp.okhttp3_doh)
    "implementation"(OkHttp.okio)
    "implementation"(Compose.paging)
    "implementation"(Jsoup.jsoup)
    "implementation"(Jsonpathkt.jsonpathkt)

}