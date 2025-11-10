# Piper JNI Documentation

Welcome to the Piper JNI documentation for IReader. This directory contains comprehensive documentation for developers and users working with the Piper text-to-speech integration.

## Documentation Overview

### For Developers

- **[Developer Guide](Developer_Guide.md)** - Complete technical documentation
  - Architecture overview
  - Build process and setup
  - API reference
  - Integration guide
  - Troubleshooting
  - Performance optimization

- **[Code Examples](Code_Examples.md)** - Practical code examples
  - Basic usage examples
  - Advanced features
  - Error handling patterns
  - Performance optimization techniques
  - Complete application examples

### For Users

- **[User Guide](User_Guide.md)** - End-user documentation
  - Getting started
  - Voice model management
  - TTS controls and features
  - Multi-language support
  - Accessibility features
  - FAQ

### Additional Resources

- **[TTS Setup Guide](../TTS_Setup_Guide.md)** - Initial setup instructions
- **[TTS Troubleshooting Guide](../TTS_Troubleshooting_Guide.md)** - Common issues and solutions

## Quick Start

### For Developers

1. Read the [Developer Guide](Developer_Guide.md) architecture section
2. Follow the build process instructions
3. Review the [Code Examples](Code_Examples.md) for integration patterns
4. Check the API reference for detailed method documentation

### For Users

1. Read the [User Guide](User_Guide.md) getting started section
2. Download your first voice model
3. Start using TTS with your books
4. Explore advanced features as needed

## Key Features

- **Offline Operation**: Works completely offline once voice models are downloaded
- **High Quality**: Neural voices with natural-sounding speech
- **Multi-Language**: Support for 20+ languages
- **Cross-Platform**: Windows, macOS, and Linux support
- **Privacy-First**: All processing happens locally on your device
- **Customizable**: Adjustable speech rate, pitch, and other parameters

## System Requirements

### Minimum Requirements

- **OS**: Windows 10, macOS 10.15, or Linux (glibc 2.27+)
- **RAM**: 4 GB
- **Storage**: 100 MB for voice models
- **CPU**: Dual-core processor

### Recommended Requirements

- **OS**: Windows 11, macOS 12+, or modern Linux
- **RAM**: 8 GB or more
- **Storage**: 500 MB for multiple voice models
- **CPU**: Quad-core processor or better

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Kotlin/Java Application                   │
│                  (DesktopTTSService)                         │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                  PiperNative (JNI Interface)                 │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│              piper_jni (C++ JNI Wrapper)                     │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                  Piper Core Library                          │
│         (Phonemizer + ONNX Runtime + Audio)                  │
└─────────────────────────────────────────────────────────────┘
```

## Technology Stack

- **JNI**: Java Native Interface for Kotlin/Java ↔ C++ bridge
- **CMake**: Cross-platform build system
- **Piper TTS**: Neural text-to-speech engine
- **ONNX Runtime**: Machine learning inference
- **espeak-ng**: Phonemization library

## Getting Help

### Documentation

- Browse the guides in this directory
- Check the [FAQ section](User_Guide.md#frequently-asked-questions-faq) in the User Guide
- Review [troubleshooting guides](Developer_Guide.md#troubleshooting-guide)

### Community

- [GitHub Issues](https://github.com/IReaderorg/IReader/issues) - Report bugs or request features
- [GitHub Discussions](https://github.com/IReaderorg/IReader/discussions) - Ask questions and share tips

### Contributing

Contributions to documentation are welcome! See the main [CONTRIBUTING.md](../../CONTRIBUTING.md) for guidelines.

## License

This documentation is part of the IReader project and is licensed under the Mozilla Public License 2.0.

Third-party components:
- **Piper TTS**: MIT License
- **ONNX Runtime**: MIT License
- **espeak-ng**: GPL v3

## Version Information

- **Documentation Version**: 1.0.0
- **Last Updated**: 2025-11-10
- **Piper JNI Version**: See [Developer Guide](Developer_Guide.md) for version information

## Document Index

### Developer Documentation

| Document | Description | Audience |
|----------|-------------|----------|
| [Developer Guide](Developer_Guide.md) | Complete technical reference | Developers |
| [Code Examples](Code_Examples.md) | Practical code samples | Developers |

### User Documentation

| Document | Description | Audience |
|----------|-------------|----------|
| [User Guide](User_Guide.md) | End-user manual | End Users |
| [TTS Setup Guide](../TTS_Setup_Guide.md) | Initial setup | End Users |
| [TTS Troubleshooting](../TTS_Troubleshooting_Guide.md) | Problem solving | End Users |

## Feedback

Found an issue with the documentation? Have suggestions for improvement?

- Open an issue on [GitHub](https://github.com/IReaderorg/IReader/issues)
- Submit a pull request with improvements
- Join the discussion in [GitHub Discussions](https://github.com/IReaderorg/IReader/discussions)

---

**Note**: This documentation covers the production-ready Piper JNI integration. For information about other IReader features, see the main [README](../../README.md).
