package uy.kohesive.injekt

import uy.kohesive.injekt.api.DefaultInjektRegistrar
import uy.kohesive.injekt.api.InjektScope
import kotlin.reflect.KClass

/**
 * Top-level InjektScope instance.
 * Extensions use: `val preferences: Preferences by injectLazy()`
 */
val injekt: InjektScope = InjektScope(DefaultInjektRegistrar())

/**
 * Minimal Injekt service locator shim for tsundoku extension compatibility.
 */
object Injekt {
    private val registry = mutableMapOf<KClass<*>, Any>()
    private val typeRegistry = mutableMapOf<Class<*>, Any>()

    fun <T : Any> get(type: KClass<T>): T {
        registry[type]?.let { return it as T }
        typeRegistry[type.java]?.let { return it as T }
        throw RuntimeException("Injekt shim: No registered instance for ${type.java.name}")
    }

    fun <T : Any> get(type: Class<T>): T {
        typeRegistry[type]?.let { return it as T }
        throw RuntimeException("Injekt shim: No registered instance for ${type.name}")
    }

    fun <T : Any> getOrNull(type: Class<T>): T? {
        return typeRegistry[type] as? T
    }

    fun <T : Any> registerValue(type: KClass<T>, value: T) {
        registry[type] = value
    }

    fun <T : Any> registerValue(type: Class<T>, value: T) {
        typeRegistry[type] = value
    }

    fun <T : Any> registerValue(value: T, type: KClass<T>) {
        registry[type] = value
        typeRegistry[type.java] = value
    }

    fun clear() {
        registry.clear()
        typeRegistry.clear()
    }
}

/**
 * Lazy injection delegate.
 */
inline fun <reified T : Any> injectLazy(): Lazy<T> {
    val type = T::class
    return lazy { Injekt.get(type) }
}

/**
 * Eager injection delegate.
 */
inline fun <reified T : Any> inject(): T {
    return Injekt.get(T::class)
}
