apply {
    from("$rootDir/compose-module.gradle")
}
dependencies {
    "implementation"(project(Modules.domain))
    "implementation"(project(Modules.core))
    "implementation"(project(Modules.coreUi))
    "implementation"(project(Modules.source))
    "implementation"(Compose.paging)
    "implementation"(Compose.icons)
    "implementation"(Compose.runtime)
    "implementation"(Jsoup.jsoup)
    "implementation"(Worker.runtimeKtx)
    "implementation"(Room.roomRuntime)
    "kapt"(Room.roomCompiler)
    "implementation"(Room.roomKtx)
    "implementation"(Room.roomPaging)
    "implementation"(Moshi.moshi)
    "implementation"(Moshi.moshiKotlin)
    /** Accompanist **/
    "implementation"(Accompanist.systemUiController)
    "implementation"(Accompanist.swipeRefresh)
    "implementation"(Accompanist.pager)
    "implementation"(Accompanist.pagerIndicator)
    "implementation"(Accompanist.insets)
    "implementation"(Accompanist.navAnimation)
    "implementation"(Accompanist.flowlayout)
    "implementation"(Accompanist.navMaterial)
    "implementation"(Accompanist.webView)

    "implementation"(Coil.coilCompose)

    "implementation"(DaggerHilt.worker)
    "implementation"(Retrofit.retrofit)


}