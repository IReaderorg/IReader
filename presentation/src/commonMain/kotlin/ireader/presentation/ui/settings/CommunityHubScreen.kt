package ireader.presentation.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable
import ireader.presentation.ui.component.reusable_composable.TopAppBarBackButton
import ireader.presentation.ui.settings.components.SettingsItem
import ireader.presentation.ui.settings.components.SettingsSectionHeader

/**
 * Community Hub - A parent screen that groups all community-related features
 * including leaderboards, popular books, reviews, and badge customization.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityHubScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onLeaderboard: () -> Unit,
    onPopularBooks: () -> Unit,
    onAllReviews: () -> Unit,
    onCharacterArtGallery: () -> Unit,
    onReadingBuddy: () -> Unit = {},
    onGlossary: () -> Unit = {},
    onCommunitySource: () -> Unit = {},
    onUserSources: () -> Unit = {},
    onLegadoSources: () -> Unit = {},
    onFeatureStore: () -> Unit = {},
    onPluginRepository: () -> Unit = {},
    onDeveloperPortal: () -> Unit = {},
    onBadgeStore: () -> Unit,
    onNFTBadge: () -> Unit,
    onBadgeManagement: () -> Unit,
    isAdmin: Boolean = false,
    onAdminBadgeVerification: () -> Unit = {},
    onAdminCharacterArtVerification: () -> Unit = {},
    onAdminUserPanel: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { MidSizeTextComposable(text = localize(Res.string.community)) },
                navigationIcon = { TopAppBarBackButton(onClick = onBack) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier.padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Header Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.People,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = localize(Res.string.community),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Connect with other readers, compete on leaderboards, and customize your profile",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Discover Section
            item {
                SettingsSectionHeader(
                    title = "Discover",
                    icon = Icons.Filled.Explore
                )
            }
            
            item {
                SettingsItem(
                    title = localize(Res.string.leaderboard),
                    description = "Compete with other readers based on reading time",
                    icon = Icons.Filled.EmojiEvents,
                    onClick = onLeaderboard
                )
            }
            
            item {
                SettingsItem(
                    title = localize(Res.string.popular_books),
                    description = "Discover what the community is reading",
                    icon = Icons.Filled.TrendingUp,
                    onClick = onPopularBooks
                )
            }
            
            item {
                SettingsItem(
                    title = localize(Res.string.community_reviews),
                    description = "Read reviews from other readers",
                    icon = Icons.Filled.RateReview,
                    onClick = onAllReviews
                )
            }
            
            // Creative Section
            item {
                SettingsSectionHeader(
                    title = "Creative",
                    icon = Icons.Filled.Palette
                )
            }
            
            item {
                SettingsItem(
                    title = "Character Art Gallery",
                    description = "Browse and share AI-generated character art",
                    icon = Icons.Filled.Image,
                    onClick = onCharacterArtGallery
                )
            }
            
            item {
                SettingsItem(
                    title = "Reading Buddy",
                    description = "Your reading companion with daily quotes",
                    icon = Icons.Filled.Pets,
                    onClick = onReadingBuddy
                )
            }
            
            // Tools Section
            item {
                SettingsSectionHeader(
                    title = "Tools",
                    icon = Icons.Filled.Build
                )
            }
            
            item {
                SettingsItem(
                    title = localize(Res.string.glossary),
                    description = "Manage translation glossaries for your books",
                    icon = Icons.Filled.Translate,
                    onClick = onGlossary
                )
            }
            
            item {
                SettingsItem(
                    title = "Community Source",
                    description = "Share and download AI translations with the community",
                    icon = Icons.Filled.CloudSync,
                    onClick = onCommunitySource
                )
            }
            
            item {
                SettingsItem(
                    title = "User Sources",
                    description = "Create custom sources to scrape novels from any website",
                    icon = Icons.Filled.Extension,
                    onClick = onUserSources
                )
            }
            
            item {
                SettingsItem(
                    title = "Legado Sources",
                    description = "Import book sources from Legado/阅读 format",
                    icon = Icons.Filled.CloudDownload,
                    onClick = onLegadoSources
                )
            }
            
            // Feature Store Section
            item {
                SettingsSectionHeader(
                    title = localize(Res.string.feature_store),
                    icon = Icons.Filled.ShoppingCart
                )
            }
            
            item {
                SettingsItem(
                    title = localize(Res.string.feature_store),
                    description = localize(Res.string.feature_store_description),
                    icon = Icons.Filled.ShoppingCart,
                    onClick = onFeatureStore
                )
            }
            
            item {
                SettingsItem(
                    title = "Plugin Repositories",
                    description = "Manage plugin sources and add custom repositories",
                    icon = Icons.Filled.Storage,
                    onClick = onPluginRepository
                )
            }
            
            item {
                SettingsItem(
                    title = "Developer Portal",
                    description = "Manage your plugins and grant access to users",
                    icon = Icons.Filled.Code,
                    onClick = onDeveloperPortal
                )
            }
            
            // Badges & Customization Section
            item {
                SettingsSectionHeader(
                    title = localize(Res.string.badges_customization),
                    icon = Icons.Filled.Star
                )
            }
            
            item {
                SettingsItem(
                    title = localize(Res.string.badge_store),
                    description = "Purchase unique badges to customize your profile",
                    icon = Icons.Outlined.AccountBalanceWallet,
                    onClick = onBadgeStore
                )
            }
            
            item {
                SettingsItem(
                    title = localize(Res.string.nft_badge),
                    description = "Verify NFT ownership to unlock exclusive badge",
                    icon = Icons.Filled.Star,
                    onClick = onNFTBadge
                )
            }
            
            item {
                SettingsItem(
                    title = localize(Res.string.manage_badges),
                    description = "Customize which badges appear on your profile and reviews",
                    icon = Icons.Outlined.Settings,
                    onClick = onBadgeManagement
                )
            }
            
            // Admin Section (only visible to admins)
            if (isAdmin) {
                item {
                    SettingsSectionHeader(
                        title = localize(Res.string.admin),
                        icon = Icons.Filled.AdminPanelSettings
                    )
                }
                
                item {
                    SettingsItem(
                        title = localize(Res.string.badge_verification),
                        description = "Review and approve badge purchase requests",
                        icon = Icons.Outlined.VerifiedUser,
                        onClick = onAdminBadgeVerification
                    )
                }
                
                item {
                    SettingsItem(
                        title = "Character Art Verification",
                        description = "Review and approve character art submissions",
                        icon = Icons.Outlined.Image,
                        onClick = onAdminCharacterArtVerification
                    )
                }
                
                item {
                    SettingsItem(
                        title = "User Management",
                        description = "Manage users, assign badges, and reset passwords",
                        icon = Icons.Outlined.People,
                        onClick = onAdminUserPanel
                    )
                }
            }
        }
    }
}
