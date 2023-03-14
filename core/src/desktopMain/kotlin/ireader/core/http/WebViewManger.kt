package ireader.core.http

import org.jsoup.nodes.Document

actual class WebViewManger {
    actual var isInit: Boolean
        get() = false
        set(value) {}
    actual var userAgent: String = ""
    actual var selector: String?
        get() = ""
        set(value) {}
    actual var html: Document
        get() = Document("")
        set(value) {}
    actual var webUrl: String?
        get() = ""
        set(value) {}
    actual var inProgress: Boolean
        get() = false
        set(value) {}

    actual fun init(): Any {
        return 0
    }

    actual fun update() {
    }

    actual fun destroy() {
    }

}