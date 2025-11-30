# IReader Developer Documentation

Welcome to the IReader developer documentation! This directory contains comprehensive guides for developers working on the IReader project.

## 📚 Documentation Index

### Architecture & Design

- **[ARCHITECTURE.md](ARCHITECTURE.md)** - Complete architecture guide
  - Clean Architecture principles
  - Module structure and responsibilities
  - Layer boundaries and dependency rules
  - Development guidelines and best practices
  - Code organization patterns
  - Testing strategies

- **[MODULE_DEPENDENCIES.md](../MODULE_DEPENDENCIES.md)** - Module dependency documentation
  - Detailed dependency graph
  - Module overview and responsibilities
  - Allowed and forbidden dependencies
  - External dependency categories
  - Troubleshooting dependency issues

### Build & Configuration

- **[BUILD_OPTIMIZATION.md](../BUILD_OPTIMIZATION.md)** - Build configuration guide
  - Version catalog structure
  - Dependency management
  - Build performance optimizations
  - Common build configurations

## 🚀 Quick Start

### For New Developers

1. Start with **[ARCHITECTURE.md](ARCHITECTURE.md)** to understand the project structure
2. Review **[MODULE_DEPENDENCIES.md](../MODULE_DEPENDENCIES.md)** to understand module relationships
3. Check **[BUILD_OPTIMIZATION.md](../BUILD_OPTIMIZATION.md)** for build setup

### For Feature Development

1. Review the architecture guide for layer responsibilities
2. Follow the "Adding a New Feature" section in ARCHITECTURE.md
3. Ensure your code follows clean architecture principles
4. Add tests for your feature
5. Document public APIs with KDoc

### For Build Issues

1. Check **[BUILD_OPTIMIZATION.md](../BUILD_OPTIMIZATION.md)** troubleshooting section
2. Review **[MODULE_DEPENDENCIES.md](../MODULE_DEPENDENCIES.md)** for dependency conflicts
3. Use Gradle commands to analyze dependencies

## 📖 Key Concepts

### Clean Architecture

IReader follows Clean Architecture with three main layers:

```
Presentation → Domain → Data
```

- **Domain**: Business logic, use cases, repository interfaces
- **Data**: Repository implementations, database, network
- **Presentation**: UI, ViewModels, Compose screens

### Module Structure

```
:android, :desktop     # Platform entry points
:presentation          # UI layer
:domain                # Business logic
:data                  # Data access
:core                  # Shared utilities
:source-api            # Extension API
:i18n                  # Localization
```

### Dependency Rules

✅ **Allowed**: Presentation → Domain, Data → Domain
❌ **Forbidden**: Domain → Presentation, Domain → Data

## 🛠️ Development Workflow

### 1. Understanding the Codebase

```bash
# View module structure
ls -la

# Check dependencies
./gradlew :domain:dependencies

# Run tests
./gradlew test
```

### 2. Making Changes

1. Identify the appropriate layer (domain/data/presentation)
2. Follow existing patterns in that layer
3. Write tests for your changes
4. Document public APIs with KDoc
5. Ensure clean architecture principles are followed

### 3. Testing

```bash
# Run all tests
./gradlew test

# Run specific module tests
./gradlew :domain:test

# Run with coverage
./gradlew testDebugUnitTest jacocoTestReport
```

### 4. Building

```bash
# Clean build
./gradlew clean build

# Build specific variant
./gradlew assembleDebug

# Build desktop
./gradlew :desktop:packageDistributionForCurrentOS
```

## 📝 Documentation Standards

### KDoc Comments

All public APIs in the domain layer should have KDoc comments:

```kotlin
/**
 * Brief description of the class or function.
 * 
 * Detailed explanation if needed.
 * 
 * @param paramName Description of parameter
 * @return Description of return value
 * @throws ExceptionType When this exception is thrown
 */
```

### Code Comments

- Use comments to explain "why", not "what"
- Keep comments up-to-date with code changes
- Remove commented-out code
- Use TODO comments for future improvements

### Documentation Updates

When making significant changes:

1. Update relevant documentation files
2. Add examples if introducing new patterns
3. Update architecture diagrams if structure changes
4. Keep documentation in sync with code

## 🔍 Finding Information

### By Topic

- **Architecture**: ARCHITECTURE.md
- **Dependencies**: MODULE_DEPENDENCIES.md
- **Build**: BUILD_OPTIMIZATION.md
- **Features**: Feature-specific guides

### By Module

Each module's responsibilities are documented in:
- ARCHITECTURE.md (Module Structure section)
- MODULE_DEPENDENCIES.md (Module Overview section)

### By Layer

Layer-specific guidelines are in:
- ARCHITECTURE.md (Layer Responsibilities section)

## 🤝 Contributing

### Documentation Contributions

- Keep documentation clear and concise
- Use examples to illustrate concepts
- Update documentation with code changes
- Follow existing documentation style

### Code Contributions

1. Follow clean architecture principles
2. Write tests for new features
3. Document public APIs
4. Update relevant documentation
5. Follow Kotlin coding conventions

## 📞 Getting Help

### Common Questions

1. **Where should my code go?**
   - See ARCHITECTURE.md "Layer Responsibilities"

2. **How do I add a dependency?**
   - See BUILD_OPTIMIZATION.md "Adding New Dependencies"

3. **Why is my build failing?**
   - See BUILD_OPTIMIZATION.md "Troubleshooting"

4. **How do I test my feature?**
   - See ARCHITECTURE.md "Testing Strategy"

### Resources

- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)

## 📅 Maintenance

### Regular Updates

- Review and update documentation quarterly
- Keep examples current with latest code
- Update metrics and benchmarks
- Add new patterns as they emerge

### Version History

- **1.0** (2025-11-13): Initial comprehensive documentation
  - Architecture guide
  - Module dependencies
  - Build optimization
  - Developer guides

---

**Questions or suggestions?** Open an issue or submit a PR to improve this documentation!
