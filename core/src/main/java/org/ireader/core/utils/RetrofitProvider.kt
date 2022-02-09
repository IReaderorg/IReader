package org.ireader.core.utils

import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Inject

class RetrofitProvider @Inject constructor(
    private val client: OkHttpClient,
    private val moshi: Moshi,
) {

    fun get(url: String): Retrofit = Retrofit.Builder()
        .baseUrl(url)
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi).asLenient())
        .build()

}