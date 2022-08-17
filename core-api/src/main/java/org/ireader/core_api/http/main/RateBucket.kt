package org.ireader.core_api.http.main

interface RateBucket {
    fun tryConsume(): Boolean
    fun tryConsume(tokens: Int): Boolean
    fun getTokens(): Int

    fun serialize() :String

}