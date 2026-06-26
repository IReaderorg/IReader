package uy.kohesive.injekt.api

/**
 * Minimal InjektRegistrar interface shim for tsundoku extension compatibility.
 */
interface InjektRegistrar : InjektRegistry, InjektFactory {
    fun importModule(submodule: InjektModule) {
        submodule.registerWith(this)
    }
}
