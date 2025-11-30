# This is a configuration file for R8

-dontobfuscate

# Keep extension's common dependencies
-keep class ireader.core.source.** { public protected *; }
-keep class ireader.core.http.** { public protected *; }
-keep class ireader.data.catalog.CatalogGithubApi { public protected *; }

# Keep catalog installation flow to prevent R8 from optimizing away flow collection
-keep class ireader.domain.catalogs.CatalogStore { *; }
-keep class ireader.domain.catalogs.service.CatalogInstallationChanges { *; }
-keep class ireader.domain.catalogs.service.CatalogInstallationChange { *; }
-keep class ireader.data.catalog.impl.AndroidCatalogInstallationChanges { *; }
-keepclassmembers class ireader.domain.catalogs.CatalogStore {
    kotlinx.coroutines.flow.MutableStateFlow catalogsFlow;
    java.util.List catalogs;
    <methods>;
}
-keepclassmembers class ireader.data.catalog.impl.AndroidCatalogInstallationChanges {
    kotlinx.coroutines.flow.MutableSharedFlow flow;
    <methods>;
}
-keep,allowoptimization class ireader.** { public protected *; }
-keep,allowoptimization class kotlinx.coroutines.** { public protected *; }
-keep,allowoptimization class androidx.preference.** { public protected *; }
-keep,allowoptimization class okhttp3.** { public protected *; }
-keep,allowoptimization class okio.** { public protected *; }
-keep,allowoptimization class org.jsoup.** { public protected *; }
-keep,allowoptimization class kotlin.** { public protected *; }
-keep,allowoptimization class io.ktor.** { public protected *; }
-keep,allowoptimization class android.content.pm.** { public protected *; }
-keep,allowoptimization class com.google.gson.** { public protected *; }
-keep,allowoptimization class androidx.lifecycle.** { public protected *; }
-keep,allowoptimization class androidx.work.** { public protected *; }
-keep,allowoptimization class androidx.hilt.** { public protected *; }
-keep,allowoptimization class androidx.datastore.** { public protected *; }
-keep,allowoptimization class org.jetbrains.kotlinx.** { public protected *; }
-keep,allowoptimization class app.cash.quickjs.** { public protected *; }
-keep,allowoptimization class com.google.accompanist.** { public protected *; }

# JS Plugin Support - Keep JavaScript engine classes
-keep class app.cash.quickjs.** { *; }
-keep class org.graalvm.** { *; }
-keep class ireader.domain.js.** { *; }
-keepclassmembers class ireader.domain.js.** {
    <methods>;
    <fields>;
}
-keep,allowoptimization class androidx.compose.** { public protected *; }
-keep,allowoptimization class org.tinylog.** { public protected *; }
-keep,allowoptimization class org.koin.** { public protected *; }
-keep,allowoptimization class app.cash.sqldelight.** { public protected *; }
-keep,allowoptimization class okhttp3.** { public protected *; }
-dontwarn okhttp3.internal.Util

-dontwarn android.support.**
-dontwarn androidx.**

-keepattributes SourceFile,
                LineNumberTable,
                RuntimeVisibleAnnotations,
                RuntimeVisibleParameterAnnotations,
                RuntimeVisibleTypeAnnotations,
                AnnotationDefault

-renamesourcefileattribute SourceFile

-dontwarn org.conscrypt.**


##---------------Begin: proguard configuration for couroutines  ----------
# When editing this file, update the following files as well:
# - META-INF/com.android.tools/proguard/coroutines.pro
# - META-INF/com.android.tools/r8/coroutines.pro

# ServiceLoader support
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Most of volatile fields are updated with AFU and should not be mangled
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Same story for the standard library's SafeContinuation that also uses AtomicReferenceFieldUpdater
-keepclassmembers class kotlin.coroutines.SafeContinuation {
    volatile <fields>;
}




##---------------End: proguard configuration for Couroutines  ----------

##---------------Begin: proguard configuration for Okhttp  ----------
#Okhttp
# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**

# A resource is loaded with a relative path so the package of this class must be preserved.
-adaptresourcefilenames okhttp3/internal/publicsuffix/PublicSuffixDatabase.gz

# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

# OkHttp platform used only on JVM and when Conscrypt and other security providers are available.
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
##---------------End: proguard configuration for Okhttp  ----------
##---------------Begin: proguard configuration for okio  ----------

# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*
##---------------End: proguard configuration for okio  ----------

##---------------Begin: proguard configuration for Ktor  ----------
# Ktor
-keep class io.ktor.** { *; }
-keep class kotlinx.coroutines.** { *; }
-keep class com.typesafe.** { *; }
-dontwarn kotlinx.atomicfu.**
-dontwarn io.netty.**
-dontwarn com.typesafe.**
-dontwarn org.slf4j.**
##---------------End: proguard configuration for Ktor  ----------


#---------
# Keep trakt-java and tmdb-java entity names (for GSON)
-keep class ireader.common.models.*.entities.** {
    <fields>;
    <init>(...);
}
-keep class ireader.common.models.*.entities.** {
    <fields>;
    <init>(...);
}

-keep class org.ireader.data.catalog.** {
    kotlinx.serialization.KSerializer serializer(...);
    <fields>;
    <init>(...);
}
-keep class ireader.common.models.** {
    kotlinx.serialization.KSerializer serializer(...);
    <fields>;
    <init>(...);
}


##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*
-dontnote kotlinx.serialization.* # core serialization annotations

# Gson specific classes
-dontwarn sun.misc.**


##---------------End: proguard configuration for Gson  ----------

##---------------Begin: proguard configuration for kotlinx.serialization  ----------
-keepattributes *Annotation*, InnerClasses

# kotlinx-serialization-json specific.
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class ireader.**$$serializer { *; }
-keepclassmembers class org.ireader.** {
    *** Companion;
}
-keepclasseswithmembers class ireader.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep class kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.** {
    <methods>;
}

# Keep Gemini API model classes for proper serialization
-keep class ireader.domain.usecases.translate.WebscrapingTranslateEngine$GeminiRequest { *; }
-keep class ireader.domain.usecases.translate.WebscrapingTranslateEngine$GeminiContent { *; }
-keep class ireader.domain.usecases.translate.WebscrapingTranslateEngine$GeminiPart { *; }
-keep class ireader.domain.usecases.translate.WebscrapingTranslateEngine$GeminiGenerationConfig { *; }
-keep class ireader.domain.usecases.translate.WebscrapingTranslateEngine$GeminiResponse { *; }
-keep class ireader.domain.usecases.translate.WebscrapingTranslateEngine$GeminiCandidate { *; }
-keep class ireader.domain.usecases.translate.WebscrapingTranslateEngine$CitationMetadata { *; }
-keep class ireader.domain.usecases.translate.WebscrapingTranslateEngine$CitationSource { *; }
-keep class ireader.domain.usecases.translate.WebscrapingTranslateEngine$GeminiModelsResponse { *; }
-keep class ireader.domain.usecases.translate.WebscrapingTranslateEngine$GeminiModelInfo { *; }

##---------------End: proguard configuration for kotlinx.serialization  ----------
# Voyager rules removed - using pure Compose Navigation

-keep class com.oracle.svm.core.annotate.*
-keepclassmembers class com.oracle.svm.core.annotate.** {
    <methods>;
}
-keep class com.oracle.svm.core.configure.*
-keepclassmembers class com.oracle.svm.core.configure.** {
    <methods>;
}
-keep class dalvik.system.*
-keepclassmembers class dalvik.system.** {
    <methods>;
}
-keep class java.beans.*
-keepclassmembers class java.beans.** {
    <methods>;
}
-keep class java.lang.*
-keepclassmembers class java.lang.** {
    <methods>;
}
-keep class javax.naming.*
-keepclassmembers class javax.naming.** {
    <methods>;
}
-keep class javax.xml.stream.*
-keepclassmembers class javax.xml.stream.** {
    <methods>;
}

-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken
-keep public class * implements java.lang.reflect.Type

-dontwarn com.oracle.svm.core.annotate.**
-dontwarn com.oracle.svm.core.configure.**
-dontwarn dalvik.system.**
-dontwarn java.beans.**
-dontwarn java.beans.**
-dontwarn java.lang.**
-dontwarn javax.naming.**
-dontwarn javax.xml.stream.**
-dontwarn org.graalvm.nativeimage.**
-dontwarn sun.reflect.**

# Ignore Java AWT classes (desktop-only, not available on Android)
# These classes are only used in desktop builds and should not be included in Android
-dontwarn java.awt.**
-dontwarn javax.imageio.**
-dontnote java.awt.**
-dontnote javax.imageio.**

# Tell R8 to ignore missing AWT classes that are referenced from multiplatform code
# but only used in desktop builds
-dontwarn java.awt.image.BufferedImage
-dontwarn javax.imageio.ImageIO

# Prevent R8 from failing on missing desktop-only classes
# The desktop implementation should not be included in Android builds
-dontwarn ireader.domain.js.update.JSPluginUpdateNotifier_desktopKt
-dontwarn ireader.domain.js.update.JSPluginUpdateNotifier$Companion

# Keep rules to prevent R8 from trying to optimize desktop source set classes
-keep,allowshrinking class ireader.domain.js.update.JSPluginUpdateNotifier { *; }

##---------------Begin: proguard configuration for J2V8  ----------
# J2V8 - Keep core V8 classes but remove inspector (debug-only)
-keep class com.eclipsesource.v8.** { *; }
-dontwarn com.eclipsesource.v8.debug.**
-dontwarn com.eclipsesource.v8.inspector.**

# Remove V8 inspector classes completely (only needed for debugging)
-assumenosideeffects class com.eclipsesource.v8.inspector.** {
    *;
}
##---------------End: proguard configuration for J2V8  ----------

##---------------Begin: proguard configuration for Supabase  ----------
# Supabase - Keep all Supabase classes for proper API communication
-keep class io.github.jan.supabase.** { *; }
-keepclassmembers class io.github.jan.supabase.** {
    <fields>;
    <methods>;
}

# Keep Supabase serialization classes - CRITICAL for API communication
# Keep all fields annotated with @SerialName to prevent field name obfuscation
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

# Keep all @Serializable classes completely intact
-keep @kotlinx.serialization.Serializable class ** { *; }
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    <fields>;
    <init>(...);
}

# Keep Supabase Realtime classes
-keep class io.github.jan.supabase.realtime.** { *; }
-keep class io.github.jan.supabase.postgrest.** { *; }
-keep class io.github.jan.supabase.storage.** { *; }
-keep class io.github.jan.supabase.gotrue.** { *; }

# Keep Kotlin reflection for Supabase (fixes KotlinReflectionInternalError)
-keep class kotlin.reflect.** { *; }
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-keepattributes RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations,RuntimeVisibleParameterAnnotations,RuntimeInvisibleParameterAnnotations

# Keep reflection for List and Collection types used by Supabase and ML Kit
-keep class java.util.** { *; }
-keep interface java.util.**
-keepclassmembers class java.util.** {
    <fields>;
    <methods>;
}

# Keep Kotlin reflection implementation classes
-keep class kotlin.reflect.jvm.** { *; }
-keep class kotlin.reflect.full.** { *; }
-keepclassmembers class kotlin.reflect.jvm.internal.** {
    <fields>;
    <methods>;
}

# Keep Kotlin reflection internal classes to prevent KotlinReflectionInternalError
-keep class kotlin.reflect.jvm.internal.** { *; }
-keep class kotlin.reflect.jvm.internal.KClassImpl { *; }
-keep class kotlin.reflect.jvm.internal.KClassImpl$Data { *; }
-keepclassmembers class kotlin.reflect.jvm.internal.KClassImpl$Data {
    *;
}

# Keep all Java reflection classes
-keep class java.lang.reflect.** { *; }
-keepclassmembers class java.lang.reflect.** {
    <fields>;
    <methods>;
}

# Keep User model and all its fields (especially isAdmin for admin features)
-keep class ireader.domain.models.remote.User { *; }
-keepclassmembers class ireader.domain.models.remote.User {
    <fields>;
    <init>(...);
}

# Keep Badge models and all their fields (especially imageUrl for badge images)
-keep class ireader.domain.models.remote.Badge { *; }
-keepclassmembers class ireader.domain.models.remote.Badge {
    <fields>;
    <init>(...);
}
-keep class ireader.domain.models.remote.UserBadge { *; }
-keepclassmembers class ireader.domain.models.remote.UserBadge {
    <fields>;
    <init>(...);
}
-keep class ireader.domain.models.remote.BadgeType { *; }
-keep class ireader.domain.models.remote.BadgeRarity { *; }

# Keep all Supabase repository DTOs for proper serialization (used with reflection)
# These nested classes are accessed via Kotlin reflection by Supabase's decodeList/decodeSingle

# Remote Repository DTOs (including UserDto with is_admin field)
-keep class ireader.data.remote.SupabaseRemoteRepository { *; }
-keep class ireader.data.remote.SupabaseRemoteRepository$** { *; }
-keepclassmembers class ireader.data.remote.SupabaseRemoteRepository$** {
    *;
}

# Review Repository DTOs
-keep class ireader.data.review.ReviewRepositoryImpl { *; }
-keep class ireader.data.review.ReviewRepositoryImpl$** { *; }
-keepclassmembers class ireader.data.review.ReviewRepositoryImpl$** {
    *;
}

# Badge Repository DTOs (keep all fields including image_url for badge images)
-keep class ireader.data.badge.BadgeRepositoryImpl { *; }
-keep class ireader.data.badge.BadgeRepositoryImpl$** { *; }
-keepclassmembers class ireader.data.badge.BadgeRepositoryImpl$** {
    *;
}
# Specifically keep image URL fields in badge DTOs
-keepclassmembers class ireader.data.badge.BadgeRepositoryImpl$*Dto {
    *** imageUrl;
    *** image_url;
    *** badgeImageUrl;
    *** badge_image_url;
}

# Leaderboard Repository DTOs - CRITICAL: Keep all nested classes and their @SerialName annotations
-keep class ireader.data.leaderboard.LeaderboardRepositoryImpl { *; }
-keep class ireader.data.leaderboard.LeaderboardRepositoryImpl$** { *; }
-keepclassmembers class ireader.data.leaderboard.LeaderboardRepositoryImpl$** {
    *;
}

# Explicitly keep LeaderboardDto and all its serialization annotations
-keep class ireader.data.leaderboard.LeaderboardRepositoryImpl$LeaderboardDto { *; }
-keepclassmembers class ireader.data.leaderboard.LeaderboardRepositoryImpl$LeaderboardDto {
    @kotlinx.serialization.SerialName <fields>;
    <fields>;
    <init>(...);
}

# NFT Repository DTOs
-keep class ireader.data.nft.NFTRepositoryImpl { *; }
-keep class ireader.data.nft.NFTRepositoryImpl$** { *; }
-keepclassmembers class ireader.data.nft.NFTRepositoryImpl$** {
    *;
}

-dontwarn io.github.jan.supabase.**
##---------------End: proguard configuration for Supabase  ----------

##---------------Begin: proguard configuration for SQLDelight  ----------
# SQLDelight - Keep database classes
-keep class app.cash.sqldelight.** { *; }
-keep class ir.kazemcodes.infinityreader.** { *; }
-keepclassmembers class ir.kazemcodes.infinityreader.** {
    <fields>;
    <methods>;
}

# Keep SQLDelight driver classes
-keep class app.cash.sqldelight.driver.** { *; }
-dontwarn app.cash.sqldelight.**
##---------------End: proguard configuration for SQLDelight  ----------

##---------------Begin: proguard configuration for AndroidX Security  ----------
# AndroidX Security Crypto - Keep encryption classes
-keep class androidx.security.crypto.** { *; }
-keepclassmembers class androidx.security.crypto.** {
    <fields>;
    <methods>;
}

# Keep EncryptedSharedPreferences and EncryptedFile
-keep class androidx.security.crypto.EncryptedSharedPreferences { *; }
-keep class androidx.security.crypto.EncryptedFile { *; }
-keep class androidx.security.crypto.MasterKey { *; }
##---------------End: proguard configuration for AndroidX Security  ----------

##---------------Begin: proguard configuration for Biometric  ----------
# AndroidX Biometric - Keep biometric authentication classes
-keep class androidx.biometric.** { *; }
-keepclassmembers class androidx.biometric.** {
    <fields>;
    <methods>;
}
##---------------End: proguard configuration for Biometric  ----------

##---------------Begin: proguard configuration for WebKit  ----------
# AndroidX WebKit - Keep WebView classes
-keep class androidx.webkit.** { *; }
-keepclassmembers class androidx.webkit.** {
    <fields>;
    <methods>;
}

# Keep JavaScript interface methods
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-dontwarn android.webkit.**
##---------------End: proguard configuration for WebKit  ----------

##---------------Begin: proguard configuration for Google ML Kit  ----------
# Google ML Kit Translation - Keep translation classes
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_translate.** { *; }
-keep class com.google.android.gms.tasks.** { *; }
-keepclassmembers class com.google.mlkit.** {
    <fields>;
    <methods>;
}
-keepclassmembers class com.google.android.gms.tasks.** {
    <fields>;
    <methods>;
}

# Keep ML Kit classes accessed via reflection in GoogleTranslateML
-keep class com.google.mlkit.nl.translate.** { *; }
-keep class com.google.mlkit.nl.translate.TranslatorOptions { *; }
-keep class com.google.mlkit.nl.translate.TranslatorOptions$Builder { *; }
-keep class com.google.mlkit.nl.translate.Translation { *; }
-keepclassmembers class com.google.mlkit.nl.translate.TranslatorOptions$Builder {
    public <methods>;
}

# Keep Google Play Services Task API for ML Kit
-keep interface com.google.android.gms.tasks.** { *; }
-keep class com.google.android.gms.tasks.OnSuccessListener { *; }
-keep class com.google.android.gms.tasks.OnFailureListener { *; }
-keep class com.google.android.gms.tasks.SuccessContinuation { *; }

-dontwarn com.google.mlkit.**
-dontwarn com.google.android.gms.internal.mlkit_translate.**
##---------------End: proguard configuration for Google ML Kit  ----------

##---------------Begin: proguard configuration for Coil  ----------
# Coil - Keep image loading classes
-keep class coil.** { *; }
-keep class coil3.** { *; }
-keepclassmembers class coil.** {
    <fields>;
    <methods>;
}
-keepclassmembers class coil3.** {
    <fields>;
    <methods>;
}

# Keep GIF decoder
-keep class coil.decode.GifDecoder { *; }
-keep class coil3.gif.** { *; }

# Keep Coil image loaders and fetchers
-keep class coil.fetch.** { *; }
-keep class coil3.fetch.** { *; }
-keep class coil.decode.** { *; }
-keep class coil3.decode.** { *; }
-keep class coil.request.** { *; }
-keep class coil3.request.** { *; }

# Keep Coil network components for loading remote images
-keep class coil.network.** { *; }
-keep class coil3.network.** { *; }

# Keep Coil disk cache
-keep class coil.disk.** { *; }
-keep class coil3.disk.** { *; }

# Keep Coil memory cache
-keep class coil.memory.** { *; }
-keep class coil3.memory.** { *; }

# Keep Coil util classes
-keep class coil.util.** { *; }
-keep class coil3.util.** { *; }

# Keep Coil compose integration
-keep class coil.compose.** { *; }
-keep class coil3.compose.** { *; }

-dontwarn coil.**
-dontwarn coil3.**
##---------------End: proguard configuration for Coil  ----------

##---------------Begin: proguard configuration for SimpleStorage  ----------
# SimpleStorage - Keep storage access classes
-keep class com.anggrayudi.storage.** { *; }
-keepclassmembers class com.anggrayudi.storage.** {
    <fields>;
    <methods>;
}
##---------------End: proguard configuration for SimpleStorage  ----------

##---------------Begin: proguard configuration for Koin  ----------
# Koin - Keep dependency injection classes
-keep class org.koin.** { *; }
-keepclassmembers class org.koin.** {
    <fields>;
    <methods>;
}

# Keep Koin modules and definitions
-keep class * extends org.koin.core.module.Module
-keepclassmembers class * {
    org.koin.core.module.Module *;
}

# Keep classes with Koin annotations
-keep @org.koin.core.annotation.* class * { *; }
##---------------End: proguard configuration for Koin  ----------

##---------------Begin: proguard configuration for Compose  ----------
# Jetpack Compose - Keep composable functions
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** {
    <fields>;
    <methods>;
}

# Keep @Composable annotated functions
-keep @androidx.compose.runtime.Composable class * { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# Keep Compose resources
-keep class org.jetbrains.compose.resources.** { *; }
##---------------End: proguard configuration for Compose  ----------

##---------------Begin: proguard configuration for Leaderboard & Statistics  ----------
# Keep leaderboard and statistics model classes for Supabase serialization
-keep class ireader.domain.models.entities.Leaderboard** { *; }
-keep class ireader.domain.models.entities.UserStats** { *; }
-keep class ireader.domain.models.entities.ReadingStats** { *; }
-keep class ireader.domain.repository.LeaderboardRepository** { *; }
-keepclassmembers class ireader.domain.models.entities.** {
    <fields>;
    <init>(...);
}
##---------------End: proguard configuration for Leaderboard & Statistics  ----------

#
#---------------Begin: Additional QuickJS Size Optimizations  ----------
# QuickJS optimization - Remove unused native libraries at build time
# This helps R8 identify and remove unused code
-assumenosideeffects class app.cash.quickjs.QuickJs {
    # Add methods here that you don't use if any
}

# Optimize QuickJS by removing debug symbols
-keepclassmembers class app.cash.quickjs.** {
    !private <methods>;
    !private <fields>;
}
##---------------End: Additional QuickJS Size Optimizations  ----------


##---------------Begin: proguard configuration for ProfileInstaller  ----------
# Keep ProfileInstaller receiver for baseline profiles and benchmark support
-keep class androidx.profileinstaller.** { *; }
-keep class androidx.profileinstaller.ProfileInstallReceiver { *; }
##---------------End: proguard configuration for ProfileInstaller  ----------

##---------------Begin: Startup Performance Optimizations  ----------
# Aggressive optimizations for faster startup
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# Remove logging in release builds for faster startup
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Remove println statements
-assumenosideeffects class kotlin.io.ConsoleKt {
    public static *** println(...);
}

# Optimize away debug code
-assumenosideeffects class io.github.aakira.napier.Napier {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
##---------------End: Startup Performance Optimizations  ----------
