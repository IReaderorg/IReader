package ir.kazemcodes.infinity.domain.network.models

import ir.kazemcodes.infinity.api_feature.network.NetworkHelper
import okhttp3.Headers
import okhttp3.OkHttpClient
import java.security.MessageDigest

interface Source {

    val id: Long

    val lang: String

    val name: String

    val supportsLatest: Boolean

}

