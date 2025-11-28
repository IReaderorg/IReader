# Architecture Guide

IReader follows the principles of **Clean Architecture** to ensure separation of concerns, testability, and maintainability. The project is divided into four main modules:

## 1. Presentation (`presentation`)
This module contains the UI logic and screens of the application. It depends on the `Domain` module.
- **Technologies**: Jetpack Compose, ViewModel.
- **Responsibilities**:
    - Rendering UI.
    - Handling user interactions.
    - Observing data from the Domain layer.

## 2. Domain (`domain`)
The core of the application containing business logic and use cases. It is a pure Kotlin module and does not depend on Android framework classes (where possible).
- **Responsibilities**:
    - Defining `Entities` (data models).
    - Defining `Repository` interfaces.
    - Implementing `Use Cases` (interactors) that encapsulate business rules.

## 3. Data (`data`)
This module implements the repository interfaces defined in the Domain layer. It handles data retrieval from various sources (database, network, file system).
- **Responsibilities**:
    - Implementing Repositories.
    - Managing Database (Room/SQLDelight).
    - Network calls (Ktor/Retrofit).
    - Data mapping (DTOs to Domain Entities).

## 4. Core (`core`)
Contains common utility classes, extensions, and base classes used across other modules.
- **Responsibilities**:
    - Dependency Injection setup (Koin/Hilt).
    - Common extension functions.
    - Logger, Preferences, and other infrastructure components.

## Dependency Graph
`Presentation` -> `Domain` <- `Data`
`Core` is used by all modules.

## Key Principles
- **Unidirectional Data Flow**: Data flows from Data -> Domain -> Presentation. Events flow from Presentation -> Domain -> Data.
- **Dependency Inversion**: High-level modules (Domain) do not depend on low-level modules (Data). Both depend on abstractions (Interfaces).
