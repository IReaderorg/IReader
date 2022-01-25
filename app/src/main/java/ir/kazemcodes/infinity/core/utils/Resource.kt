package ir.kazemcodes.infinity.core.utils

sealed class Resource<T>(val data: T? = null, val uiText: String? = null) {
    class Success<T>(data: T?): Resource<T>(data)
    class Error<T>(uiText: String, data: T? = null): Resource<T>(data, uiText)
}
