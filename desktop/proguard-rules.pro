# This is a configuration file for R8

-dontobfuscate

# Keep extension's common dependencies
-keep class ireader.core.source.** { public protected *; }
-keep class ireader.core.http.** { public protected *; }
-keep,allowoptimization class ireader.** { public protected *; }
-keep,allowoptimization class kotlinx.coroutines.** { public protected *; }
-keep,allowoptimization class androidx.preference.** { public protected *; }
-keep,allowoptimization class okhttp3.** { public protected *; }
-keep,allowoptimization class okio.** { public protected *; }
-keep,allowoptimization class org.jsoup.** { public protected *; }
-keep,allowoptimization class kotlin.** { public protected *; }
-keep,allowoptimization class io.ktor.** { public protected *; }
-keep,allowoptimization class com.google.gson.** { public protected *; }
-keep,allowoptimization class org.jetbrains.kotlinx.** { public protected *; }
-keep,allowoptimization class app.cash.quickjs.** { public protected *; }
-keep,allowoptimization class com.google.accompanist.** { public protected *; }
-keep,allowoptimization class org.tinylog.** { public protected *; }
-keep,allowoptimization class org.koin.** { public protected *; }
-keep,allowoptimization class app.cash.sqldelight.** { public protected *; }

##---------------Begin: Tsundoku / Tachiyomi Extension Support ----------
-keep,allowoptimization class eu.kanade.**
-keep,allowoptimization class tachiyomi.**
-keep,allowoptimization class mihon.**

# Keep common dependencies used in extensions
-keep,allowoptimization class kotlin.time.** { public protected *; }
-keep,allowoptimization class rx.** { public protected *; }
-keep,allowoptimization class com.dokar.quickjs.** { public protected *; }
-keep class com.dokar.quickjs.MemoryUsage { *; }
-keepclassmembers class kotlin.UByteArray { <init>(...); }
-keep class uy.kohesive.injekt.** { *; }
-dontwarn uy.kohesive.injekt.**

# Extension models, online sources, and interfaces
-keep class eu.kanade.tachiyomi.source.model.** { public protected *; }
-keep class eu.kanade.tachiyomi.source.online.** { public protected *; }
-keep class eu.kanade.tachiyomi.source.** extends eu.kanade.tachiyomi.source.Source { public protected *; }
-keep,allowoptimization class eu.kanade.tachiyomi.util.JsoupExtensionsKt { public protected *; }

# Extension network & utilities (from extensions-lib)
-keep,allowoptimization class eu.kanade.tachiyomi.network.interceptor.RateLimitInterceptorKt { public protected *; }
-keep,allowoptimization class eu.kanade.tachiyomi.network.interceptor.SpecificHostRateLimitInterceptorKt { public protected *; }
-keep,allowoptimization class eu.kanade.tachiyomi.network.NetworkHelper { public protected *; }
-keep,allowoptimization class eu.kanade.tachiyomi.network.OkHttpExtensionsKt { public protected *; }
-keep,allowoptimization class eu.kanade.tachiyomi.network.RequestsKt { public protected *; }
-keep,allowoptimization class eu.kanade.tachiyomi.AppInfo { public protected *; }

-keepclassmembers class * implements java.io.Serializable {
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
##---------------End: Tsundoku / Tachiyomi Extension Support ----------

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


# Most of volatile fields are updated with AFU and should not be mangled
-keepclassmembers class kotlinx.coroutines.** {
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

# OkHttp Multipart
-keepclasseswithmembers class okhttp3.MultipartBody$Builder { *; }

# OkHttp Zstandard (zstd) decompression/compression (CRITICAL: native JNI createJniZstd in libzstd-kmp.so reflects ZstdCompressor/ZstdDecompressor)
-keep class com.squareup.zstd.** { *; }
-dontwarn com.squareup.zstd.**
-keep class okhttp3.zstd.** { *; }
-dontwarn okhttp3.zstd.**

# OkHttp Brotli compression/decompression
-keep class okhttp3.brotli.** { *; }
-dontwarn okhttp3.brotli.**
-keep class org.brotli.** { *; }
-dontwarn org.brotli.**
##---------------End: proguard configuration for Okhttp  ----------

##---------------Begin: proguard configuration for RxJava 1.x (Tsundoku Extensions) ----------
-dontwarn sun.misc.**

-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
   long producerIndex;
   long consumerIndex;
}

-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}

-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}

-dontnote rx.internal.util.PlatformDependent
##---------------End: proguard configuration for RxJava 1.x (Tsundoku Extensions) ----------

##---------------Begin: proguard configuration for kotlinx.serialization (Tsundoku Extensions) ----------
-keep,includedescriptorclasses class eu.kanade.**$$serializer { *; }
-keepclassmembers class eu.kanade.** {
    *** Companion;
}
-keepclasseswithmembers class eu.kanade.** {
    kotlinx.serialization.KSerializer serializer(...);
}
##---------------End: proguard configuration for kotlinx.serialization (Tsundoku Extensions) ----------
##---------------Begin: proguard configuration for okio  ----------

# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*
##---------------End: proguard configuration for okio  ----------

##---------------Begin: proguard configuration for Ktor  ----------
# Ktor
-keep class io.ktor.** { *; }
-keep class kotlinx.coroutines.** { *; }
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




##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*


# Gson specific classes
-dontwarn sun.misc.**


##---------------End: proguard configuration for Gson  ----------

##---------------Begin: proguard configuration for kotlinx.serialization  ----------
-keepattributes *Annotation*, InnerClasses

# kotlinx-serialization-json specific.
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}


-keep,includedescriptorclasses class ireader.**$$serializer { *; }
-keepclassmembers class org.ireader.** {
    *** Companion;
}


-keep class kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.** {
    <methods>;
}

##---------------End: proguard configuration for kotlinx.serialization  ----------

# Log4J
-dontwarn org.apache.logging.log4j.**
-keep,includedescriptorclasses class org.apache.logging.log4j.** { *; }
# tinylog
-dontwarn org.tinylog.**.**
-keep,includedescriptorclasses class org.tinylog.**
# antlr
-dontwarn org.antlr.runtime.**.**
-keep,includedescriptorclasses class org.antlr.runtime.**

-allowaccessmodification
-dontusemixedcaseclassnames
-verbose

-keepattributes *Annotation*

-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}
-keepclassmembers class * { public <init>(...); }

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}


