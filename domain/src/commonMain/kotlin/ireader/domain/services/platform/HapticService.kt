package ireader.domain.services.platform

import ireader.domain.services.common.PlatformService

/**
 * Platform-agnostic haptic feedback service
 * 
 * Provides haptic (vibration) feedback for user interactions
 */
interface HapticService : PlatformService {
    
    /**
     * Perform haptic feedback
     * 
     * @param type Type of haptic feedback
     */
    fun performHapticFeedback(type: HapticType)
    
    /**
     * Check if haptic feedback is supported
     * 
     * @return true if haptic feedback is available
     */
    fun isHapticSupported(): Boolean
    
    /**
     * Check if haptic feedback is enabled in system settings
     * 
     * @return true if haptic is enabled
     */
    fun isHapticEnabled(): Boolean
    
    /**
     * Perform custom vibration pattern
     * 
     * @param pattern Vibration pattern (timings in milliseconds)
     * @param amplitudes Vibration amplitudes (0-255), null for default
     */
    fun performCustomVibration(
        pattern: LongArray,
        amplitudes: IntArray? = null
    )
    
    /**
     * Cancel ongoing vibration
     */
    fun cancelVibration()
}

/**
 * Haptic feedback types
 */
enum class HapticType {
    /**
     * Light impact - subtle feedback
     * Use for: Button taps, list item selection
     */
    LIGHT_IMPACT,
    
    /**
     * Medium impact - moderate feedback
     * Use for: Toggle switches, checkboxes
     */
    MEDIUM_IMPACT,
    
    /**
     * Heavy impact - strong feedback
     * Use for: Important actions, confirmations
     */
    HEAVY_IMPACT,
    
    /**
     * Success feedback - positive action completed
     * Use for: Successful operations, achievements
     */
    SUCCESS,
    
    /**
     * Warning feedback - caution needed
     * Use for: Warnings, important notices
     */
    WARNING,
    
    /**
     * Error feedback - something went wrong
     * Use for: Errors, failed operations
     */
    ERROR,
    
    /**
     * Selection feedback - item selected
     * Use for: Picker selection, slider movement
     */
    SELECTION,
    
    /**
     * Click feedback - standard click
     * Use for: General button clicks
     */
    CLICK,
    
    /**
     * Long press feedback - long press detected
     * Use for: Long press actions, context menus
     */
    LONG_PRESS,
    
    /**
     * Reject feedback - action rejected
     * Use for: Invalid input, blocked actions
     */
    REJECT
}

/**
 * Predefined vibration patterns
 */
object HapticPatterns {
    /**
     * Quick tap pattern
     */
    val QUICK_TAP = longArrayOf(0, 50)
    
    /**
     * Double tap pattern
     */
    val DOUBLE_TAP = longArrayOf(0, 50, 50, 50)
    
    /**
     * Success pattern
     */
    val SUCCESS = longArrayOf(0, 50, 50, 100)
    
    /**
     * Error pattern
     */
    val ERROR = longArrayOf(0, 100, 50, 100, 50, 100)
    
    /**
     * Warning pattern
     */
    val WARNING = longArrayOf(0, 200, 100, 200)
    
    /**
     * Notification pattern
     */
    val NOTIFICATION = longArrayOf(0, 100, 100, 100, 100, 100)
}
