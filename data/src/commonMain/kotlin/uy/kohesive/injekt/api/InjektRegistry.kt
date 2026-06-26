@file:Suppress("NOTHING_TO_INLINE")

package uy.kohesive.injekt.api

/**
 * Minimal InjektRegistry interface shim for tsundoku extension compatibility.
 */
interface InjektRegistry {
    fun <T : Any> addSingleton(forType: TypeReference<T>, singleInstance: T)
    fun <R : Any> addSingletonFactory(forType: TypeReference<R>, factoryCalledOnce: () -> R)
    fun <R : Any> addFactory(forType: TypeReference<R>, factoryCalledEveryTime: () -> R)
    fun <R : Any> addPerThreadFactory(forType: TypeReference<R>, factoryCalledOncePerThread: () -> R)
    fun <R : Any, K : Any> addPerKeyFactory(forType: TypeReference<R>, factoryCalledPerKey: (K) -> R)
    fun <R : Any, K : Any> addPerThreadPerKeyFactory(forType: TypeReference<R>, factoryCalledPerKeyPerThread: (K) -> R)
    fun <R : Any> addLoggerFactory(forLoggerType: TypeReference<R>, factoryByName: (String) -> R, factoryByClass: (Class<Any>) -> R)
    fun <O : Any, T : O> addAlias(existingRegisteredType: TypeReference<T>, otherAncestorOrInterface: TypeReference<O>)
    fun <T : Any> hasFactory(forType: TypeReference<T>): Boolean
}

// Extension functions for convenient access

inline fun <reified T : Any> InjektRegistry.hasFactory(): Boolean = hasFactory(fullType<T>())
inline fun <reified T : Any> InjektRegistry.addSingleton(singleInstance: T) = addSingleton(fullType<T>(), singleInstance)
inline fun <reified R : Any> InjektRegistry.addSingletonFactory(noinline factoryCalledOnce: () -> R) = addSingletonFactory(fullType<R>(), factoryCalledOnce)
inline fun <reified R : Any> InjektRegistry.addFactory(noinline factoryCalledEveryTime: () -> R) = addFactory(fullType<R>(), factoryCalledEveryTime)
