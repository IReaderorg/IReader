package ireader.plugin.api

import kotlinx.serialization.Serializable

/**
 * Plugin interface for community screens.
 * Adds custom UI screens to the app (forums, recommendations, social features).
 * 
 * Example:
 * ```kotlin
 * class ForumPlugin : CommunityScreenPlugin {
 *     override val manifest = PluginManifest(
 *         id = "com.example.forum",
 *         name = "Community Forum",
 *         type = PluginType.COMMUNITY_SCREEN,
 *         permissions = listOf(PluginPermission.NETWORK, PluginPermission.UI_INJECTION),
 *         // ... other manifest fields
 *     )
 *     
 *     override val screens = listOf(
 *         CommunityScreen(
 *             id = "forum",
 *             title = "Forum",
 *             icon = "forum",
 *             route = "community/forum"
 *         )
 *     )
 * }
 * ```
 */
interface CommunityScreenPlugin : Plugin {
    /**
     * Screens provided by this plugin.
     */
    val screens: List<CommunityScreen>
    
    /**
     * Navigation items to add to the app.
     */
    val navigationItems: List<CommunityNavItem>
    
    /**
     * Get screen content data.
     */
    suspend fun getScreenContent(screenId: String, params: Map<String, String> = emptyMap()): CommunityResult<ScreenContent>
    
    /**
     * Handle user action on screen.
     */
    suspend fun handleAction(action: CommunityAction): CommunityResult<ActionResponse>
    
    /**
     * Get notifications/updates for this plugin.
     */
    suspend fun getNotifications(): CommunityResult<List<CommunityNotification>>
    
    /**
     * Mark notification as read.
     */
    suspend fun markNotificationRead(notificationId: String): CommunityResult<Unit>
    
    /**
     * Get user profile (if applicable).
     */
    suspend fun getUserProfile(): CommunityResult<CommunityUserProfile?>
    
    /**
     * Authenticate user (if required).
     */
    suspend fun authenticate(credentials: Map<String, String>): CommunityResult<CommunityAuthResponse>
    
    /**
     * Check if authentication is required.
     */
    fun requiresAuth(): Boolean
    
    /**
     * Get badge count for navigation item.
     */
    fun getBadgeCount(screenId: String): Int
}

/**
 * Community screen definition.
 */
@Serializable
data class CommunityScreen(
    /** Unique screen identifier */
    val id: String,
    /** Screen title */
    val title: String,
    /** Screen icon name */
    val icon: String? = null,
    /** Navigation route */
    val route: String,
    /** Screen description */
    val description: String? = null,
    /** Whether screen requires authentication */
    val requiresAuth: Boolean = false,
    /** Screen type */
    val type: ScreenType = ScreenType.LIST,
    /** Whether screen supports pull-to-refresh */
    val supportsPullToRefresh: Boolean = true,
    /** Whether screen supports infinite scroll */
    val supportsInfiniteScroll: Boolean = false
)

/**
 * Screen types.
 */
@Serializable
enum class ScreenType {
    /** List of items */
    LIST,
    /** Grid of items */
    GRID,
    /** Detail view */
    DETAIL,
    /** Form/input */
    FORM,
    /** Web view */
    WEBVIEW,
    /** Custom composable */
    CUSTOM
}

/**
 * Navigation item for community screens.
 */
@Serializable
data class CommunityNavItem(
    /** Screen ID this item navigates to */
    val screenId: String,
    /** Display label */
    val label: String,
    /** Icon name */
    val icon: String,
    /** Sort order */
    val order: Int = 0,
    /** Where to show this item */
    val placement: NavPlacement = NavPlacement.MORE_MENU,
    /** Whether to show badge */
    val showBadge: Boolean = false
)

/**
 * Navigation placement options.
 */
@Serializable
enum class NavPlacement {
    /** Bottom navigation bar */
    BOTTOM_NAV,
    /** Side drawer */
    DRAWER,
    /** More/overflow menu */
    MORE_MENU,
    /** Library tab */
    LIBRARY_TAB,
    /** Reader menu */
    READER_MENU
}

/**
 * Screen content data.
 */
@Serializable
data class ScreenContent(
    /** Screen ID */
    val screenId: String,
    /** Content items */
    val items: List<ContentItem> = emptyList(),
    /** Header content */
    val header: ContentHeader? = null,
    /** Whether there are more items to load */
    val hasMore: Boolean = false,
    /** Next page token/cursor */
    val nextPageToken: String? = null,
    /** Actions available on this screen */
    val actions: List<ScreenAction> = emptyList(),
    /** Filters available */
    val filters: List<ContentFilter> = emptyList()
)

/**
 * Content item for community screens.
 */
@Serializable
data class ContentItem(
    /** Item ID */
    val id: String,
    /** Item type */
    val type: ContentItemType,
    /** Title */
    val title: String,
    /** Subtitle */
    val subtitle: String? = null,
    /** Description/body */
    val body: String? = null,
    /** Image URL */
    val imageUrl: String? = null,
    /** Author/creator */
    val author: String? = null,
    /** Timestamp */
    val timestamp: Long? = null,
    /** Like/upvote count */
    val likeCount: Int = 0,
    /** Comment count */
    val commentCount: Int = 0,
    /** Whether current user liked this */
    val isLiked: Boolean = false,
    /** Navigation route when tapped */
    val route: String? = null,
    /** Additional metadata */
    val metadata: Map<String, String> = emptyMap(),
    /** Available actions */
    val actions: List<String> = emptyList()
)

/**
 * Content item types.
 */
@Serializable
enum class ContentItemType {
    POST,
    COMMENT,
    REVIEW,
    RECOMMENDATION,
    USER,
    BOOK,
    CHAPTER,
    NOTIFICATION,
    ANNOUNCEMENT,
    POLL,
    CUSTOM
}

/**
 * Content header.
 */
@Serializable
data class ContentHeader(
    /** Header title */
    val title: String,
    /** Header subtitle */
    val subtitle: String? = null,
    /** Header image URL */
    val imageUrl: String? = null,
    /** Header description */
    val description: String? = null,
    /** Stats to display */
    val stats: List<HeaderStat> = emptyList()
)

@Serializable
data class HeaderStat(
    val label: String,
    val value: String,
    val icon: String? = null
)

/**
 * Screen action.
 */
@Serializable
data class ScreenAction(
    /** Action ID */
    val id: String,
    /** Action label */
    val label: String,
    /** Action icon */
    val icon: String? = null,
    /** Action type */
    val type: ActionType = ActionType.BUTTON
)

@Serializable
enum class ActionType {
    BUTTON,
    FAB,
    MENU_ITEM,
    TOOLBAR_ACTION
}

/**
 * Content filter.
 */
@Serializable
data class ContentFilter(
    /** Filter ID */
    val id: String,
    /** Filter label */
    val label: String,
    /** Filter type */
    val type: FilterType,
    /** Filter options (for SELECT type) */
    val options: List<FilterOption> = emptyList(),
    /** Default value */
    val defaultValue: String? = null
)

@Serializable
enum class FilterType {
    SELECT,
    MULTI_SELECT,
    TEXT,
    DATE_RANGE,
    TOGGLE
}

@Serializable
data class FilterOption(
    val value: String,
    val label: String
)

/**
 * Community action.
 */
@Serializable
data class CommunityAction(
    /** Action ID */
    val actionId: String,
    /** Screen ID */
    val screenId: String,
    /** Target item ID (if applicable) */
    val itemId: String? = null,
    /** Action parameters */
    val params: Map<String, String> = emptyMap()
)

/**
 * Action response.
 */
@Serializable
data class ActionResponse(
    /** Whether action succeeded */
    val success: Boolean,
    /** Response message */
    val message: String? = null,
    /** Updated item (if applicable) */
    val updatedItem: ContentItem? = null,
    /** Navigation route (if action triggers navigation) */
    val navigateTo: String? = null,
    /** Whether to refresh screen */
    val shouldRefresh: Boolean = false
)

/**
 * Community notification.
 */
@Serializable
data class CommunityNotification(
    /** Notification ID */
    val id: String,
    /** Notification title */
    val title: String,
    /** Notification body */
    val body: String,
    /** Notification type */
    val type: NotificationType,
    /** Timestamp */
    val timestamp: Long,
    /** Whether notification is read */
    val isRead: Boolean = false,
    /** Navigation route when tapped */
    val route: String? = null,
    /** Related item ID */
    val relatedItemId: String? = null
)

@Serializable
enum class NotificationType {
    REPLY,
    MENTION,
    LIKE,
    FOLLOW,
    ANNOUNCEMENT,
    UPDATE,
    CUSTOM
}

/**
 * Community user profile.
 */
@Serializable
data class CommunityUserProfile(
    /** User ID */
    val id: String,
    /** Username */
    val username: String,
    /** Display name */
    val displayName: String? = null,
    /** Avatar URL */
    val avatarUrl: String? = null,
    /** Bio */
    val bio: String? = null,
    /** Join date */
    val joinedAt: Long? = null,
    /** Post count */
    val postCount: Int = 0,
    /** Follower count */
    val followerCount: Int = 0,
    /** Following count */
    val followingCount: Int = 0
)

/**
 * Community authentication response.
 */
@Serializable
data class CommunityAuthResponse(
    /** Whether authentication succeeded */
    val success: Boolean,
    /** User profile */
    val profile: CommunityUserProfile? = null,
    /** Access token */
    val accessToken: String? = null,
    /** Error message */
    val errorMessage: String? = null
)

/**
 * Result wrapper for community operations.
 */
sealed class CommunityResult<out T> {
    data class Success<T>(val data: T) : CommunityResult<T>()
    data class Error(val error: CommunityError) : CommunityResult<Nothing>()
    
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }
    
    inline fun <R> map(transform: (T) -> R): CommunityResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }
}

/**
 * Community errors.
 */
@Serializable
sealed class CommunityError {
    data class NetworkError(val message: String) : CommunityError()
    data class AuthRequired(val message: String) : CommunityError()
    data class Unauthorized(val message: String) : CommunityError()
    data class NotFound(val itemId: String) : CommunityError()
    data class RateLimited(val retryAfterMs: Long?) : CommunityError()
    data class ServerError(val statusCode: Int, val message: String) : CommunityError()
    data class ValidationError(val field: String, val message: String) : CommunityError()
    data class Unknown(val message: String) : CommunityError()
}
