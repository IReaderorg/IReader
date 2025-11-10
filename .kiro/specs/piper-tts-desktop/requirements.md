# Requirements Document

## Introduction

This document outlines the requirements for integrating Piper TTS (Text-to-Speech) into the IReader desktop application. Piper is an open-source neural text-to-speech system that uses the VITS architecture and runs in ONNX format, enabling high-quality, offline-capable speech synthesis on desktop platforms. This integration will replace the current simulated reading approach with actual audio output, providing users with a professional-grade reading experience that works offline and bypasses any regional restrictions.

## Glossary

- **Piper TTS**: An open-source neural text-to-speech system that generates natural-sounding speech from text using AI models
- **ONNX**: Open Neural Network Exchange, a universal format for AI models that enables cross-platform compatibility and optimization
- **VITS**: Variational Inference with adversarial learning for end-to-end Text-to-Speech, the AI architecture used by Piper
- **Voice Model**: A trained neural network file that contains the parameters for generating speech in a specific voice and language
- **DesktopTTSService**: The existing service class that manages TTS operations on the desktop platform
- **Audio Playback Engine**: The component responsible for playing generated audio through the system's audio output
- **Model Manager**: The component responsible for downloading, storing, and managing Piper voice models
- **Speech Synthesizer**: The component that interfaces with Piper to convert text into audio waveforms

## Requirements

### Requirement 1

**User Story:** As a desktop user, I want the application to generate actual speech audio from text, so that I can listen to my books with natural-sounding voices instead of simulated reading.

#### Acceptance Criteria

1. WHEN the user presses the play button on a chapter, THE DesktopTTSService SHALL generate audio waveforms from the chapter text using Piper TTS
2. WHEN audio generation completes for a text segment, THE Audio Playback Engine SHALL play the generated audio through the system's default audio output device
3. THE Speech Synthesizer SHALL process text in manageable chunks to prevent memory overflow and enable responsive playback controls
4. WHEN the user adjusts the speech rate setting, THE Speech Synthesizer SHALL apply the rate multiplier to the generated audio without regenerating the voice model
5. THE DesktopTTSService SHALL maintain the current paragraph tracking and chapter navigation functionality while playing actual audio

### Requirement 2

**User Story:** As a desktop user, I want to download and select from multiple voice models, so that I can choose a voice that suits my preferences and language needs.

#### Acceptance Criteria

1. THE Model Manager SHALL provide a list of available Piper voice models with metadata including language, gender, quality level, and file size
2. WHEN the user selects a voice model for download, THE Model Manager SHALL download the ONNX model file and configuration JSON from the Piper repository
3. THE Model Manager SHALL store downloaded voice models in a persistent local directory within the application data folder
4. WHEN the user selects a different voice model, THE Speech Synthesizer SHALL load the new model and use it for subsequent speech generation
5. THE Model Manager SHALL verify the integrity of downloaded model files using checksums before marking them as available

### Requirement 3

**User Story:** As a desktop user, I want the TTS to work completely offline, so that I can listen to my books without an internet connection and without regional restrictions.

#### Acceptance Criteria

1. THE Speech Synthesizer SHALL generate speech audio using only locally stored voice models without requiring network connectivity
2. WHEN no internet connection is available, THE DesktopTTSService SHALL continue to function normally with previously downloaded voice models
3. THE Model Manager SHALL cache all necessary model files and configurations locally after the initial download
4. THE DesktopTTSService SHALL not depend on any external API calls or cloud services for speech generation
5. WHEN the application starts offline, THE DesktopTTSService SHALL initialize successfully if at least one voice model is already downloaded

### Requirement 4

**User Story:** As a desktop user, I want responsive playback controls, so that I can pause, resume, and navigate through chapters without delays.

#### Acceptance Criteria

1. WHEN the user presses the pause button, THE Audio Playback Engine SHALL stop audio playback within 200 milliseconds
2. WHEN the user presses the resume button, THE Audio Playback Engine SHALL continue playback from the paused position within 200 milliseconds
3. WHEN the user skips to the next paragraph, THE DesktopTTSService SHALL stop current audio playback and begin generating audio for the new paragraph within 500 milliseconds
4. WHEN the user skips to a different chapter, THE DesktopTTSService SHALL cancel any pending audio generation and load the new chapter content within 1 second
5. THE Speech Synthesizer SHALL process text in streaming fashion to enable playback to begin before the entire chapter is synthesized

### Requirement 5

**User Story:** As a desktop user, I want the TTS to highlight the currently spoken word or sentence, so that I can follow along visually while listening.

#### Acceptance Criteria

1. WHEN audio playback is active, THE DesktopTTSService SHALL emit word boundary events containing the word index and text offset
2. WHEN a word boundary event is emitted, THE Reader UI SHALL highlight the corresponding word in the displayed text
3. THE Speech Synthesizer SHALL calculate word boundaries based on the generated audio timing information
4. WHEN the user scrolls away from the current reading position, THE Reader UI SHALL maintain the highlight but not force auto-scroll
5. WHEN playback is paused or stopped, THE Reader UI SHALL remove the word highlighting

### Requirement 6

**User Story:** As a desktop user, I want the application to handle errors gracefully, so that TTS failures do not crash the application or prevent me from reading.

#### Acceptance Criteria

1. WHEN a voice model fails to load, THE Model Manager SHALL log the error and display a user-friendly error message without crashing the application
2. WHEN audio generation fails for a text segment, THE DesktopTTSService SHALL skip to the next segment and log the error
3. WHEN the audio playback device is unavailable, THE Audio Playback Engine SHALL display an error message and allow the user to retry or select a different output device
4. WHEN disk space is insufficient for model downloads, THE Model Manager SHALL display a clear error message and cancel the download
5. IF the Piper library fails to initialize, THEN THE DesktopTTSService SHALL fall back to the simulated reading mode and notify the user

### Requirement 7

**User Story:** As a desktop user, I want efficient resource usage, so that TTS does not consume excessive CPU, memory, or battery power.

#### Acceptance Criteria

1. THE Speech Synthesizer SHALL limit memory usage to a maximum of 500 MB during active speech generation
2. THE Model Manager SHALL load voice models lazily only when needed for speech generation
3. WHEN the application is idle, THE Speech Synthesizer SHALL release loaded models from memory after 5 minutes of inactivity
4. THE Audio Playback Engine SHALL use buffered streaming to minimize memory footprint for long chapters
5. THE Speech Synthesizer SHALL utilize hardware acceleration when available on the user's system

### Requirement 8

**User Story:** As a desktop user, I want my TTS preferences to persist, so that my voice selection and settings are remembered across application sessions.

#### Acceptance Criteria

1. WHEN the user selects a voice model, THE Model Manager SHALL save the selection to the application preferences
2. WHEN the application starts, THE DesktopTTSService SHALL load the previously selected voice model automatically
3. THE DesktopTTSService SHALL persist speech rate, pitch, and volume settings to preferences
4. WHEN the user changes TTS settings, THE DesktopTTSService SHALL apply the changes immediately and save them to preferences
5. THE Model Manager SHALL maintain a list of downloaded models in preferences to avoid re-scanning the file system on startup
