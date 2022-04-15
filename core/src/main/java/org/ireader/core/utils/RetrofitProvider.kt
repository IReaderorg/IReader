package org.ireader.core.utils

import com.squareup.moshi.Moshi
import org.ireader.core_api.http.HttpClients
import org.ireader.core_api.http.okhttp
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Inject

class RetrofitProvider @Inject constructor(
    private val client: HttpClients,
    private val moshi: Moshi,
) {

    fun get(url: String): Retrofit = Retrofit.Builder()
        .baseUrl(url)
        .client(client.default.okhttp)
        .addConverterFactory(MoshiConverterFactory.create(moshi).asLenient())
        .build()

}