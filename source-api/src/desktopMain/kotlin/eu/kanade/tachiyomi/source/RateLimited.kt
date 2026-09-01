package eu.kanade.tachiyomi.source

/**
 * A source that declares its own request-pacing requirements.
 *
 * Sources implementing this interface tell the host app the minimum and recommended
 * delay to leave between requests to their site. The app uses this to seed and clamp
 * its own per-extension rate-limit settings, but extensions should not rely solely on
 * the host app enforcing this - a resilient extension bakes its own throttling in too.
 */
interface RateLimited {
    /**
     * The minimum delay (in milliseconds) this source needs between requests.
     * The app will not let a user configure less delay than this for the source.
     */
    val minimumDelayMillis: Long

    /**
     * The delay (in milliseconds) this source's author recommends between requests.
     * Used to seed the default value in the app's per-extension rate-limit settings.
     */
    val recommendedDelayMillis: Long
        get() = minimumDelayMillis

    /**
     * How many requests this source can tolerate in a quick burst before [recommendedDelayMillis]
     * needs to be enforced. For example, a value of 3 lets 3 requests through immediately (or
     * with minimal spacing), then the next request waits out the full delay window - useful for
     * sources where a handful of requests per novel (e.g. one per chapter) is fine, but a long
     * unbroken stream is not. Purely a UI-default seed, like [recommendedDelayMillis] - not a
     * hard-enforced cap the way [minimumDelayMillis] is.
     */
    val recommendedPermits: Int
        get() = 1
}
