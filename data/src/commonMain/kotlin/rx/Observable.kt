package rx

/**
 * Minimal RxJava Observable shim for tsundoku extension compatibility.
 *
 * Tsundoku extensions use Observable in deprecated fetch* methods on HttpSource.
 * This shim provides just enough functionality for:
 * - Observable.just(value)
 * - observable.map { transform }
 * - observable.awaitSingle()
 * - Call.asObservableSuccess()
 *
 * Extensions that use advanced RxJava operators will not work.
 */
class Observable<T> private constructor(private val producer: (() -> T)?) {

    private var cachedValue: T? = null
    private var hasValue = false

    /**
     * Map operator — transforms the emitted value.
     */
    fun <R> map(func: (T) -> R): Observable<R> {
        return Observable {
            val source = if (hasValue) cachedValue!! else producer!!()
            func(source)
        }
    }

    /**
     * FlatMap operator — transforms and flattens.
     */
    fun <R> flatMap(func: (T) -> Observable<R>): Observable<R> {
        return Observable {
            val source = if (hasValue) cachedValue!! else producer!!()
            func(source).toBlocking().first()
        }
    }

    /**
     * Subscribe with onNext callback (simplified).
     */
    fun subscribe(onNext: (T) -> Unit): Subscription {
        try {
            val value = producer!!()
            onNext(value)
        } catch (e: Exception) {
            // Ignore errors in subscribe
        }
        return Subscription()
    }

    /**
     * Convert to blocking observable.
     */
    fun toBlocking(): BlockingObservable<T> = BlockingObservable {
        if (hasValue) cachedValue!! else producer!!()
    }

    companion object {
        /**
         * Create an Observable that emits a single value.
         */
        fun <T> just(value: T): Observable<T> {
            val obs = Observable<T>(null)
            obs.cachedValue = value
            obs.hasValue = true
            return obs
        }

        /**
         * Create an Observable from a producer function.
         */
        fun <T> fromCallable(callable: () -> T): Observable<T> {
            return Observable(callable)
        }

        /**
         * Create an empty Observable.
         */
        fun <T> empty(): Observable<T> {
            return Observable(null)
        }

        /**
         * Create an Observable that emits an error.
         */
        fun <T> error(throwable: Throwable): Observable<T> {
            return Observable { throw throwable }
        }
    }
}

/**
 * Blocking wrapper for Observable.
 */
class BlockingObservable<T>(private val producer: () -> T) {
    fun first(): T = producer()
    fun firstOrDefault(defaultValue: T): T {
        return try {
            producer()
        } catch (e: Exception) {
            defaultValue
        }
    }
}

/**
 * Minimal Subscription class.
 */
class Subscription {
    var isUnsubscribed = false
        private set

    fun unsubscribe() {
        isUnsubscribed = true
    }
}
