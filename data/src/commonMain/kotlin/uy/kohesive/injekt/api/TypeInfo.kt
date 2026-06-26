package uy.kohesive.injekt.api

import java.lang.reflect.*

/**
 * Minimal TypeReference shim for tsundoku extension compatibility.
 */
interface TypeReference<T> {
    val type: Type
}

/**
 * Minimal FullTypeReference shim for tsundoku extension compatibility.
 * Captures generic type information at runtime.
 */
abstract class FullTypeReference<T> protected constructor() : TypeReference<T> {
    override val type: Type = javaClass.getGenericSuperclass().let { superClass ->
        if (superClass is Class<*>) {
            throw IllegalArgumentException("Internal error: TypeReference constructed without actual type information")
        }
        (superClass as ParameterizedType).getActualTypeArguments()[0]
    }
}

/**
 * Create a FullTypeReference using reified generics.
 */
inline fun <reified T : Any> fullType(): FullTypeReference<T> = object : FullTypeReference<T>() {}
inline fun <reified T : Any> typeRef(): FullTypeReference<T> = object : FullTypeReference<T>() {}

/**
 * Get erased Class from a Type.
 */
@Suppress("UNCHECKED_CAST")
fun Type.erasedType(): Class<Any> {
    return when (this) {
        is Class<*> -> this as Class<Any>
        is ParameterizedType -> this.getRawType().erasedType()
        is GenericArrayType -> {
            val elementType = this.getGenericComponentType().erasedType()
            java.lang.reflect.Array.newInstance(elementType, 0).javaClass
        }
        is WildcardType -> this.getUpperBounds()[0].erasedType()
        else -> throw IllegalStateException("Should not get here.")
    }
}
