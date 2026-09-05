package ireader.presentation.ui.settings.sync

import androidx.compose.runtime.Stable
import ireader.domain.data.repository.RemoteRepository
import ireader.domain.preferences.prefs.SupabasePreferences
import ireader.presentation.ui.core.viewmodel.StateViewModel
import kotlinx.coroutines.launch
import ireader.domain.utils.extensions.currentTimeToLong

@Stable
data class SupabaseConfigState(
    val autoSyncEnabled: Boolean = true,
    val syncOnWifiOnly: Boolean = true,
    val lastSyncTime: Long = 0L,
    val isTesting: Boolean = false,
    val isSyncing: Boolean = false,
    val testResult: String? = null,
    val error: String? = null,
    val copyStatusMessage: String? = null,
    // Single Project Mode (Recommended for personal Supabase)
    val isSingleProjectMode: Boolean = true,
    val singleProjectUrl: String = "",
    val singleProjectKey: String = "",
    val isPersonalConfigured: Boolean = false,
    val showImportDialog: Boolean = false,
    val importInputText: String = "",
    val showShareDialog: Boolean = false,
    // Custom configuration toggle (Advanced 7-project)
    val useCustomSupabase: Boolean = false,
    // Default configuration (from local.properties/config.properties)
    val hasDefaultConfig: Boolean = false,
    // 7-Project configuration
    // Project 1 - Auth
    val authUrl: String = "",
    val authApiKey: String = "",
    // Project 2 - Reading
    val readingUrl: String = "",
    val readingApiKey: String = "",
    // Project 3 - Library
    val libraryUrl: String = "",
    val libraryApiKey: String = "",
    // Project 4 - Book Reviews
    val bookReviewsUrl: String = "",
    val bookReviewsApiKey: String = "",
    // Project 5 - Chapter Reviews
    val chapterReviewsUrl: String = "",
    val chapterReviewsApiKey: String = "",
    // Project 6 - Badges
    val badgesUrl: String = "",
    val badgesApiKey: String = "",
    // Project 7 - Analytics
    val analyticsUrl: String = "",
    val analyticsApiKey: String = "",
    // Project 8 - Community Source
    val communityUrl: String = "",
    val communityApiKey: String = ""
)

class SupabaseConfigViewModel(
    private val supabasePreferences: SupabasePreferences,
    private val remoteRepository: RemoteRepository,
    private val syncManager: ireader.domain.services.SyncManager? = null,
    private val bookRepository: ireader.domain.data.repository.BookRepository? = null
) : StateViewModel<SupabaseConfigState>(SupabaseConfigState()) {
    
    init {
        loadConfiguration()
    }
    
    fun loadConfiguration() {
        scope.launch {
            // Check if default config exists (from local.properties/config.properties)
            val hasDefault = try {
                val defaultAuthUrl = ireader.domain.config.PlatformConfig.getSupabaseAuthUrl()
                defaultAuthUrl.isNotEmpty()
            } catch (e: Exception) {
                false
            }

            val singleUrl = supabasePreferences.userSupabaseUrl().get().ifEmpty {
                supabasePreferences.supabaseLibraryUrl().get()
            }
            val singleKey = supabasePreferences.userSupabaseAnonKey().get().ifEmpty {
                supabasePreferences.supabaseLibraryKey().get()
            }
            
            updateState { it.copy(
                autoSyncEnabled = supabasePreferences.autoSyncEnabled().get(),
                syncOnWifiOnly = supabasePreferences.syncOnWifiOnly().get(),
                lastSyncTime = supabasePreferences.lastSyncTime().get(),
                useCustomSupabase = supabasePreferences.useCustomSupabase().get(),
                hasDefaultConfig = hasDefault,
                singleProjectUrl = singleUrl,
                singleProjectKey = singleKey,
                isPersonalConfigured = supabasePreferences.isPersonalSupabaseConfigured(),
                // 7-Project configuration (user overrides)
                authUrl = supabasePreferences.supabaseAuthUrl().get(),
                authApiKey = supabasePreferences.supabaseAuthKey().get(),
                readingUrl = supabasePreferences.supabaseReadingUrl().get(),
                readingApiKey = supabasePreferences.supabaseReadingKey().get(),
                libraryUrl = supabasePreferences.supabaseLibraryUrl().get(),
                libraryApiKey = supabasePreferences.supabaseLibraryKey().get(),
                bookReviewsUrl = supabasePreferences.supabaseBookReviewsUrl().get(),
                bookReviewsApiKey = supabasePreferences.supabaseBookReviewsKey().get(),
                chapterReviewsUrl = supabasePreferences.supabaseChapterReviewsUrl().get(),
                chapterReviewsApiKey = supabasePreferences.supabaseChapterReviewsKey().get(),
                badgesUrl = supabasePreferences.supabaseBadgesUrl().get(),
                badgesApiKey = supabasePreferences.supabaseBadgesKey().get(),
                analyticsUrl = supabasePreferences.supabaseAnalyticsUrl().get(),
                analyticsApiKey = supabasePreferences.supabaseAnalyticsKey().get(),
                communityUrl = supabasePreferences.supabaseCommunityUrl().get(),
                communityApiKey = supabasePreferences.supabaseCommunityKey().get()
            )}
        }
    }
    
    fun setSingleProjectUrl(url: String) {
        updateState { it.copy(singleProjectUrl = url) }
    }

    fun setSingleProjectKey(key: String) {
        updateState { it.copy(singleProjectKey = key) }
    }

    fun setIsSingleProjectMode(isSingle: Boolean) {
        updateState { it.copy(isSingleProjectMode = isSingle) }
    }

    fun saveSingleProjectConfig() {
        scope.launch {
            val url = currentState.singleProjectUrl.trim()
            val key = currentState.singleProjectKey.trim()
            supabasePreferences.userSupabaseUrl().set(url)
            supabasePreferences.userSupabaseAnonKey().set(key)
            // Mirror to library and reading endpoints for multi-client compatibility
            supabasePreferences.supabaseLibraryUrl().set(url)
            supabasePreferences.supabaseLibraryKey().set(key)
            supabasePreferences.supabaseReadingUrl().set(url)
            supabasePreferences.supabaseReadingKey().set(key)
            supabasePreferences.useCustomSupabase().set(true)

            updateState {
                it.copy(
                    isPersonalConfigured = supabasePreferences.isPersonalSupabaseConfigured(),
                    testResult = "✓ Personal Supabase configuration saved successfully!",
                    error = null
                )
            }
        }
    }

    fun getSetupSqlScript(): String {
        return """
-- ==========================================================
-- IReader Cloud Sync Setup Script
-- Paste this script into your Supabase SQL Editor and click RUN.
-- Dashboard URL: https://supabase.com/dashboard/project/_/sql
-- ==========================================================

-- 1. Create full sync manifest table (High-Fidelity Document Store)
CREATE TABLE IF NOT EXISTS public.sync_manifest (
    user_id TEXT NOT NULL PRIMARY KEY,
    manifest JSONB NOT NULL,
    updated_at BIGINT NOT NULL DEFAULT 0
);
ALTER TABLE public.sync_manifest ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow public sync_manifest access" ON public.sync_manifest;
CREATE POLICY "Allow public sync_manifest access" ON public.sync_manifest FOR ALL USING (true) WITH CHECK (true);

-- 2. Create synced_books table (Relational View)
CREATE TABLE IF NOT EXISTS public.synced_books (
    user_id TEXT NOT NULL,
    book_id TEXT NOT NULL,
    source_id BIGINT NOT NULL,
    title TEXT NOT NULL,
    book_url TEXT NOT NULL,
    last_read BIGINT NOT NULL DEFAULT 0,
    cover_url TEXT DEFAULT '',
    source_name TEXT DEFAULT '',
    author TEXT DEFAULT '',
    description TEXT DEFAULT '',
    genres TEXT DEFAULT '',
    status BIGINT DEFAULT 0,
    favorite BOOLEAN DEFAULT true,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    PRIMARY KEY (user_id, book_id)
);
CREATE INDEX IF NOT EXISTS idx_synced_books_user_id ON public.synced_books(user_id);
ALTER TABLE public.synced_books ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow public synced_books access" ON public.synced_books;
CREATE POLICY "Allow public synced_books access" ON public.synced_books FOR ALL USING (true) WITH CHECK (true);

-- 3. Create reading_progress table (Relational View)
CREATE TABLE IF NOT EXISTS public.reading_progress (
    user_id TEXT NOT NULL,
    book_id TEXT NOT NULL,
    last_chapter_slug TEXT NOT NULL,
    last_scroll_position FLOAT DEFAULT 0,
    updated_at BIGINT DEFAULT 0,
    PRIMARY KEY (user_id, book_id)
);
CREATE INDEX IF NOT EXISTS idx_reading_progress_user_id ON public.reading_progress(user_id);
ALTER TABLE public.reading_progress ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow public reading_progress access" ON public.reading_progress;
CREATE POLICY "Allow public reading_progress access" ON public.reading_progress FOR ALL USING (true) WITH CHECK (true);

-- 4. Create users & gamification economy (Spirit Stones & Check-ins)
CREATE TABLE IF NOT EXISTS public.users (
    id UUID PRIMARY KEY DEFAULT auth.uid(),
    email TEXT,
    username TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Users can read own profile" ON public.users;
CREATE POLICY "Users can read own profile" ON public.users FOR SELECT USING (auth.uid() = id);
DROP POLICY IF EXISTS "Users can update own profile" ON public.users;
CREATE POLICY "Users can update own profile" ON public.users FOR UPDATE USING (auth.uid() = id);

ALTER TABLE public.users
    ADD COLUMN IF NOT EXISTS display_name TEXT,
    ADD COLUMN IF NOT EXISTS bio TEXT DEFAULT '',
    ADD COLUMN IF NOT EXISTS avatar_url TEXT,
    ADD COLUMN IF NOT EXISTS cover_image_url TEXT,
    ADD COLUMN IF NOT EXISTS level INT DEFAULT 1,
    ADD COLUMN IF NOT EXISTS xp BIGINT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS level_title TEXT DEFAULT 'Novice Reader',
    ADD COLUMN IF NOT EXISTS spirit_stones BIGINT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS active_title_id TEXT,
    ADD COLUMN IF NOT EXISTS checkin_streak INT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS last_checkin_date DATE;

CREATE TABLE IF NOT EXISTS public.daily_checkins (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    checkin_date DATE NOT NULL DEFAULT CURRENT_DATE,
    streak_day INT NOT NULL DEFAULT 1,
    reward_amount INT NOT NULL DEFAULT 10,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE (user_id, checkin_date)
);
ALTER TABLE public.daily_checkins ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS daily_checkins_read ON public.daily_checkins;
CREATE POLICY daily_checkins_read ON public.daily_checkins FOR SELECT USING (auth.uid() = user_id);

CREATE TABLE IF NOT EXISTS public.spirit_stone_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    amount BIGINT NOT NULL,
    type TEXT NOT NULL,
    description TEXT,
    reference_id TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);
ALTER TABLE public.spirit_stone_transactions ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS sst_read ON public.spirit_stone_transactions;
CREATE POLICY sst_read ON public.spirit_stone_transactions FOR SELECT USING (auth.uid() = user_id);

CREATE TABLE IF NOT EXISTS public.user_titles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    title_id TEXT NOT NULL,
    title_name TEXT NOT NULL,
    rarity TEXT NOT NULL DEFAULT 'COMMON',
    is_active BOOLEAN DEFAULT FALSE,
    acquired_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE (user_id, title_id)
);
ALTER TABLE public.user_titles ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS user_titles_all ON public.user_titles;
CREATE POLICY user_titles_all ON public.user_titles FOR ALL USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

-- 5. Daily Check-in RPC Function
DROP FUNCTION IF EXISTS public.checkin_daily();
CREATE OR REPLACE FUNCTION public.checkin_daily()
RETURNS JSON
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public AS $$
DECLARE
    v_user UUID := auth.uid();
    v_today DATE := CURRENT_DATE;
    v_last DATE;
    v_streak INT;
    v_reward INT;
BEGIN
    IF v_user IS NULL THEN RAISE EXCEPTION 'Not authenticated'; END IF;

    SELECT last_checkin_date, COALESCE(checkin_streak, 0) INTO v_last, v_streak
      FROM public.users WHERE id = v_user;

    IF v_last = v_today THEN
        RETURN json_build_object('already', true, 'streak_day', v_streak, 'reward', 0);
    END IF;

    IF v_last = v_today - 1 THEN
        v_streak := v_streak + 1;
    ELSE
        v_streak := 1;
    END IF;

    v_reward := CASE
        WHEN v_streak % 30 = 0 THEN 200
        WHEN v_streak % 7 = 0 THEN 50
        ELSE 10
    END;

    INSERT INTO public.daily_checkins (user_id, checkin_date, streak_day, reward_amount)
    VALUES (v_user, v_today, v_streak, v_reward)
    ON CONFLICT (user_id, checkin_date) DO NOTHING;

    UPDATE public.users
       SET spirit_stones = COALESCE(spirit_stones, 0) + v_reward,
           checkin_streak = v_streak,
           last_checkin_date = v_today
     WHERE id = v_user;

    INSERT INTO public.spirit_stone_transactions (user_id, amount, type, description)
    VALUES (v_user, v_reward, 'CHECKIN', 'Daily check-in (day ' || v_streak || ')');

    RETURN json_build_object('already', false, 'streak_day', v_streak, 'reward', v_reward);
END;
$$;
GRANT EXECUTE ON FUNCTION public.checkin_daily() TO authenticated;

-- 6. Spend Stones RPC Function
DROP FUNCTION IF EXISTS public.spend_stones(TEXT, TEXT, INT);
DROP FUNCTION IF EXISTS public.spend_stones(INT, TEXT);
DROP FUNCTION IF EXISTS public.spend_stones;
CREATE OR REPLACE FUNCTION public.spend_stones(
    p_item_type TEXT,
    p_item_id TEXT,
    p_cost INT
)
RETURNS JSON
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public AS $$
DECLARE
    v_user UUID := auth.uid();
    v_balance BIGINT;
BEGIN
    IF v_user IS NULL THEN RAISE EXCEPTION 'Not authenticated'; END IF;
    IF p_cost < 0 THEN RAISE EXCEPTION 'Invalid cost'; END IF;

    SELECT COALESCE(spirit_stones, 0) INTO v_balance FROM public.users WHERE id = v_user FOR UPDATE;
    IF v_balance < p_cost THEN
        RETURN json_build_object('ok', false, 'reason', 'INSUFFICIENT_STONES', 'balance', v_balance);
    END IF;

    UPDATE public.users SET spirit_stones = spirit_stones - p_cost WHERE id = v_user;
    INSERT INTO public.spirit_stone_transactions (user_id, amount, type, description, reference_id)
    VALUES (v_user, -p_cost, 'SPEND', 'Purchased ' || p_item_type || ': ' || p_item_id, p_item_id);

    IF p_item_type = 'TITLE' THEN
        INSERT INTO public.user_titles (user_id, title_id, title_name)
        VALUES (v_user, p_item_id, p_item_id)
        ON CONFLICT (user_id, title_id) DO NOTHING;
    END IF;

    RETURN json_build_object('ok', true, 'balance', v_balance - p_cost);
END;
$$;
GRANT EXECUTE ON FUNCTION public.spend_stones(TEXT, TEXT, INT) TO authenticated;
""".trimIndent()
    }

    fun exportConfig(): String {
        return supabasePreferences.exportConfigJson()
    }

    fun importConfig(text: String): Boolean {
        val success = supabasePreferences.importConfigJson(text)
        if (success) {
            loadConfiguration()
        }
        return success
    }

    fun setShowImportDialog(show: Boolean) {
        updateState { it.copy(showImportDialog = show, importInputText = "") }
    }

    fun setImportInputText(text: String) {
        updateState { it.copy(importInputText = text) }
    }

    fun setShowShareDialog(show: Boolean) {
        updateState { it.copy(showShareDialog = show) }
    }

    fun setCopyStatus(message: String?) {
        updateState { it.copy(copyStatusMessage = message) }
    }

    fun setUseCustomSupabase(useCustom: Boolean) {
        updateState { it.copy(useCustomSupabase = useCustom) }
        scope.launch {
            supabasePreferences.useCustomSupabase().set(useCustom)
        }
    }
    
    fun fillAllWithSame(url: String, apiKey: String) {
        updateState { it.copy(
            authUrl = url,
            authApiKey = apiKey,
            readingUrl = url,
            readingApiKey = apiKey,
            libraryUrl = url,
            libraryApiKey = apiKey,
            bookReviewsUrl = url,
            bookReviewsApiKey = apiKey,
            chapterReviewsUrl = url,
            chapterReviewsApiKey = apiKey,
            badgesUrl = url,
            badgesApiKey = apiKey,
            analyticsUrl = url,
            analyticsApiKey = apiKey,
            communityUrl = url,
            communityApiKey = apiKey
        )}
    }
    
    fun setAutoSync(enabled: Boolean) {
        updateState { it.copy(autoSyncEnabled = enabled) }
        scope.launch {
            supabasePreferences.autoSyncEnabled().set(enabled)
        }
    }
    
    fun setWifiOnly(wifiOnly: Boolean) {
        updateState { it.copy(syncOnWifiOnly = wifiOnly) }
        scope.launch {
            supabasePreferences.syncOnWifiOnly().set(wifiOnly)
        }
    }
    
    fun saveConfiguration() {
        scope.launch {
            try {
                // Save all 7 project configurations
                supabasePreferences.supabaseAuthUrl().set(currentState.authUrl)
                supabasePreferences.supabaseAuthKey().set(currentState.authApiKey)
                supabasePreferences.supabaseReadingUrl().set(currentState.readingUrl)
                supabasePreferences.supabaseReadingKey().set(currentState.readingApiKey)
                supabasePreferences.supabaseLibraryUrl().set(currentState.libraryUrl)
                supabasePreferences.supabaseLibraryKey().set(currentState.libraryApiKey)
                supabasePreferences.supabaseBookReviewsUrl().set(currentState.bookReviewsUrl)
                supabasePreferences.supabaseBookReviewsKey().set(currentState.bookReviewsApiKey)
                supabasePreferences.supabaseChapterReviewsUrl().set(currentState.chapterReviewsUrl)
                supabasePreferences.supabaseChapterReviewsKey().set(currentState.chapterReviewsApiKey)
                supabasePreferences.supabaseBadgesUrl().set(currentState.badgesUrl)
                supabasePreferences.supabaseBadgesKey().set(currentState.badgesApiKey)
                supabasePreferences.supabaseAnalyticsUrl().set(currentState.analyticsUrl)
                supabasePreferences.supabaseAnalyticsKey().set(currentState.analyticsApiKey)
                supabasePreferences.supabaseCommunityUrl().set(currentState.communityUrl)
                supabasePreferences.supabaseCommunityKey().set(currentState.communityApiKey)
                
                updateState { it.copy(
                    testResult = "? Configuration saved successfully! Total storage: 3.5GB",
                    error = null
                )}
            } catch (e: Exception) {
                updateState { it.copy(
                    error = "Failed to save configuration: ${e.message}"
                )}
            }
        }
    }
    
    fun clearPersonalConfig() {
        scope.launch {
            supabasePreferences.userSupabaseUrl().set("")
            supabasePreferences.userSupabaseAnonKey().set("")
            supabasePreferences.supabaseLibraryUrl().set("")
            supabasePreferences.supabaseLibraryKey().set("")
            supabasePreferences.supabaseReadingUrl().set("")
            supabasePreferences.supabaseReadingKey().set("")
            updateState {
                it.copy(
                    singleProjectUrl = "",
                    singleProjectKey = "",
                    isPersonalConfigured = false,
                    testResult = "Personal Supabase configuration cleared."
                )
            }
        }
    }

    fun testConnection() {
        scope.launch {
            updateState { it.copy(isTesting = true, testResult = null) }
            
            try {
                if (currentState.isSingleProjectMode) {
                    val url = currentState.singleProjectUrl.trim()
                    val key = currentState.singleProjectKey.trim()
                    if (url.isBlank() || key.isBlank()) {
                        updateState {
                            it.copy(
                                isTesting = false,
                                testResult = "✗ Please enter both Supabase Project URL and API Key before testing."
                            )
                        }
                        return@launch
                    }
                    // Temporarily or permanently ensure prefs are updated to test
                    saveSingleProjectConfig()
                    val result = remoteRepository.getSyncManifest("test_probe")
                    if (result.isSuccess) {
                        updateState {
                            it.copy(
                                isTesting = false,
                                testResult = "✓ Connection successful! Personal Supabase is ready for sync."
                            )
                        }
                        return@launch
                    } else {
                        val errMsg = result.exceptionOrNull()?.message ?: "Unknown error"
                        updateState {
                            it.copy(
                                isTesting = false,
                                testResult = "✗ Connection failed: $errMsg. (Have you run the Setup SQL script?)"
                            )
                        }
                        return@launch
                    }
                }

                // Multi-project test connection
                val result = remoteRepository.getCurrentUser()
                
                if (result.isSuccess) {
                    updateState { it.copy(
                        isTesting = false,
                        testResult = "✓ Connection successful! Supabase is configured correctly."
                    )}
                } else {
                    updateState { it.copy(
                        isTesting = false,
                        testResult = "✗ Connection failed: ${result.exceptionOrNull()?.message}"
                    )}
                }
            } catch (e: Exception) {
                updateState { it.copy(
                    isTesting = false,
                    testResult = "✗ Connection failed: ${e.message}"
                )}
            }
        }
    }
    
    fun triggerManualSync() {
        scope.launch {
            updateState { it.copy(isSyncing = true) }
            
            try {
                // Check if sync manager is available
                if (syncManager == null || bookRepository == null) {
                    updateState { it.copy(
                        isSyncing = false,
                        error = "Sync not available. Please restart the app."
                    )}
                    return@launch
                }
                
                // Get current user
                val userResult = remoteRepository.getCurrentUser()
                val user = userResult.getOrNull()
                
                if (user == null) {
                    updateState { it.copy(
                        isSyncing = false,
                        error = "Please sign in to sync"
                    )}
                    return@launch
                }
                
                // Get all books
                val books = bookRepository.findAllBooks()
                
                if (books.isEmpty()) {
                    updateState { it.copy(
                        isSyncing = false,
                        lastSyncTime = currentTimeToLong(),
                        error = "No books to sync"
                    )}
                    return@launch
                }
                
                // Perform full sync
                val syncResult = syncManager.performFullSync(user.id, books)
                
                if (syncResult.isSuccess) {
                    val currentTime = currentTimeToLong()
                    supabasePreferences.lastSyncTime().set(currentTime)
                    
                    val favoriteCount = books.count { it.favorite }
                    updateState { it.copy(
                        isSyncing = false,
                        lastSyncTime = currentTime,
                        testResult = "? Synced $favoriteCount favorite books successfully!",
                        error = null
                    )}
                } else {
                    updateState { it.copy(
                        isSyncing = false,
                        error = "Sync failed: ${syncResult.exceptionOrNull()?.message}"
                    )}
                }
            } catch (e: Exception) {
                updateState { it.copy(
                    isSyncing = false,
                    error = "Sync failed: ${e.message}"
                )}
            }
        }
    }
    
    fun clearError() {
        updateState { it.copy(error = null) }
    }
    
    // Individual project setters
    fun setAuthUrl(url: String) {
        updateState { it.copy(authUrl = url) }
    }
    
    fun setAuthApiKey(apiKey: String) {
        updateState { it.copy(authApiKey = apiKey) }
    }
    
    fun setReadingUrl(url: String) {
        updateState { it.copy(readingUrl = url) }
    }
    
    fun setReadingApiKey(apiKey: String) {
        updateState { it.copy(readingApiKey = apiKey) }
    }
    
    fun setLibraryUrl(url: String) {
        updateState { it.copy(libraryUrl = url) }
    }
    
    fun setLibraryApiKey(apiKey: String) {
        updateState { it.copy(libraryApiKey = apiKey) }
    }
    
    fun setBookReviewsUrl(url: String) {
        updateState { it.copy(bookReviewsUrl = url) }
    }
    
    fun setBookReviewsApiKey(apiKey: String) {
        updateState { it.copy(bookReviewsApiKey = apiKey) }
    }
    
    fun setChapterReviewsUrl(url: String) {
        updateState { it.copy(chapterReviewsUrl = url) }
    }
    
    fun setChapterReviewsApiKey(apiKey: String) {
        updateState { it.copy(chapterReviewsApiKey = apiKey) }
    }
    
    fun setBadgesUrl(url: String) {
        updateState { it.copy(badgesUrl = url) }
    }
    
    fun setBadgesApiKey(apiKey: String) {
        updateState { it.copy(badgesApiKey = apiKey) }
    }
    
    fun setAnalyticsUrl(url: String) {
        updateState { it.copy(analyticsUrl = url) }
    }
    
    fun setAnalyticsApiKey(apiKey: String) {
        updateState { it.copy(analyticsApiKey = apiKey) }
    }
    
    fun setCommunityUrl(url: String) {
        updateState { it.copy(communityUrl = url) }
    }
    
    fun setCommunityApiKey(apiKey: String) {
        updateState { it.copy(communityApiKey = apiKey) }
    }
}
