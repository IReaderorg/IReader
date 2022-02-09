package org.ireader.core.utils

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

fun encodeString(string: String): String {
    return URLEncoder.encode(string, StandardCharsets.UTF_8.name())
}

fun decodeString(string: String): String {
    return URLDecoder.decode(string, StandardCharsets.UTF_8.name())
}


fun Any?.isNull(): Boolean {
    return this == null
}

fun Any?.isNotNull(): Boolean {
    return this != null
}