

package org.ireader.core_api.util

import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.encodeUtf8
import okio.ByteString.Companion.toByteString

fun String.decodeBase64() = decodeBase64()!!

fun String.md5() = encodeUtf8().md5().hex()

fun String.encodeBase64() = encodeToByteArray().toByteString().base64()
