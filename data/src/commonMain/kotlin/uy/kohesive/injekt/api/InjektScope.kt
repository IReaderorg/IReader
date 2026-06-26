@file:Suppress("NOTHING_TO_INLINE")

package uy.kohesive.injekt.api

import java.lang.reflect.Type
import kotlin.reflect.KClass

/**
 * Minimal InjektScope shim for tsundoku extension compatibility.
 * Delegates to an InjektRegistrar which handles actual instance management.
 */
open class InjektScope(val registrar: InjektRegistrar) : InjektRegistrar by registrar {
    inline fun <reified T : Any> injectLazy(): Lazy<T> {
        return lazy { get(fullType<T>()) }
    }

    inline fun <reified T : Any> injectValue(): Lazy<T> {
        return lazyOf(get(fullType<T>()))
    }

    inline fun <reified T : Any> injectLazy(key: Any): Lazy<T> {
        return lazy { get(fullType<T>(), key) }
    }

    inline fun <reified T : Any> injectValue(key: Any): Lazy<T> {
        return lazyOf(get(fullType<T>(), key))
    }
}

/**
 * Default InjektRegistrar implementation backed by our Injekt shim.
 */
class DefaultInjektRegistrar : InjektRegistrar {
    private val singletons = mutableMapOf<Type, Any>()
    private val factories = mutableMapOf<Type, () -> Any>()

    override fun <T : Any> addSingleton(forType: TypeReference<T>, singleInstance: T) {
        singletons[forType.type] = singleInstance
        // Also register in the global Injekt shim
        val erased = forType.type.erasedType()
        uy.kohesive.injekt.Injekt.registerValue(erased as Class<Any>, singleInstance as Any)
    }

    override fun <R : Any> addSingletonFactory(forType: TypeReference<R>, factoryCalledOnce: () -> R) {
        val value = factoryCalledOnce()
        singletons[forType.type] = value
    }

    override fun <R : Any> addFactory(forType: TypeReference<R>, factoryCalledEveryTime: () -> R) {
        factories[forType.type] = factoryCalledEveryTime
    }

    override fun <R : Any> addPerThreadFactory(forType: TypeReference<R>, factoryCalledOncePerThread: () -> R) {
        // Simplified: just use regular factory
        factories[forType.type] = factoryCalledOncePerThread
    }

    override fun <R : Any, K : Any> addPerKeyFactory(forType: TypeReference<R>, factoryCalledPerKey: (K) -> R) {
        // Simplified: not fully supported
    }

    override fun <R : Any, K : Any> addPerThreadPerKeyFactory(forType: TypeReference<R>, factoryCalledPerKeyPerThread: (K) -> R) {
        // Simplified: not fully supported
    }

    override fun <R : Any> addLoggerFactory(forLoggerType: TypeReference<R>, factoryByName: (String) -> R, factoryByClass: (Class<Any>) -> R) {
        // Simplified: not fully supported
    }

    override fun <O : Any, T : O> addAlias(existingRegisteredType: TypeReference<T>, otherAncestorOrInterface: TypeReference<O>) {
        // Simplified: not fully supported
    }

    override fun <T : Any> hasFactory(forType: TypeReference<T>): Boolean {
        return singletons.containsKey(forType.type) || factories.containsKey(forType.type)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <R : Any> getInstance(forType: Type): R {
        // Try singleton first
        singletons[forType]?.let { return it as R }
        // Try factory
        factories[forType]?.let { return it() as R }
        // Fall back to global Injekt shim
        val erased = forType.erasedType()
        return uy.kohesive.injekt.Injekt.get(erased as Class<R>)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <R : Any> getInstanceOrElse(forType: Type, default: R): R {
        return getInstanceOrNull(forType) ?: default
    }

    override fun <R : Any> getInstanceOrElse(forType: Type, default: () -> R): R {
        return getInstanceOrNull(forType) ?: default()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <R : Any> getInstanceOrNull(forType: Type): R? {
        singletons[forType]?.let { return it as R }
        factories[forType]?.let { return it() as R }
        val erased = forType.erasedType()
        return uy.kohesive.injekt.Injekt.getOrNull(erased as Class<R>)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <R : Any, K : Any> getKeyedInstance(forType: Type, key: K): R {
        throw UnsupportedOperationException("Keyed instances not supported in shim")
    }

    override fun <R : Any, K : Any> getKeyedInstanceOrElse(forType: Type, key: K, default: R): R = default
    override fun <R : Any, K : Any> getKeyedInstanceOrElse(forType: Type, key: K, default: () -> R): R = default()
    override fun <R : Any, K : Any> getKeyedInstanceOrNull(forType: Type, key: K): R? = null

    override fun <R : Any> getLogger(expectedLoggerType: Type, byName: String): R {
        throw UnsupportedOperationException("Logger not supported in shim")
    }

    override fun <R : Any, T : Any> getLogger(expectedLoggerType: Type, forClass: Class<T>): R {
        throw UnsupportedOperationException("Logger not supported in shim")
    }
}
