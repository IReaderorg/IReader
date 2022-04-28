

package org.ireader.core_api.http

import kotlinx.datetime.Clock

class RateBucket(
    internal var capacity: Int,
    internal var refillRate: Long,
    internal var tokens: Int = capacity,
    internal var refillTime: Long = Clock.System.now().toEpochMilliseconds()
) {

    fun tryConsume(): Boolean {
        refill()
        return if (this.tokens >= 1) {
            this.tokens -= 1
            true
        } else {
            false
        }
    }

    fun tryConsume(tokens: Int): Boolean {
        refill()
        return if (this.tokens >= tokens && tokens <= this.capacity) {
            this.tokens -= tokens
            true
        } else {
            false
        }
    }

    fun getTokens(): Int {
        return this.tokens
    }

    private fun refill() {
        val now = Clock.System.now().toEpochMilliseconds()
        val toRefill = ((now - refillTime) / refillRate).toInt()
        this.tokens += toRefill
        if (this.tokens > this.capacity) {
            this.tokens = this.capacity
            refillTime = now
        }
        refillTime += refillRate
    }

    companion object
}

fun RateBucket.serialize(): String {
    return "$capacity;$refillRate;$tokens;$refillTime"
}

fun RateBucket.Companion.deserialize(serialized: String): RateBucket {
    val deserialized = serialized.split(";")
    return RateBucket(
        capacity = deserialized[0].toInt(),
        refillRate = deserialized[1].toLong(),
        tokens = deserialized[2].toInt(),
        refillTime = deserialized[3].toLong()
    )
}
