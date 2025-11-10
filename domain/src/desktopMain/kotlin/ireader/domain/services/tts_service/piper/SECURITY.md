# Piper TTS Security Features

This document describes the security features implemented in the Piper TTS integration to ensure safe and secure operation.

## Overview

The Piper TTS implementation includes comprehensive security measures to protect against:
- Malicious or corrupted native libraries
- Path traversal attacks
- Resource exhaustion (DoS)
- Invalid or malicious input
- Unauthorized file access

## Security Components

### 1. LibraryVerifier

**Purpose**: Verifies the integrity and authenticity of native libraries before loading.

**Features**:
- SHA-256 checksum verification
- Code signature verification (Windows/macOS)
- File integrity checks
- Security audit logging

**Usage**:
```kotlin
val verifier = LibraryVerifier()
val result = verifier.verifyLibrary(libraryPath)

if (result.isVerified) {
    // Safe to load the library
    System.load(libraryPath.toString())
} else {
    // Handle verification failure
    println(result.getReport())
}
```

**Configuration**:

Library verification is enabled by default. To disable (not recommended for production):
```
-Dpiper.verify.libraries=false
```

To disable signature verification only:
```
-Dpiper.verify.signatures=false
```

**Checksum Management**:

The `LibraryVerifier` maintains a map of known checksums for library files. When building new library versions:

1. Calculate checksums for all libraries:
```kotlin
val verifier = LibraryVerifier()
verifier.generateChecksums(listOf(
    Path.of("piper_jni.dll"),
    Path.of("onnxruntime.dll")
))
```

2. Update the `knownChecksums` map in `LibraryVerifier.kt` with the output

### 2. InputSanitizer

**Purpose**: Validates and sanitizes all user inputs to prevent injection attacks and invalid data.

**Features**:
- Text sanitization (removes control characters)
- File path validation (prevents path traversal)
- Parameter range validation
- URL validation
- Filename sanitization

**Usage**:

**Text Sanitization**:
```kotlin
val sanitizer = InputSanitizer()
val safeText = sanitizer.sanitizeText(userInput)
val audio = PiperNative.synthesize(instance, safeText)
```

**File Path Validation**:
```kotlin
val result = sanitizer.validateModelPath(modelPath)
if (result.isValid) {
    // Safe to use the path
    val instance = PiperNative.initialize(result.sanitizedValue!!, configPath)
}
```

**Parameter Validation**:
```kotlin
val result = sanitizer.validateRange(
    value = speechRate,
    min = 0.5f,
    max = 2.0f,
    paramName = "speechRate"
)
```

**Limits**:
- Maximum text length: 100,000 characters
- Maximum model file size: 500 MB
- Maximum config file size: 10 MB

### 3. SecurityManager

**Purpose**: Enforces security policies and manages resource access.

**Features**:
- File access control (sandboxing)
- Resource limit enforcement
- Instance tracking
- Security event logging
- Permission management

**Usage**:

**File Access Control**:
```kotlin
val securityManager = SecurityManager()

if (securityManager.canLoadModel(modelPath)) {
    // Safe to load the model
    val instance = PiperNative.initialize(modelPath, configPath)
}
```

**Resource Monitoring**:
```kotlin
if (securityManager.canAllocateMemory(requiredBytes)) {
    // Safe to proceed with allocation
}
```

**Instance Management**:
```kotlin
val instanceId = PiperNative.initialize(modelPath, configPath)
securityManager.registerInstance(instanceId)

// ... use the instance ...

PiperNative.shutdown(instanceId)
securityManager.unregisterInstance(instanceId)
```

**Security Policy**:

The default security policy:
- Allows model files only in approved directories (user's app data folder)
- Limits model file size to 500 MB
- Limits config file size to 10 MB
- Limits memory usage to 2 GB
- Limits concurrent instances to 10
- Allows only `.onnx` model files
- Allows only `.json`, `.yaml`, `.yml` config files

**Custom Policy**:
```kotlin
val customPolicy = SecurityManager.SecurityPolicy(
    allowedModelDirectories = setOf(Path.of("/custom/models")),
    maxModelFileSize = 1024L * 1024 * 1024, // 1 GB
    maxConcurrentInstances = 5
)

val securityManager = SecurityManager(customPolicy)
```

**Security Reports**:
```kotlin
println(securityManager.getSecurityReport())
```

## Security Best Practices

### For Developers

1. **Always validate inputs**: Use `InputSanitizer` for all user-provided data
2. **Verify libraries**: Keep library verification enabled in production
3. **Monitor security events**: Regularly review security logs
4. **Update checksums**: When updating libraries, regenerate and update checksums
5. **Limit permissions**: Use the most restrictive security policy that works
6. **Handle errors gracefully**: Don't expose sensitive information in error messages

### For Users

1. **Download from trusted sources**: Only download voice models from official sources
2. **Keep software updated**: Install security updates promptly
3. **Monitor resource usage**: Watch for unusual memory or CPU usage
4. **Report suspicious activity**: Report any security concerns to the developers

## Security Event Logging

All security-relevant events are logged for audit purposes:

**Event Types**:
- `FILE_ACCESS_DENIED`: File access was denied by security policy
- `FILE_ACCESS_GRANTED`: File access was granted (if logging enabled)
- `RESOURCE_LIMIT_EXCEEDED`: Resource limit was exceeded
- `INVALID_FILE_EXTENSION`: Invalid file extension detected
- `PATH_TRAVERSAL_ATTEMPT`: Potential path traversal attack detected
- `SUSPICIOUS_ACTIVITY`: Suspicious behavior detected
- `POLICY_VIOLATION`: Security policy violation

**Viewing Events**:
```kotlin
val securityManager = SecurityManager()

// Get recent events
val events = securityManager.getRecentEvents(limit = 50)

// Get events by type
val deniedAccess = securityManager.getEventsByType(
    SecurityEventType.FILE_ACCESS_DENIED
)
```

## Threat Model

### Threats Mitigated

1. **Malicious Libraries**: Prevented by checksum and signature verification
2. **Path Traversal**: Prevented by path validation and sandboxing
3. **Resource Exhaustion**: Prevented by resource limits and monitoring
4. **Injection Attacks**: Prevented by input sanitization
5. **Unauthorized Access**: Prevented by file access control

### Known Limitations

1. **Memory-based attacks**: Limited protection against sophisticated memory attacks
2. **Side-channel attacks**: No specific protection against timing attacks
3. **Social engineering**: Cannot prevent users from manually bypassing security
4. **Zero-day exploits**: Cannot protect against unknown vulnerabilities in dependencies

## Compliance

The security implementation follows industry best practices:
- OWASP Top 10 guidelines
- CWE/SANS Top 25 Most Dangerous Software Errors
- Secure coding standards for Java/Kotlin

## Reporting Security Issues

If you discover a security vulnerability, please report it to:
- Email: security@example.com
- Do not disclose publicly until patched
- Include detailed reproduction steps
- Allow reasonable time for fix before disclosure

## Security Updates

Security updates are released as needed. Subscribe to security advisories:
- GitHub Security Advisories
- Project mailing list
- Release notes

## Additional Resources

- [OWASP Secure Coding Practices](https://owasp.org/www-project-secure-coding-practices-quick-reference-guide/)
- [CWE Top 25](https://cwe.mitre.org/top25/)
- [Java Security Guidelines](https://www.oracle.com/java/technologies/javase/seccodeguide.html)

## Version History

- **v1.0.0** (2024-11-10): Initial security implementation
  - Library integrity verification
  - Input sanitization
  - Sandboxing and permissions
  - Security event logging
