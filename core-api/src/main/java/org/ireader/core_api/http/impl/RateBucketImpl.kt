

package org.ireader.core_api.http.impl

import kotlinx.datetime.Clock
import org.ireader.core_api.http.main.RateBucket

class RateBucketImpl(
    internal var capacity: Int,
    internal var refillRate: Long,
    internal var tokens: Int = capacity,
    internal var refillTime: Long = Clock.System.now().toEpochMilliseconds()
) : RateBucket {

    override fun tryConsume(): Boolean {
        refill()
        return if (this.tokens >= 1) {
            this.tokens -= 1
            true
        } else {
            false
        }
    }

    override fun tryConsume(tokens: Int): Boolean {
        refill()
        return if (this.tokens >= tokens && tokens <= this.capacity) {
            this.tokens -= tokens
            true
        } else {
            false
        }
    }

    override fun getTokens(): Int {
        return this.tokens
    }

    override fun serialize(): String {
        return "$capacity;$refillRate;$tokens;$refillTime"
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



fun RateBucketImpl.Companion.deserialize(serialized: String): RateBucketImpl {
    val deserialized = serialized.split(";")
    return RateBucketImpl(
        capacity = deserialized[0].toInt(),
        refillRate = deserialized[1].toLong(),
        tokens = deserialized[2].toInt(),
        refillTime = deserialized[3].toLong()
    )
}
