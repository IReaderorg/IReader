# AndroidTest Pass/Fail Checklist

> **Legend**: ✅ = all pass | ❌ = some/all fail | ⏳ = not yet run | 🔧 = needs fix | 🛠️ = fix in progress
> Run one test class at a time. Fix failures immediately before moving on.

## Core Screens (extends BaseComposeTest)

| # | Test Class | Status | Notes |
|---|-----------|--------|-------|
| 1 | `core/BookDetailScreenTest` | ⏳ | |
| 2 | `core/LibraryScreenTest` | ⏳ | |
| 3 | `core/HistoryScreenTest` | ⏳ | |
| 4 | `core/DownloadsScreenTest` | ⏳ | |
| 5 | `core/ExploreScreenTest` | ⏳ | |
| 6 | `core/UpdatesScreenTest` | ⏳ | |
| 7 | `core/SourcesScreenTest` | ⏳ | |
| 8 | `core/AppNavigationTest` | ⏳ | |
| 9 | `core/ReaderScreenTest` | ⏳ | |

## Features

| # | Test Class | Status | Notes |
|---|-----------|--------|-------|
| 10 | `features/MigrationTest` | ⏳ | |
| 11 | `features/DeepLinkTest` | ⏳ | |
| 12 | `features/SourceInstallationTest` | ⏳ | |
| 13 | `features/BackupRestoreTest` | ⏳ | |
| 14 | `features/ReaderFullFeatureTest` | ⏳ | |
| 15 | `features/TTSFeatureTest` | ⏳ | |
| 16 | `features/CategoriesTest` | ⏳ | |

## Settings

| # | Test Class | Status | Notes |
|---|-----------|--------|-------|
| 17 | `settings/SettingsNavigationTest` | ⏳ | |
| 18 | `settings/GeneralSettingsTest` | ⏳ | |
| 19 | `settings/AppearanceSettingsTest` | ⏳ | |
| 20 | `settings/ReaderSettingsTest` | ⏳ | |
| 21 | `settings/DownloadSettingsTest` | ⏳ | |
| 22 | `settings/LibrarySettingsTest` | ⏳ | |
| 23 | `settings/SecuritySettingsTest` | ⏳ | |
| 24 | `settings/NotificationSettingsTest` | ⏳ | |
| 25 | `settings/DataSettingsTest` | ⏳ | |
| 26 | `settings/TrackingSettingsTest` | ⏳ | |
| 27 | `settings/AdvancedSettingsTest` | ⏳ | |
| 28 | `settings/SettingsPersistenceTest` | ⏳ | |
| 29 | `settings/BackupSettingsTest` | ⏳ | |

## Kotlin Tests

| # | Test Class | Status | Notes |
|---|-----------|--------|-------|
| 30 | `SmokeTest` | ✅ | Known passed |
| 31 | `BookDetailNavigationTest` | ✅ | Known passed |
| 32 | `BookDetailViewModelTest` | ⏳ | |
| 33 | `BookDetailIntegrationTest` | ⏳ | |
| 34 | `DownloadIntegrationTest` | ⏳ | |
| 35 | `DownloadServiceTest` | ⏳ | |
| 36 | `ExploreNavigationFlowTest` | ⏳ | |
| 37 | `ExploreScreenIntegrationTest` | ⏳ | |
| 38 | `ExtensionInstallerIntegrationTest` | ⏳ | |

## Summary

| Category | Total | ✅ Pass | ❌ Fail | ⏳ Pending |
|----------|-------|--------|--------|-----------|
| Core | 9 | 0 | 0 | 9 |
| Features | 7 | 0 | 0 | 7 |
| Settings | 13 | 0 | 0 | 13 |
| Kotlin | 9 | 2 | 0 | 7 |
| **Total** | **38** | **2** | **0** | **36** |
