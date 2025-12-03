package ireader.domain.usecases.services

/**
 * iOS implementation of StartExtensionManagerService
 * 
 * On iOS, extensions are managed differently (JS plugins via JavaScriptCore)
 */
actual class StartExtensionManagerService {
    actual fun start() {
        // No-op on iOS - extensions are JS plugins loaded via JavaScriptCore
    }
    
    actual fun stop() {
        // No-op on iOS
    }
}
