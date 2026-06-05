# WebReader - Rust + JVM + Frontend Implementation Plan

**Date:** 2026-06-04
**Architecture:** Rust Backend + Embedded JVM for Sources + React Frontend
**Target:** Cheap server, multi-user, low RAM usage

---

## 1. Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                    WebReader Rust + JVM Architecture                 │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                    Web Browser (Client)                       │   │
│  │  ┌────────────────────────────────────────────────────────┐  │   │
│  │  │           React 18 + Next.js 14 (TypeScript)           │  │   │
│  │  │  - Beautiful library with Framer Motion animations    │  │   │
│  │  │  - Canvas-based reader with smooth scrolling          │  │   │
│  │  │  - PWA with offline support                           │  │   │
│  │  │  - Tailwind CSS + Radix UI components                 │  │   │
│  │  └────────────────────────────────────────────────────────┘  │   │
│  └──────────────────────────┬───────────────────────────────────┘   │
│                             │ HTTP / WebSocket                       │
│  ┌──────────────────────────▼───────────────────────────────────┐   │
│  │                  Rust Backend (Axum)                          │   │
│  │  ┌────────────────────────────────────────────────────────┐  │   │
│  │  │              REST API Layer (Axum Router)               │  │   │
│  │  │  - /api/v1/sources/*                                   │  │   │
│  │  │  - /api/v1/books/*                                     │  │   │
│  │  │  - /api/v1/chapters/*                                  │  │   │
│  │  │  - /api/v1/users/*                                     │  │   │
│  │  │  - /ws (WebSocket for real-time updates)               │  │   │
│  │  └────────────────────────────────────────────────────────┘  │   │
│  │  ┌────────────────────────────────────────────────────────┐  │   │
│  │  │              Service Layer (Tokio)                      │  │   │
│  │  │  - Authentication (JWT)                                │  │   │
│  │  │  - Rate limiting                                       │  │   │
│  │  │  - User management                                     │  │   │
│  │  │  - Cache management                                    │  │   │
│  │  └────────────────────────────────────────────────────────┘  │   │
│  │  ┌────────────────────────────────────────────────────────┐  │   │
│  │  │              Database Layer (SQLx)                      │  │   │
│  │  │  - SQLite (default) or PostgreSQL                      │  │   │
│  │  │  - Connection pooling                                  │  │   │
│  │  │  - Migrations                                          │  │   │
│  │  └────────────────────────────────────────────────────────┘  │   │
│  └──────────────────────────┬───────────────────────────────────┘   │
│                             │ JNI / FFI                              │
│  ┌──────────────────────────▼───────────────────────────────────┐   │
│  │                  Embedded JVM (Sources)                       │   │
│  │  ┌────────────────────────────────────────────────────────┐  │   │
│  │  │              JNI Bridge (rust-jni)                      │  │   │
│  │  │  - Create JVM instance                                  │  │   │
│  │  │  - Load source JARs                                     │  │   │
│  │  │  - Call source methods                                  │  │   │
│  │  │  - Handle exceptions                                    │  │   │
│  │  └────────────────────────────────────────────────────────┘  │   │
│  │  ┌────────────────────────────────────────────────────────┐  │   │
│  │  │              Source Runtime (JVM)                       │  │   │
│  │  │  ┌──────────────────────────────────────────────────┐  │  │   │
│  │  │  │  IReader source-api                              │  │  │   │
│  │  │  │  - HttpSource, ParsedHttpSource                  │  │  │   │
│  │  │  │  - CatalogSource interface                       │  │  │   │
│  │  │  └──────────────────────────────────────────────────┘  │  │   │
│  │  │  ┌──────────────────────────────────────────────────┐  │  │   │
│  │  │  │  Loaded Sources (Isolated ClassLoaders)          │  │  │   │
│  │  │  │  - Source A (ClassLoader A)                      │  │  │   │
│  │  │  │  - Source B (ClassLoader B)                      │  │  │   │
│  │  │  │  - Source C (ClassLoader C)                      │  │  │   │
│  │  │  └──────────────────────────────────────────────────┘  │  │   │
│  │  └────────────────────────────────────────────────────────┘  │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 2. Technology Stack

### Backend: Rust

| Component | Technology | Version | Purpose |
|-----------|-----------|---------|---------|
| **Web Framework** | Axum | 0.7 | HTTP server, routing |
| **Async Runtime** | Tokio | 1.35 | Async I/O, concurrency |
| **Database** | SQLx | 0.7 | Type-safe SQL, connection pooling |
| **Serialization** | serde + serde_json | 1.0 | JSON serialization |
| **Authentication** | jsonwebtoken | 9.0 | JWT tokens |
| **HTTP Client** | reqwest | 0.12 | Fetch from sources |
| **JNI Bridge** | jni | 0.21 | Communicate with JVM |
| **Logging** | tracing + tracing-subscriber | 0.1 | Structured logging |

### Embedded JVM

| Component | Technology | Purpose |
|-----------|-----------|---------|
| **JVM Implementation** | OpenJDK 17 (via JNI) | Run source bytecode |
| **Source API** | IReader source-api | Source interfaces |
| **ClassLoader** | URLClassLoader (per source) | Isolate sources |

### Frontend: React + Next.js

| Component | Technology | Purpose |
|-----------|-----------|---------|
| **Framework** | React 18 + Next.js 14 | UI framework |
| **Language** | TypeScript 5 | Type safety |
| **Styling** | Tailwind CSS 3 | Utility-first CSS |
| **Components** | Radix UI | Accessible primitives |
| **Animations** | Framer Motion | Smooth animations |
| **State** | Zustand | Lightweight state management |
| **Data Fetching** | TanStack Query | Server state management |
| **Forms** | React Hook Form + Zod | Form validation |

---

## 3. Project Structure

```
webreader-rust/
├── Cargo.toml                          # Rust dependencies
├── build.rs                            # Build script for JNI
│
├── src/                                # Rust source code
│   ├── main.rs                         # Entry point
│   ├── lib.rs                          # Library exports
│   │
│   ├── api/                            # REST API handlers
│   │   ├── mod.rs
│   │   ├── sources.rs                  # Source endpoints
│   │   ├── books.rs                    # Book endpoints
│   │   ├── chapters.rs                 # Chapter endpoints
│   │   ├── users.rs                    # User endpoints
│   │   └── websocket.rs                # WebSocket handler
│   │
│   ├── services/                       # Business logic
│   │   ├── mod.rs
│   │   ├── auth.rs                     # Authentication service
│   │   ├── cache.rs                    # Cache management
│   │   ├── rate_limiter.rs             # Rate limiting
│   │   └── user_manager.rs             # User management
│   │
│   ├── jvm/                            # JVM integration
│   │   ├── mod.rs
│   │   ├── bridge.rs                   # JNI bridge
│   │   ├── source_manager.rs           # Source lifecycle
│   │   └── source_proxy.rs             # Source method calls
│   │
│   ├── db/                             # Database
│   │   ├── mod.rs
│   │   ├── models.rs                   # Database models
│   │   ├── queries.rs                  # SQL queries
│   │   └── migrations.rs               # Migrations
│   │
│   ├── models/                         # Domain models
│   │   ├── mod.rs
│   │   ├── source.rs                   # Source model
│   │   ├── book.rs                     # Book model
│   │   ├── chapter.rs                  # Chapter model
│   │   └── user.rs                     # User model
│   │
│   └── config/                         # Configuration
│       ├── mod.rs
│       └── settings.rs
│
├── jvm/                                # JVM source code
│   ├── SourceBridge.java               # JNI bridge class
│   ├── SourceLoader.java               # Source loading logic
│   └── SourceCache.java                # Response caching
│
├── frontend/                           # React frontend
│   ├── package.json
│   ├── next.config.js
│   ├── tailwind.config.js
│   │
│   ├── src/
│   │   ├── app/                        # Next.js app router
│   │   │   ├── layout.tsx
│   │   │   ├── page.tsx
│   │   │   ├── sources/
│   │   │   ├── library/
│   │   │   ├── reader/
│   │   │   └── settings/
│   │   │
│   │   ├── components/                 # Reusable components
│   │   │   ├── ui/                     # Base UI components
│   │   │   ├── library/                # Library components
│   │   │   ├── reader/                 # Reader components
│   │   │   └── sources/                # Source components
│   │   │
│   │   ├── hooks/                      # Custom hooks
│   │   │   ├── useSources.ts
│   │   │   ├── useBooks.ts
│   │   │   └── useReader.ts
│   │   │
│   │   ├── lib/                        # Utilities
│   │   │   ├── api.ts                  # API client
│   │   │   ├── auth.ts                 # Auth utilities
│   │   │   └── utils.ts
│   │   │
│   │   ├── stores/                     # Zustand stores
│   │   │   ├── authStore.ts
│   │   │   ├── libraryStore.ts
│   │   │   └── readerStore.ts
│   │   │
│   │   └── types/                      # TypeScript types
│   │       ├── source.ts
│   │       ├── book.ts
│   │       └── chapter.ts
│   │
│   └── public/                         # Static assets
│
├── sources/                            # Source JARs directory
│   ├── ireader/
│   └── suwayomi/
│
├── migrations/                         # Database migrations
│   ├── 001_initial.sql
│   └── 002_add_users.sql
│
├── Dockerfile
├── docker-compose.yml
└── README.md
```

---

## 4. Implementation Details

### 4.1 Rust Backend (Axum)

```rust
// src/main.rs
use axum::{routing::get, Router, Server};
use std::net::SocketAddr;
use std::sync::Arc;

mod api;
mod config;
mod db;
mod jvm;
mod models;
mod services;

use crate::jvm::SourceManager;
use crate::services::{AuthService, CacheService, RateLimiter};

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    // Initialize tracing
    tracing_subscriber::init();
    
    // Load configuration
    let config = config::load()?;
    
    // Initialize database
    let db = db::init(&config.database_url).await?;
    
    // Initialize JVM for sources
    let jvm = jvm::init(&config.jvm_path, &config.sources_dir)?;
    let source_manager = SourceManager::new(jvm, &config.sources_dir).await?;
    
    // Initialize services
    let cache = CacheService::new(config.cache_size);
    let rate_limiter = RateLimiter::new(config.rate_limit);
    let auth_service = AuthService::new(db.clone(), &config.jwt_secret);
    
    // Build application state
    let state = Arc::new(AppState {
        db,
        source_manager,
        cache,
        rate_limiter,
        auth_service,
    });
    
    // Build router
    let app = Router::new()
        .route("/api/v1/health", get(api::health))
        .nest("/api/v1/sources", api::sources::routes())
        .nest("/api/v1/books", api::books::routes())
        .nest("/api/v1/chapters", api::chapters::routes())
        .nest("/api/v1/users", api::users::routes())
        .route("/ws", get(api::websocket::handler))
        .with_state(state);
    
    // Start server
    let addr: SocketAddr = format!("{}:{}", config.host, config.port).parse()?;
    tracing::info!("WebReader server starting on {}", addr);
    
    Server::bind(&addr)
        .serve(app.into_make_service())
        .await?;
    
    Ok(())
}

pub struct AppState {
    db: db::Database,
    source_manager: SourceManager,
    cache: CacheService,
    rate_limiter: RateLimiter,
    auth_service: AuthService,
}
```

### 4.2 JNI Bridge for JVM

```rust
// src/jvm/bridge.rs
use jni::objects::{JClass, JObject, JString, JValue};
use jni::{InitArgsBuilder, JJavaVM, JNIVersion, JavaVM};
use std::path::Path;

pub struct JvmBridge {
    jvm: JavaVM,
    env: jni::AttachGuard<'static>,
}

impl JvmBridge {
    pub fn new(jvm_path: &Path, classpath: &Path) -> Result<Self, jni::errors::Error> {
        let jvm_args = InitArgsBuilder::new()
            .version(JNIVersion::V8)
            .option(&format!(
                "-Djava.class.path={}",
                classpath.display()
            ))
            .option("-Xms64m")
            .option("-Xmx256m")
            .option("-XX:+UseSerialGC")
            .build()?;
        
        let jvm = JavaVM::new(jvm_args)?;
        let env = jvm.attach_current_thread()?;
        
        Ok(Self { jvm, env })
    }
    
    pub fn load_source(
        &mut self,
        source_id: &str,
        jar_path: &Path,
    ) -> Result<(), SourceError> {
        let source_id = self.env.new_string(source_id)?;
        let jar_path = self.env.new_string(jar_path.to_str().unwrap())?;
        
        self.env.call_static_method(
            "SourceBridge",
            "loadSource",
            "(Ljava/lang/String;Ljava/lang/String;)V",
            &[
                JValue::Object(&source_id.into()),
                JValue::Object(&jar_path.into()),
            ],
        )?;
        
        Ok(())
    }
    
    pub fn search(
        &mut self,
        source_id: &str,
        query: &str,
        page: i32,
    ) -> Result<String, SourceError> {
        let source_id = self.env.new_string(source_id)?;
        let query = self.env.new_string(query)?;
        
        let result = self.env.call_static_method(
            "SourceBridge",
            "search",
            "(Ljava/lang/String;Ljava/lang/String;I)Ljava/lang/String;",
            &[
                JValue::Object(&source_id.into()),
                JValue::Object(&query.into()),
                JValue::Int(page),
            ],
        )?;
        
        let result_str: JString = result.l()?.into();
        let result_rust: String = self.env.get_string(&result_str)?.into();
        
        Ok(result_rust)
    }
    
    pub fn get_popular(
        &mut self,
        source_id: &str,
        page: i32,
    ) -> Result<String, SourceError> {
        let source_id = self.env.new_string(source_id)?;
        
        let result = self.env.call_static_method(
            "SourceBridge",
            "getPopular",
            "(Ljava/lang/String;I)Ljava/lang/String;",
            &[
                JValue::Object(&source_id.into()),
                JValue::Int(page),
            ],
        )?;
        
        let result_str: JString = result.l()?.into();
        let result_rust: String = self.env.get_string(&result_str)?.into();
        
        Ok(result_rust)
    }
}
```

### 4.3 Java Source Bridge

```java
// jvm/SourceBridge.java
import ireader.core.source.CatalogSource;
import ireader.core.source.Dependencies;
import ireader.core.source.model.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SourceBridge {
    private static final Map<String, CatalogSource> sources = new ConcurrentHashMap<>();
    private static Dependencies dependencies;
    
    public static void init(Dependencies deps) {
        dependencies = deps;
    }
    
    public static void loadSource(String sourceId, String jarPath) throws Exception {
        URL[] urls = { new URL("file:" + jarPath) };
        URLClassLoader classLoader = new URLClassLoader(urls, SourceBridge.class.getClassLoader());
        
        // Find source class in JAR
        // This is simplified - actual implementation would read manifest
        Class<?> sourceClass = classLoader.loadClass(sourceId + ".MainSource");
        CatalogSource source = (CatalogSource) sourceClass
            .getConstructor(Dependencies.class)
            .newInstance(dependencies);
        
        sources.put(sourceId, source);
    }
    
    public static String search(String sourceId, String query, int page) {
        CatalogSource source = sources.get(sourceId);
        if (source == null) {
            return "{\"error\": \"Source not found\"}";
        }
        
        try {
            MangasPage result = source.searchMangas(MangasPageRequest(query, page));
            return Json.encodeToString(result);
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }
    
    public static String getPopular(String sourceId, int page) {
        CatalogSource source = sources.get(sourceId);
        if (source == null) {
            return "{\"error\": \"Source not found\"}";
        }
        
        try {
            MangasPage result = source.getPopularManga(page);
            return Json.encodeToString(result);
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }
    
    public static String getBookDetails(String sourceId, String bookJson) {
        // Implementation...
        return "{}";
    }
    
    public static String getChapters(String sourceId, String bookJson) {
        // Implementation...
        return "[]";
    }
    
    public static String getChapterContent(String sourceId, String chapterJson) {
        // Implementation...
        return "[]";
    }
}
```

### 4.4 Source Manager

```rust
// src/jvm/source_manager.rs
use crate::jvm::bridge::JvmBridge;
use crate::models::Source;
use std::path::Path;
use std::sync::Arc;
use tokio::sync::Mutex;

pub struct SourceManager {
    jvm: Arc<Mutex<JvmBridge>>,
    sources_dir: PathBuf,
}

impl SourceManager {
    pub async fn new(jvm: JvmBridge, sources_dir: &Path) -> Result<Self, SourceError> {
        let manager = Self {
            jvm: Arc::new(Mutex::new(jvm)),
            sources_dir: sources_dir.to_path_buf(),
        };
        
        manager.load_all_sources().await?;
        
        Ok(manager)
    }
    
    pub async fn load_all_sources(&self) -> Result<(), SourceError> {
        let source_files: Vec<_> = std::fs::read_dir(&self.sources_dir)?
            .filter_map(|entry| entry.ok())
            .filter(|entry| {
                entry.path().extension()
                    .map(|ext| ext == "jar")
                    .unwrap_or(false)
            })
            .collect();
        
        for file in source_files {
            let source_id = file.path()
                .file_stem()
                .unwrap()
                .to_str()
                .unwrap()
                .to_string();
            
            let mut jvm = self.jvm.lock().await;
            if let Err(e) = jvm.load_source(&source_id, &file.path()) {
                tracing::error!("Failed to load source {}: {}", source_id, e);
            } else {
                tracing::info!("Loaded source: {}", source_id);
            }
        }
        
        Ok(())
    }
    
    pub async fn search(
        &self,
        source_id: &str,
        query: &str,
        page: i32,
    ) -> Result<String, SourceError> {
        let mut jvm = self.jvm.lock().await;
        jvm.search(source_id, query, page)
    }
    
    pub async fn get_popular(
        &self,
        source_id: &str,
        page: i32,
    ) -> Result<String, SourceError> {
        let mut jvm = self.jvm.lock().await;
        jvm.get_popular(source_id, page)
    }
}
```

### 4.5 API Handlers

```rust
// src/api/sources.rs
use axum::{
    extract::{Path, Query, State},
    routing::get,
    Json, Router,
};
use serde::Deserialize;
use std::sync::Arc;

use crate::api::AppState;

pub fn routes() -> Router<Arc<AppState>> {
    Router::new()
        .route("/", get(list_sources))
        .route("/:id", get(get_source))
        .route("/:id/search", get(search_source))
        .route("/:id/popular", get(get_popular))
}

#[derive(Deserialize)]
pub struct SearchQuery {
    q: String,
    page: Option<i32>,
}

#[derive(Deserialize)]
pub struct PageQuery {
    page: Option<i32>,
}

async fn list_sources(
    State(state): State<Arc<AppState>>,
) -> Result<Json<Vec<Source>>, ApiError> {
    let sources = state.source_manager.list_sources().await?;
    Ok(Json(sources))
}

async fn get_source(
    State(state): State<Arc<AppState>>,
    Path(id): Path<String>,
) -> Result<Json<Source>, ApiError> {
    let source = state.source_manager.get_source(&id).await?;
    Ok(Json(source))
}

async fn search_source(
    State(state): State<Arc<AppState>>,
    Path(id): Path<String>,
    Query(query): Query<SearchQuery>,
) -> Result<Json<serde_json::Value>, ApiError> {
    // Check cache first
    let cache_key = format!("search:{}:{}:{}", id, query.q, query.page.unwrap_or(1));
    if let Some(cached) = state.cache.get(&cache_key).await {
        return Ok(Json(serde_json::from_str(&cached)?));
    }
    
    // Rate limit check
    let user_id = get_current_user_id(&state).await?;
    if !state.rate_limiter.acquire(user_id).await {
        return Err(ApiError::TooManyRequests);
    }
    
    // Call source via JVM
    let result = state.source_manager
        .search(&id, &query.q, query.page.unwrap_or(1))
        .await?;
    
    // Cache result
    state.cache.set(&cache_key, &result, 300).await; // 5 minutes
    
    Ok(Json(serde_json::from_str(&result)?))
}

async fn get_popular(
    State(state): State<Arc<AppState>>,
    Path(id): Path<String>,
    Query(query): Query<PageQuery>,
) -> Result<Json<serde_json::Value>, ApiError> {
    let cache_key = format!("popular:{}:{}", id, query.page.unwrap_or(1));
    
    if let Some(cached) = state.cache.get(&cache_key).await {
        return Ok(Json(serde_json::from_str(&cached)?));
    }
    
    let result = state.source_manager
        .get_popular(&id, query.page.unwrap_or(1))
        .await?;
    
    state.cache.set(&cache_key, &result, 300).await;
    
    Ok(Json(serde_json::from_str(&result)?))
}
```

### 4.6 Database Layer

```rust
// src/db/models.rs
use serde::{Deserialize, Serialize};
use sqlx::FromRow;

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
pub struct User {
    pub id: i64,
    pub username: String,
    pub password_hash: String,
    pub created_at: chrono::NaiveDateTime,
}

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
pub struct Book {
    pub id: i64,
    pub user_id: i64,
    pub source_id: String,
    pub title: String,
    pub author: Option<String>,
    pub description: Option<String>,
    pub cover_url: Option<String>,
    pub url: String,
    pub status: i32,
    pub created_at: chrono::NaiveDateTime,
}

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
pub struct Chapter {
    pub id: i64,
    pub book_id: i64,
    pub title: String,
    pub url: String,
    pub chapter_number: f32,
    pub read: bool,
    pub bookmarked: bool,
    pub created_at: chrono::NaiveDateTime,
}

// src/db/queries.rs
use super::models::*;
use sqlx::{Pool, Sqlite};

pub struct BookRepository {
    pool: Pool<Sqlite>,
}

impl BookRepository {
    pub fn new(pool: Pool<Sqlite>) -> Self {
        Self { pool }
    }
    
    pub async fn get_user_books(&self, user_id: i64) -> Result<Vec<Book>, sqlx::Error> {
        sqlx::query_as::<_, Book>(
            "SELECT * FROM books WHERE user_id = ? ORDER BY created_at DESC"
        )
        .bind(user_id)
        .fetch_all(&self.pool)
        .await
    }
    
    pub async fn add_book(&self, book: &Book) -> Result<i64, sqlx::Error> {
        let result = sqlx::query(
            "INSERT INTO books (user_id, source_id, title, author, description, cover_url, url, status)
             VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
        )
        .bind(book.user_id)
        .bind(&book.source_id)
        .bind(&book.title)
        .bind(&book.author)
        .bind(&book.description)
        .bind(&book.cover_url)
        .bind(&book.url)
        .bind(book.status)
        .execute(&self.pool)
        .await?;
        
        Ok(result.last_insert_rowid())
    }
}
```

---

## 5. Memory Usage Analysis

### Rust + JVM Memory Breakdown

```
┌─────────────────────────────────────────────────────────────────────┐
│                 Rust + JVM Memory Layout                             │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                    Rust Backend                               │   │
│  │  - Axum runtime: ~5-10 MB                                    │   │
│  │  - Application code: ~10-20 MB                               │   │
│  │  - Database connections: ~5-10 MB                            │   │
│  │  - HTTP client: ~5-10 MB                                     │   │
│  │  - Cache: ~20-50 MB (configurable)                           │   │
│  │  - User sessions: ~1-5 MB                                    │   │
│  │  Total Rust: ~50-100 MB                                      │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                    Embedded JVM                               │   │
│  │  - JVM overhead: ~30-50 MB                                   │   │
│  │  - IReader source-api: ~10-20 MB                             │   │
│  │  - Per source: ~2-5 MB (50 sources = ~150 MB)                │   │
│  │  - Source cache: ~20-50 MB                                   │   │
│  │  Total JVM: ~200-300 MB                                      │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                    Total Memory                               │   │
│  │  - Minimal (10 sources, 5 users): ~250-400 MB                │   │
│  │  - Small (50 sources, 20 users): ~400-600 MB                 │   │
│  │  - Medium (100 sources, 50 users): ~600-900 MB               │   │
│  │  - Large (200 sources, 100 users): ~1-1.5 GB                 │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### Comparison: Rust+JVM vs Pure Ktor

| Configuration | Rust + JVM | Pure Ktor | Winner |
|---------------|------------|-----------|--------|
| **10 sources, 5 users** | 250-400 MB | 400-600 MB | Rust |
| **50 sources, 20 users** | 400-600 MB | 768 MB - 1 GB | Rust |
| **100 sources, 50 users** | 600-900 MB | 1-2 GB | Rust |
| **200 sources, 100 users** | 1-1.5 GB | 2-4 GB | Rust |

**Rust + JVM uses ~40-50% less memory than pure Ktor** because:
- Rust has no GC overhead
- Rust binary is more memory-efficient
- JVM is only used for sources, not entire application

---

## 6. Frontend Implementation

### 6.1 React + Next.js Setup

```typescript
// frontend/src/lib/api.ts
const API_BASE = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1';

interface ApiResponse<T> {
  data: T;
  error?: string;
}

export async function fetchSources(): Promise<Source[]> {
  const res = await fetch(`${API_BASE}/sources`);
  const data = await res.json();
  return data;
}

export async function searchSource(
  sourceId: string,
  query: string,
  page: number = 1
): Promise<SearchResult> {
  const res = await fetch(
    `${API_BASE}/sources/${sourceId}/search?q=${encodeURIComponent(query)}&page=${page}`
  );
  const data = await res.json();
  return data;
}

export async function getPopular(
  sourceId: string,
  page: number = 1
): Promise<SearchResult> {
  const res = await fetch(
    `${API_BASE}/sources/${sourceId}/popular?page=${page}`
  );
  const data = await res.json();
  return data;
}
```

### 6.2 Source List Component

```tsx
// frontend/src/components/sources/SourceList.tsx
'use client';

import { useQuery } from '@tanstack/react-query';
import { fetchSources } from '@/lib/api';
import { SourceCard } from './SourceCard';

export function SourceList() {
  const { data: sources, isLoading, error } = useQuery({
    queryKey: ['sources'],
    queryFn: fetchSources,
  });

  if (isLoading) {
    return <div className="animate-pulse">Loading sources...</div>;
  }

  if (error) {
    return <div className="text-red-500">Error loading sources</div>;
  }

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      {sources?.map((source) => (
        <SourceCard key={source.id} source={source} />
      ))}
    </div>
  );
}
```

### 6.3 Reader Component

```tsx
// frontend/src/components/reader/Reader.tsx
'use client';

import { useEffect, useRef, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { motion, AnimatePresence } from 'framer-motion';

interface ReaderProps {
  chapterId: string;
}

export function Reader({ chapterId }: ReaderProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [pages, setPages] = useState<string[]>([]);

  const { data: chapter } = useQuery({
    queryKey: ['chapter', chapterId],
    queryFn: () => fetchChapterContent(chapterId),
  });

  useEffect(() => {
    if (chapter?.content) {
      setPages(chapter.content);
    }
  }, [chapter]);

  const nextPage = () => {
    if (currentPage < pages.length - 1) {
      setCurrentPage(currentPage + 1);
    }
  };

  const prevPage = () => {
    if (currentPage > 0) {
      setCurrentPage(currentPage - 1);
    }
  };

  return (
    <div className="relative w-full h-screen bg-black">
      <canvas
        ref={canvasRef}
        className="w-full h-full"
        onClick={(e) => {
          const rect = e.currentTarget.getBoundingClientRect();
          const x = e.clientX - rect.left;
          if (x > rect.width / 2) {
            nextPage();
          } else {
            prevPage();
          }
        }}
      />
      
      <AnimatePresence>
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          exit={{ opacity: 0, y: 20 }}
          className="absolute bottom-4 left-4 right-4 flex justify-between text-white"
        >
          <button
            onClick={prevPage}
            disabled={currentPage === 0}
            className="px-4 py-2 bg-gray-800 rounded disabled:opacity-50"
          >
            Previous
          </button>
          <span>
            {currentPage + 1} / {pages.length}
          </span>
          <button
            onClick={nextPage}
            disabled={currentPage === pages.length - 1}
            className="px-4 py-2 bg-gray-800 rounded disabled:opacity-50"
          >
            Next
          </button>
        </motion.div>
      </AnimatePresence>
    </div>
  );
}
```

---

## 7. Deployment

### Dockerfile

```dockerfile
# Build stage for Rust
FROM rust:1.75-slim as rust-builder
WORKDIR /app
COPY Cargo.toml Cargo.lock ./
COPY src ./src
RUN cargo build --release

# Build stage for frontend
FROM node:20-alpine as frontend-builder
WORKDIR /app/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend ./
RUN npm run build

# Runtime stage
FROM debian:bookworm-slim
RUN apt-get update && apt-get install -y \
    openjdk-17-jre-headless \
    libsqlite3-0 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy Rust binary
COPY --from=rust-builder /app/target/release/webreader-server /app/

# Copy frontend
COPY --from=frontend-builder /app/frontend/out /app/static

# Copy JVM bridge
COPY jvm/ /app/jvm/

# Copy sources directory
RUN mkdir -p /app/sources

ENV JVM_PATH=/usr/lib/jvm/java-17-openjdk-amd64/lib/server/libjvm.so
ENV SOURCES_DIR=/app/sources
ENV STATIC_DIR=/app/static
ENV DATABASE_URL=sqlite:///app/data/webreader.db

EXPOSE 8080

CMD ["./webreader-server"]
```

### Docker Compose

```yaml
# docker-compose.yml
version: '3.8'

services:
  webreader:
    build: .
    ports:
      - "8080:8080"
    environment:
      - RUST_LOG=info
      - JVM_MAX_MEMORY=256m
      - CACHE_SIZE=50
      - RATE_LIMIT=30
    volumes:
      - ./data:/app/data
      - ./sources:/app/sources:ro
    deploy:
      resources:
        limits:
          memory: 512M
          cpus: '1.0'
    restart: unless-stopped
```

---

## 8. Implementation Phases

### Phase 1: Foundation
- [ ] Rust backend with Axum
- [ ] Basic REST API
- [ ] SQLite database
- [ ] JWT authentication
- [ ] React frontend setup

### Phase 2: JVM Integration
- [ ] JNI bridge implementation
- [ ] Source loading from JARs
- [ ] Source method calls
- [ ] Error handling

### Phase 3: Source Support
- [ ] IReader Kotlin DSL sources
- [ ] IReader JS sources (via GraalVM)
- [ ] Suwayomi source bridge

### Phase 4: Reader
- [ ] Canvas-based reader
- [ ] Touch/click navigation
- [ ] Reading progress tracking
- [ ] Bookmarks

### Phase 5: Multi-User
- [ ] User registration/login
- [ ] Per-user libraries
- [ ] Rate limiting
- [ ] Session management

### Phase 6: Performance
- [ ] Response caching
- [ ] Connection pooling
- [ ] Lazy source loading
- [ ] Image optimization

---

## 9. Advantages of Rust + JVM Approach

1. **Low Memory Usage**
   - Rust: ~50-100 MB (no GC)
   - JVM: ~200-300 MB (only for sources)
   - Total: ~250-400 MB (vs 768 MB - 1 GB for pure Ktor)

2. **High Performance**
   - Rust's zero-cost abstractions
   - No GC pauses for main application
   - Efficient async I/O with Tokio

3. **Source Compatibility**
   - Full IReader source support via JVM
   - Suwayomi extension support
   - No source code changes needed

4. **Safety**
   - Rust's memory safety guarantees
   - JVM sandboxing for sources
   - Isolated ClassLoaders

5. **Deployment**
   - Single binary (Rust) + JRE
   - Small Docker image (~100-200 MB)
   - Easy to distribute

---

*This plan provides a complete implementation guide for the Rust + JVM + React architecture.*