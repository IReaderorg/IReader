# Aggressive size optimization rules for QuickJS
# Include this file in addition to proguard-rules.pro for maximum size reduction
# 
# WARNING: Be careful with -assumenosideeffects rules!
# Do NOT remove Kotlin reflection methods as they are used by Supabase for serialization.

# Optimize aggressively
-optimizationpasses 5
-allowaccessmodification
-mergeinterfacesaggressively
-overloadaggressivly

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# Remove Napier logging
-assumenosideeffects class io.github.aakira.napier.Napier {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# Remove Kotlin assertions (but keep reflection-related methods)
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void checkNotNull(...);
    public static void checkNotNullParameter(...);
    public static void checkNotNullExpressionValue(...);
    public static void checkExpressionValueIsNotNull(...);
    public static void checkReturnedValueIsNotNull(...);
    public static void checkFieldIsNotNull(...);
    public static void checkParameterIsNotNull(...);
    public static void throwUninitializedPropertyAccessException(...);
}

# Optimize Kotlin metadata
-dontwarn kotlin.Metadata
-keepattributes RuntimeVisibleAnnotations

# DISABLED: Do NOT remove Kotlin reflection - it's used by Supabase!
# -assumenosideeffects class kotlin.jvm.internal.Reflection {
#     public static *** getOrCreateKotlinClass(...);
#     public static *** getOrCreateKotlinPackage(...);
# }

# Optimize QuickJS - remove debug features
-assumenosideeffects class app.cash.quickjs.QuickJs {
    # Remove any debug/logging methods if they exist
}

# Remove Firebase Analytics debug logging (if using Firebase)
-assumenosideeffects class com.google.firebase.analytics.FirebaseAnalytics {
    public void setAnalyticsCollectionEnabled(boolean);
}

# Optimize Compose - remove debug features
-assumenosideeffects class androidx.compose.runtime.ComposerKt {
    void sourceInformation(...);
    void sourceInformationMarkerStart(...);
    void sourceInformationMarkerEnd(...);
}

# Remove Ktor logging
-assumenosideeffects class io.ktor.client.plugins.logging.** {
    *;
}

# Optimize coroutines - remove debug features
-assumenosideeffects class kotlinx.coroutines.debug.** {
    *;
}

# Remove OkHttp logging
-assumenosideeffects class okhttp3.internal.platform.Platform {
    public void log(...);
}

# Optimize string concatenation
-optimizations !code/simplification/string

# Remove parameter names (reduces metadata)
-keepparameternames

# Repackage classes to reduce DEX size
-repackageclasses ''
-flattenpackagehierarchy
