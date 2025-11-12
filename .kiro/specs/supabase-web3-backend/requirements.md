# Requirements Document

## Introduction

This document specifies the requirements for implementing a portable, Supabase-based backend for the IReader Kotlin Multiplatform application with Web3 wallet-based authentication. The system will enable users to authenticate using their crypto wallet addresses and sync their reading data across devices while maintaining Clean Architecture principles to ensure the backend implementation remains swappable.

## Glossary

- **IReader App**: The Kotlin Multiplatform (KMM) application for reading books across Android and Desktop platforms
- **Supabase**: The backend-as-a-service platform providing Postgres database, authentication, and real-time capabilities
- **Web3 Wallet**: A cryptocurrency wallet application (e.g., Trust Wallet, SafePal) that can sign messages to prove ownership
- **Wallet Address**: A unique hexadecimal identifier (e.g., 0xF9Abb...7eCc) that serves as the user's identity
- **RemoteRepository**: The domain layer interface that abstracts backend operations
- **Clean Architecture**: A software design pattern that separates concerns into layers (domain, data, presentation) with dependencies pointing inward
- **Book ID**: A normalized, universal identifier derived from a book's title (e.g., "lord-of-the-mysteries")
- **Source ID**: A plugin-specific identifier for a book that varies across different content sources
- **Chapter Slug**: A unique identifier for a chapter within a book (e.g., "chapter-101")
- **Supabase Realtime**: A feature that enables real-time data synchronization across connected clients

## Requirements

### Requirement 1: Clean Architecture Backend Design

**User Story:** As a developer, I want the backend implementation to follow Clean Architecture principles, so that the app can be migrated to different backend providers without rewriting business logic.

#### Acceptance Criteria

1. THE IReader App SHALL define a RemoteRepository interface in the domain layer that specifies all backend operations
2. THE IReader App SHALL implement the RemoteRepository interface using Supabase in the data layer
3. THE IReader App SHALL ensure that the domain layer contains no direct dependencies on Supabase libraries
4. THE IReader App SHALL structure the data layer to allow replacement of Supabase with alternative backend providers without modifying domain logic
5. THE IReader App SHALL use dependency injection to provide the RemoteRepository implementation to use cases

### Requirement 2: Web3 Wallet Authentication

**User Story:** As a user, I want to sign in using my crypto wallet, so that I can authenticate without creating a password and use my wallet address as my identity.

#### Acceptance Criteria

1. WHEN a user initiates the sign-in flow, THE IReader App SHALL request the user's wallet to sign a unique challenge message
2. THE IReader App SHALL verify the signed message to confirm wallet ownership without requiring gas fees
3. WHEN authentication succeeds, THE IReader App SHALL use the wallet address as the permanent user identifier
4. THE IReader App SHALL store the authenticated wallet address in the local session
5. THE IReader App SHALL create a user record in the Supabase users table if the wallet address does not exist

### Requirement 3: User Database Schema

**User Story:** As a system administrator, I want a users table that stores wallet-based user profiles, so that the system can track user identity and supporter status.

#### Acceptance Criteria

1. THE Supabase database SHALL contain a users table with wallet_address as the primary key
2. THE users table SHALL include a username field that allows optional user-defined display names
3. THE users table SHALL include a created_at timestamp field that records account creation time
4. THE users table SHALL include an is_supporter boolean field that defaults to false
5. THE users table SHALL enforce uniqueness on the wallet_address field

### Requirement 4: Reading Progress Synchronization

**User Story:** As a user, I want my reading progress to sync across all my devices, so that I can continue reading from where I left off on any device.

#### Acceptance Criteria

1. THE IReader App SHALL store reading progress in a Supabase reading_progress table linked to the user's wallet address
2. THE reading_progress table SHALL record the last_chapter_slug for each book the user is reading
3. THE reading_progress table SHALL record the last_scroll_position as a floating-point value for precise position tracking
4. WHEN a user updates their reading position, THE IReader App SHALL sync the change to Supabase within 5 seconds
5. THE IReader App SHALL use Supabase Realtime to receive reading progress updates from other devices within 5 seconds

### Requirement 5: Reading Progress Database Schema

**User Story:** As a system administrator, I want a reading_progress table that tracks user reading positions, so that the system can synchronize progress across devices.

#### Acceptance Criteria

1. THE Supabase database SHALL contain a reading_progress table with an auto-incrementing id as the primary key
2. THE reading_progress table SHALL include a user_wallet_address field that references users.wallet_address as a foreign key
3. THE reading_progress table SHALL include a book_id field that stores the normalized book identifier
4. THE reading_progress table SHALL include an updated_at timestamp field that records the last modification time
5. THE reading_progress table SHALL enforce a unique constraint on the combination of user_wallet_address and book_id

### Requirement 6: Universal Book Identification

**User Story:** As a developer, I want books to be identified by normalized titles rather than source-specific IDs, so that reviews and comments are shared across all sources for the same book.

#### Acceptance Criteria

1. WHEN a user opens a book, THE IReader App SHALL generate a book_id by normalizing the book title to lowercase and replacing spaces with hyphens
2. THE IReader App SHALL use the normalized book_id for all backend operations related to reviews, comments, and reading progress
3. THE IReader App SHALL ensure that books with identical titles from different sources share the same book_id
4. THE IReader App SHALL remove special characters from book titles during normalization to ensure consistency
5. THE IReader App SHALL store the mapping between source_id and book_id locally for efficient lookups

### Requirement 7: Database Security and Access Control

**User Story:** As a security administrator, I want database access to be restricted based on user authentication, so that users can only access and modify their own data.

#### Acceptance Criteria

1. THE Supabase database SHALL enforce Row Level Security (RLS) policies on all user-related tables
2. THE reading_progress table SHALL allow users to read and write only their own progress records
3. THE users table SHALL allow users to read all records but write only their own record
4. THE Supabase database SHALL reject unauthenticated requests to protected tables
5. THE Supabase database SHALL log all failed authentication attempts for security monitoring

### Requirement 8: Offline Support and Conflict Resolution

**User Story:** As a user, I want to continue reading offline and have my progress sync when I reconnect, so that I'm not blocked by network issues.

#### Acceptance Criteria

1. WHEN the network is unavailable, THE IReader App SHALL store reading progress updates in a local queue
2. WHEN network connectivity is restored, THE IReader App SHALL sync all queued progress updates to Supabase
3. IF a conflict occurs between local and remote progress, THE IReader App SHALL use the most recent updated_at timestamp to resolve the conflict
4. THE IReader App SHALL notify the user if a sync conflict is resolved automatically
5. THE IReader App SHALL retry failed sync operations up to 3 times with exponential backoff

### Requirement 9: Backend Configuration Management

**User Story:** As a developer, I want Supabase connection details to be configurable, so that the app can connect to different environments (development, staging, production).

#### Acceptance Criteria

1. THE IReader App SHALL read Supabase URL and API key from a configuration file or environment variables
2. THE IReader App SHALL validate Supabase connection parameters at startup
3. THE IReader App SHALL provide clear error messages if Supabase connection fails
4. THE IReader App SHALL support switching between multiple Supabase projects without code changes
5. THE IReader App SHALL never hardcode Supabase credentials in source code

### Requirement 10: Performance and Scalability

**User Story:** As a user, I want backend operations to be fast and responsive, so that my reading experience is not interrupted by slow data loading.

#### Acceptance Criteria

1. THE IReader App SHALL complete reading progress sync operations within 2 seconds under normal network conditions
2. THE Supabase database SHALL use indexes on frequently queried fields (user_wallet_address, book_id)
3. THE IReader App SHALL implement connection pooling to minimize database connection overhead
4. THE IReader App SHALL cache frequently accessed data locally to reduce backend requests
5. THE IReader App SHALL implement pagination for queries that return large result sets
