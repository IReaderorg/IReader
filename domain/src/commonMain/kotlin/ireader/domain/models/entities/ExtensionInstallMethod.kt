package ireader.domain.models.entities

/**
 * Represents different methods for installing extensions
 * Based on Mihon's installation system
 */
enum class ExtensionInstallMethod {
    /**
     * Standard package installer (default)
     */
    PACKAGE_INSTALLER,
    
    /**
     * Shizuku integration for privileged installation
     */
    SHIZUKU,
    
    /**
     * Private installation method
     */
    PRIVATE,
    
    /**
     * Legacy installation method
     */
    LEGACY
}
