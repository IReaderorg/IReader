package tachiyomi.core.common.util.lang

import rx.Observable

/**
 * Minimal awaitSingle shim for tsundoku extension compatibility.
 *
 * The real implementation uses RxJava 1.x Subscriber pattern with cancellation support.
 * This simplified version just blocks and returns the first value.
 * Cancellation is not supported but is not needed for our use case.
 */
suspend fun <T> Observable<T>.awaitSingle(): T {
    return toBlocking().first()
}
