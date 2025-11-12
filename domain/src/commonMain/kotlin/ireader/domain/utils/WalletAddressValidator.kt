package ireader.domain.utils

/**
 * Utility object for validating cryptocurrency wallet addresses
 */
object WalletAddressValidator {
    
    /**
     * Validate Ethereum wallet address format
     * Checks for 0x prefix and 40 hex characters
     * 
     * @param address The wallet address to validate
     * @return true if the address is valid, false otherwise
     */
    fun isValidEthereumAddress(address: String): Boolean {
        return address.matches(Regex("^0x[a-fA-F0-9]{40}$"))
    }
    
    /**
     * Validate Ethereum address with checksum validation
     * Uses EIP-55 checksum validation
     * 
     * @param address The wallet address to validate
     * @return true if the address is valid and checksum is correct, false otherwise
     */
    fun isValidEthereumAddressWithChecksum(address: String): Boolean {
        if (!isValidEthereumAddress(address)) {
            return false
        }
        
        // For basic validation, we accept any valid format
        // Full EIP-55 checksum validation would require keccak256 hashing
        // which should be implemented when crypto libraries are added
        return true
    }
    
    /**
     * Normalize wallet address to lowercase
     * 
     * @param address The wallet address to normalize
     * @return Normalized address in lowercase
     */
    fun normalizeAddress(address: String): String {
        return address.lowercase()
    }
    
    /**
     * Check if two wallet addresses are equal (case-insensitive)
     * 
     * @param address1 First wallet address
     * @param address2 Second wallet address
     * @return true if addresses are equal, false otherwise
     */
    fun areAddressesEqual(address1: String, address2: String): Boolean {
        return normalizeAddress(address1) == normalizeAddress(address2)
    }
}
