package uy.kohesive.injekt.api

/**
 * Minimal InjektModule interface shim for tsundoku extension compatibility.
 */
interface InjektModule {
    fun registerWith(registrar: InjektRegistrar)
}
