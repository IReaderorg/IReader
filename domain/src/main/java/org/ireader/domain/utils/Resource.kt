package org.ireader.domain.utils

sealed class Resource<T>(val data: T? = null, val uiText: org.ireader.common_extensions.UiText? = null) {
    class Success<T>(data: T?) : Resource<T>(data)
    class Error<T>(uiText: org.ireader.common_extensions.UiText, data: T? = null) : Resource<T>(data, uiText)
}
