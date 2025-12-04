# IReader-Extensions Migration Plan

This document outlines the detailed steps needed to migrate IReader-extensions to support iOS via Kotlin/JS while maintaining Android/Desktop compatibility.

## Overview

**Goal**: Update IReader-extensions to produce both:
- APK/JAR files for Android/Desktop (existing)
- JS bundles for iOS (new)

**Key Changes**:
1. Update source-api dependency to new version with Ksoup
2. Migrate all sources from Jsoup to Ksoup imports
3. Add JS target to source build configurations
4. Create JS init files for each source
5. Update CI/CD workflow to build JS bundles

---

## Phase 1: Update Dependencies

### 1.1 Update `gradle/libs.versions.toml`

```toml
[versions]
ireader = "1.5.0"  # Update to new version with Ksoup & JS support
ktor = "3.3.2"

[libraries]
# Update source-api to new version
ireader-core = { module = "io.github.ireaderorg:source-api", version.ref = "ireader" }

# Add Ksoup (replaces Jsoup for KMP)
ksoup = { module = "com.fleeksoft.ksoup:ksoup", version = "0.2.2" }
ksoup-network = { module = "com.fleeksoft.ksoup:ksoup-network", version = "0.2.2" }

# Add Ktor JS client
ktor-client-js = { module = "io.ktor:ktor-client-js", version.ref = "ktor" }
```

### 1.2 Update `common/build.gradle.kts`

```kotlin
plugins {
    kotlin("multiplatform")  // Change from kotlin("jvm")
    kotlin("plugin.serialization")
}

kotlin {
    jvm()  // For Android/Desktop
    
    js(IR) {
        browser {
            webpackTask {
                mainOutputFileName = "common.js"
            }
        }
        binaries.library()
    }
    
    sourceSets {
        commonMain.dependencies {
            compileOnly(libs.stdlib)
            compileOnly(libs.ksoup)  // Replace jsoup
            compileOnly(libs.ireader.core)
            compileOnly(libs.ktor.core)
        }
        
        jvmMain.dependencies {
            compileOnly(libs.ktor.cio)
        }
        
        jsMain.dependencies {
            compileOnly(libs.ktor.client.js)
        }
    }
}
```

---

## Phase 2: Migrate Common Utilities

### 2.1 Files to Migrate in `common/src/main/kotlin/`

Move files to `commonMain` and update imports:

| File | Changes Needed |
|------|----------------|
| `DateParser.kt` | Use `kotlinx.datetime` instead of `java.text.SimpleDateFormat` |
| `HtmlCleaner.kt` | Update Jsoup → Ksoup imports |
| `StatusParser.kt` | No changes (pure Kotlin) |
| `UrlBuilder.kt` | No changes (pure Kotlin) |
| `ImageUrlHelper.kt` | No changes (pure Kotlin) |
| `SelectorConstants.kt` | No changes (pure Kotlin) |
| `ErrorHandler.kt` | No changes (pure Kotlin) |
| `RateLimiter.kt` | Use `kotlinx.datetime` for time |

### 2.2 Import Changes

**Before (Jsoup):**
```kotlin
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
```

**After (Ksoup):**
```kotlin
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.select.Elements
```

### 2.3 Date Parsing Migration

**Before (JVM-only):**
```kotlin
import java.text.SimpleDateFormat
import java.util.Locale

fun parseDate(dateString: String, format: String): Long {
    return SimpleDateFormat(format, Locale.US).parse(dateString)?.time ?: 0L
}
```

**After (KMP):**
```kotlin
import kotlinx.datetime.*

fun parseDate(dateString: String, format: String): Long {
    // Use kotlinx-datetime or manual parsing
    return try {
        // Simple ISO format
        Instant.parse(dateString).toEpochMilliseconds()
    } catch (e: Exception) {
        Clock.System.now().toEpochMilliseconds()
    }
}
```

---

## Phase 3: Migrate Individual Sources

### 3.1 Source Directory Structure

**Current:**
```
sources/en/novelupdates/
├── main/
│   └── src/
│       └── ireader/novelupdates/
│           └── NovelUpdates.kt
└── build.gradle.kts
```

**After Migration:**
```
sources/en/novelupdates/
├── src/
│   ├── commonMain/
│   │   └── kotlin/ireader/novelupdates/
│   │       └── NovelUpdates.kt
│   └── jsMain/
│       └── kotlin/ireader/novelupdates/
│           └── Init.kt
└── build.gradle.kts
```

### 3.2 Update Source `build.gradle.kts`

**Before:**
```kotlin
plugins {
    id("ireader.extension")
}

// Extension configuration...
```

**After:**
```kotlin
plugins {
    id("ireader.extension")
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm()  // For Android/Desktop (existing)
    
    js(IR) {
        browser {
            webpackTask {
                mainOutputFileName = "${project.name}.js"
            }
        }
        binaries.executable()
    }
    
    sourceSets {
        commonMain.dependencies {
            implementation(project(":common"))
            implementation(libs.ireader.core)
            implementation(libs.ksoup)
            implementation(libs.ktor.core)
        }
        
        jsMain.dependencies {
            // JS-specific dependencies if needed
        }
    }
}

// Keep existing extension configuration for Android...
```

### 3.3 Create JS Init File

For each source, create `src/jsMain/kotlin/<package>/Init.kt`:

```kotlin
package ireader.novelupdates

import ireader.js.runtime.registerSource
import ireader.js.runtime.JsDependencies
import kotlin.js.JsExport

/**
 * Initialize NovelUpdates source for iOS/JS runtime.
 */
@JsExport
@OptIn(ExperimentalJsExport::class)
fun initNovelUpdates() {
    registerSource("novelupdates") { deps ->
        NovelUpdates(deps.toDependencies())
    }
    console.log("NovelUpdates source registered")
}
```

### 3.4 Source Code Migration Checklist

For each source file:

- [ ] Move from `main/src/` to `src/commonMain/kotlin/`
- [ ] Update Jsoup imports to Ksoup
- [ ] Replace `Jsoup.parse()` with `Ksoup.parse()`
- [ ] Replace `Jsoup.connect()` with Ktor HTTP client
- [ ] Update date parsing to use kotlinx-datetime
- [ ] Remove any JVM-specific code
- [ ] Create `Init.kt` in jsMain

---

## Phase 4: Update Build System

### 4.1 Update `buildSrc` Plugin

Modify the `ireader.extension` plugin to support multiplatform:

```kotlin
// buildSrc/src/main/kotlin/ireader.extension.gradle.kts

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library")  // For Android APK generation
}

kotlin {
    // JVM target for Android/Desktop
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
    }
    
    // JS target for iOS
    js(IR) {
        browser {
            webpackTask {
                mainOutputFileName = "${project.name}.js"
                output.libraryTarget = "commonjs2"
            }
        }
        binaries.executable()
    }
    
    sourceSets {
        commonMain.dependencies {
            implementation(project(":common"))
        }
    }
}
```

### 4.2 Update Root `build.gradle.kts`

Add JS build tasks:

```kotlin
// Root build.gradle.kts

tasks.register("buildAllJsSources") {
    description = "Build all sources as JS bundles for iOS"
    group = "build"
    
    dependsOn(
        subprojects
            .filter { it.path.startsWith(":sources:") }
            .map { "${it.path}:jsBrowserProductionWebpack" }
    )
}

tasks.register("packageJsSources") {
    description = "Package all JS sources for distribution"
    group = "distribution"
    
    dependsOn("buildAllJsSources")
    
    doLast {
        val outputDir = file("build/js-dist")
        outputDir.mkdirs()
        
        // Copy all JS bundles
        subprojects
            .filter { it.path.startsWith(":sources:") }
            .forEach { project ->
                val jsDir = project.file("build/dist/js/productionExecutable")
                if (jsDir.exists()) {
                    copy {
                        from(jsDir)
                        into(outputDir.resolve(project.name))
                    }
                }
            }
        
        // Create index.json
        // ...
    }
}
```

---

## Phase 5: Update CI/CD Workflow

### 5.1 Update `.github/workflows/build.yml`

Add JS build job:

```yaml
jobs:
  build-android:
    # ... existing Android build ...
    
  build-js:
    name: Build JS Sources
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v4
      
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
          
      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v3
        
      - name: Build JS Sources
        run: ./gradlew buildAllJsSources --no-daemon
        
      - name: Package JS Sources
        run: ./gradlew packageJsSources --no-daemon
        
      - name: Upload JS Artifacts
        uses: actions/upload-artifact@v4
        with:
          name: js-sources
          path: build/js-dist/
          
  deploy:
    needs: [build-android, build-js]
    # ... deploy to CDN ...
```

---

## Phase 6: Source-by-Source Migration

### Priority Order

Migrate sources in this order based on popularity and complexity:

**Tier 1 - High Priority (Simple Sources):**
1. NovelUpdates
2. RoyalRoad
3. ScribbleHub
4. WebNovel
5. LightNovelPub

**Tier 2 - Medium Priority:**
6. Wuxiaworld
7. NovelFull
8. ReadLightNovel
9. BoxNovel
10. FreeWebNovel

**Tier 3 - Lower Priority (Complex/API-based):**
11. Sources with custom APIs
12. Sources with Cloudflare protection
13. Sources with complex JavaScript rendering

### Migration Template

For each source, follow this template:

```kotlin
// src/commonMain/kotlin/ireader/<source>/<SourceName>.kt

package ireader.<source>

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import ireader.core.source.Dependencies
import ireader.core.source.SourceFactory
import ireader.core.source.model.*

@Extension
abstract class <SourceName>(deps: Dependencies) : SourceFactory(deps) {
    
    override val name: String = "<Source Name>"
    override val baseUrl: String = "https://example.com"
    override val lang: String = "en"
    override val id: Long = <unique_id>L
    
    override val exploreFetchers = listOf(
        BaseExploreFetcher(
            key = "search",
            endpoint = "/search?q={query}&page={page}",
            selector = "div.novel-item",
            nameSelector = "h3.title",
            linkSelector = "a",
            linkAtt = "href",
            coverSelector = "img",
            coverAtt = "src",
            addBaseUrlToLink = true,
            type = Type.Search
        ),
        // ... other fetchers
    )
    
    override val detailFetcher = Detail(
        nameSelector = "h1",
        descriptionSelector = "div.description",
        authorBookSelector = "span.author",
        coverSelector = "img.cover",
        coverAtt = "src",
        categorySelector = "div.genres a",
        statusSelector = "span.status",
        addBaseurlToCoverLink = true,
        onStatus = { text ->
            when (text.lowercase()) {
                "ongoing" -> MangaInfo.ONGOING
                "completed" -> MangaInfo.COMPLETED
                else -> MangaInfo.UNKNOWN
            }
        }
    )
    
    override val chapterFetcher = Chapters(
        selector = "ul.chapter-list li",
        nameSelector = "a",
        linkSelector = "a",
        linkAtt = "href",
        addBaseUrlToLink = true
    )
    
    override val contentFetcher = Content(
        pageContentSelector = "div.chapter-content p"
    )
}
```

---

## Testing Checklist

### For Each Migrated Source:

1. **Compile Test:**
   ```bash
   ./gradlew :sources:en:<source>:compileKotlinJvm
   ./gradlew :sources:en:<source>:compileKotlinJs
   ```

2. **Unit Tests:**
   ```bash
   ./gradlew :sources:en:<source>:test
   ```

3. **JS Bundle Test:**
   ```bash
   ./gradlew :sources:en:<source>:jsBrowserProductionWebpack
   # Check build/dist/js/productionExecutable/<source>.js exists
   ```

4. **Browser Console Test:**
   ```javascript
   // Load runtime.js first, then source.js
   init<SourceName>()
   SourceBridge.search('<source-id>', 'test', 1).then(console.log)
   ```

5. **Android APK Test:**
   - Install APK on device
   - Test search, details, chapters, content

---

## Rollback Plan

If migration causes issues:

1. Keep original JVM-only sources in a `legacy/` branch
2. Use feature flags to enable/disable JS builds
3. Maintain backward compatibility with existing APK format

---

## Timeline Estimate

| Phase | Duration | Dependencies |
|-------|----------|--------------|
| Phase 1: Dependencies | 1 day | None |
| Phase 2: Common Utils | 2 days | Phase 1 |
| Phase 3: First 5 Sources | 3 days | Phase 2 |
| Phase 4: Build System | 2 days | Phase 3 |
| Phase 5: CI/CD | 1 day | Phase 4 |
| Phase 6: Remaining Sources | 5-10 days | Phase 5 |

**Total: ~2-3 weeks**

---

## Files to Create/Modify Summary

### New Files:
- `common/src/commonMain/kotlin/...` (migrated from main)
- `common/src/jsMain/kotlin/...` (JS-specific if needed)
- `sources/*/src/jsMain/kotlin/.../Init.kt` (for each source)

### Modified Files:
- `gradle/libs.versions.toml`
- `common/build.gradle.kts`
- `buildSrc/src/main/kotlin/ireader.extension.gradle.kts`
- `sources/*/build.gradle.kts` (each source)
- `sources/*/src/commonMain/kotlin/...` (migrated sources)
- `.github/workflows/build.yml`

### Deleted Files:
- None (keep for backward compatibility)

---

## Questions to Resolve

1. **Source API Version**: When will the new source-api with JS support be published to Maven?
2. **CDN Setup**: Where will JS bundles be hosted? (sources.ireader.app?)
3. **Versioning**: How to version JS bundles separately from APKs?
4. **Testing**: Do we need iOS simulator tests in CI?

---

## Next Steps

1. ✅ Review this plan
2. ⏳ Publish new source-api to Maven
3. ⏳ Start Phase 1 in IReader-extensions
4. ⏳ Migrate sources incrementally
5. ⏳ Test on iOS simulator
6. ⏳ Deploy to production
