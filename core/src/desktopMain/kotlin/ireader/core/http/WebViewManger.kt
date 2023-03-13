package ireader.core.http

import org.jsoup.nodes.Document

actual class WebViewManger {
    actual var isInit: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var userAgent: String
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var selector: String?
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var html: Document
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var webUrl: String?
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var inProgress: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}

    actual fun init(): Any {
        TODO("Not yet implemented")
    }

    actual fun update() {
    }

    actual fun destroy() {
    }

}