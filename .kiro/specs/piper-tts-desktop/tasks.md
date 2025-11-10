# Implementation Plan

- [x] 1. Set up Piper native library infrastructure








  - Create JNI wrapper structure for Piper C++ library
  - Set up native library loading mechanism for Windows, macOS, and Linux
  - Create PiperNative object with external function declarations
  - Configure build.gradle.kts to include native libraries in resources
  - _Requirements: 1.1, 3.1, 3.4_





- [ ] 2. Implement core data models








  - [ ] 2.1 Create AudioData data class with PCM format support
    - Define AudioData with samples, sampleRate, channels, and format properties


    - Create AudioFormat enum with PCM_16, PCM_24, PCM_32 values
    - _Requirements: 1.1, 1.2_



  - [x] 2.2 Create AudioChunk data class for streaming


    - Define AudioChunk with data, text, and isLast properties
    - _Requirements: 1.3, 4.5_

  - [x] 2.3 Create WordBoundary data class for text highlighting


    - Define WordBoundary with word, offsets, and timing properties
    - _Requirements: 5.1, 5.2, 5.3_



  - [ ] 2.4 Extend DesktopTTSState with Piper-specific properties
    - Add currentWordBoundary, availableVoiceModels, selectedVoiceModel properties
    - Add isDownloadingModel and downloadProgress properties


    - _Requirements: 5.1, 2.1, 8.1_




- [ ] 3. Implement PiperSpeechSynthesizer








  - [ ] 3.1 Create SpeechSynthesizer interface
    - Define initialize, synthesize, synthesizeStream, getWordBoundaries, and shutdown methods


    - _Requirements: 1.1, 1.3, 5.3_

  - [x] 3.2 Implement PiperSpeechSynthesizer class


    - Implement initialize method to load ONNX model and config
    - Implement synthesize method to generate audio from text
    - Add error handling with Result types
    - _Requirements: 1.1, 6.1, 6.2_


  - [ ] 3.3 Implement streaming synthesis
    - Create synthesizeStream method that splits text into sentences
    - Return Flow<AudioChunk> for progressive audio generation
    - _Requirements: 1.3, 4.5_


  - [ ] 3.4 Implement word boundary calculation
    - Create getWordBoundaries method to calculate word timings
    - Split text into words and estimate phoneme durations
    - _Requirements: 5.1, 5.2, 5.3_


  - [ ] 3.5 Add resource cleanup
    - Implement shutdown method to release native resources

    - _Requirements: 7.3_






- [ ] 4. Implement AudioPlaybackEngine


  - [x] 4.1 Create AudioPlayback interface

    - Define play, playStream, pause, resume, stop methods

    - Define getCurrentPosition and isPlaying methods
    - _Requirements: 1.2, 4.1, 4.2_



  - [ ] 4.2 Implement AudioPlaybackEngine with Java Sound API
    - Initialize SourceDataLine with proper AudioFormat


    - Implement play method for single audio playback
    - Add 8KB buffer configuration
    - _Requirements: 1.2, 7.4_



  - [ ] 4.3 Implement streaming playback
    - Create playStream method that consumes Flow<AudioChunk>
    - Handle pause/resume during streaming
    - Track current playback position
    - _Requirements: 4.5, 7.4_



  - [ ] 4.4 Implement playback controls
    - Implement pause method with <200ms response time



    - Implement resume method with <200ms response time


    - Implement stop method with buffer cleanup
    - _Requirements: 4.1, 4.2_



  - [ ] 4.5 Add error handling for audio device issues


    - Handle unavailable audio devices gracefully



    - Provide user-friendly error messages
    - _Requirements: 6.3_



- [ ] 5. Implement PiperModelManager


  - [x] 5.1 Create VoiceModel data class


    - Define VoiceModel with id, name, language, quality, gender, size, URLs
    - Create Quality and Gender enums
    - _Requirements: 2.1_



  - [ ] 5.2 Create ModelManager interface
    - Define getAvailableModels, downloadModel, getDownloadedModels methods
    - Define deleteModel and getModelPaths methods
    - _Requirements: 2.1, 2.2, 2.3, 2.4_

  - [x] 5.3 Implement PiperModelManager class


    - Initialize models directory in app data folder



    - Implement getAvailableModels from embedded JSON resource
    - _Requirements: 2.1, 2.3_



  - [ ] 5.4 Implement model download functionality
    - Create downloadModel method with progress Flow
    - Download ONNX model and config JSON files


    - Verify file integrity with checksums
    - _Requirements: 2.2, 2.5, 6.4_

  - [x] 5.5 Implement model storage and retrieval


    - Create getDownloadedModels to scan local storage
    - Implement getModelPaths to return file paths
    - Implement deleteModel for cleanup




    - _Requirements: 2.3, 2.4_




  - [ ] 5.6 Add storage space validation
    - Check available disk space before downloads


    - Display clear error for insufficient storage
    - _Requirements: 6.4_


- [x] 6. Enhance DesktopTTSService with Piper integration



  - [ ] 6.1 Inject Piper components into service
    - Add PiperSpeechSynthesizer, AudioPlaybackEngine, PiperModelManager dependencies


    - Update initialize method to load selected voice model

    - _Requirements: 1.1, 8.2_

  - [x] 6.2 Update readText method for audio synthesis


    - Replace simulation logic with Piper synthesize call
    - Play generated audio through AudioPlaybackEngine
    - Maintain paragraph progression logic
    - _Requirements: 1.1, 1.2, 1.5_



  - [ ] 6.3 Implement word boundary tracking
    - Create startWordBoundaryTracking method


    - Emit word boundary events to state
    - Calculate timing based on audio playback position


    - _Requirements: 5.1, 5.2, 5.3_

  - [ ] 6.4 Update playback control methods
    - Modify pauseReading to pause AudioPlaybackEngine
    - Modify stopReading to stop audio and clear state
    - Ensure <200ms response times
    - _Requirements: 4.1, 4.2_

  - [ ] 6.5 Update chapter navigation
    - Modify skipToNextChapter to stop audio before loading
    - Modify skipToPreviousChapter with audio cleanup
    - Ensure <500ms paragraph skip response
    - _Requirements: 4.3, 4.4_

  - [ ] 6.6 Add fallback to simulation mode
    - Detect Piper initialization failures

    - Fall back to existing simulation logic
    - Notify user of fallback mode


    - _Requirements: 6.5_


  - [ ] 6.7 Update shutdown method
    - Call synthesizer.shutdown() to release resources
    - Stop audio playback engine
    - _Requirements: 7.3_

- [ ] 7. Implement model management UI


  - [ ] 7.1 Create VoiceModelSelector composable
    - Display list of available voice models
    - Show model metadata (language, quality, size)

    - Indicate downloaded vs available models
    - _Requirements: 2.1_

  - [ ] 7.2 Add model download UI
    - Create download button for each model
    - Display download progress bar
    - Show download status messages
    - _Requirements: 2.2_

  - [ ] 7.3 Add model selection UI

    - Allow user to select active voice model
    - Persist selection to preferences
    - Reload synthesizer when model changes
    - _Requirements: 2.4, 8.1_

  - [ ] 7.4 Add model management actions
    - Implement delete model functionality
    - Show storage usage information
    - Add confirmation dialogs
    - _Requirements: 2.4_




- [ ] 8. Implement word highlighting in Reader UI




  - [ ] 8.1 Update Reader text rendering
    - Observe currentWordBoundary from TTS state


    - Apply highlight style to current word

    - _Requirements: 5.2_







  - [x] 8.2 Add highlight styling


    - Create highlighted text style with background color
    - Ensure highlight is visible in light and dark themes
    - _Requirements: 5.2_



  - [ ] 8.3 Handle scroll behavior
    - Maintain highlight without forcing auto-scroll


    - Clear highlight when playback stops
    - _Requirements: 5.4, 5.5_

- [ ] 9. Add preference persistence


  - [ ] 9.1 Add Piper preferences to AppPreferences

    - Add selectedPiperModel preference
    - Add downloadedModels list preference
    - _Requirements: 8.1, 8.5_

  - [ ] 9.2 Load preferences on service initialization
    - Read selectedPiperModel and load voice
    - Restore TTS settings (rate, pitch, volume)
    - _Requirements: 8.2, 8.3_

  - [ ] 9.3 Save preferences on changes
    - Persist model selection immediately
    - Save TTS settings when modified
    - _Requirements: 8.4_

- [ ] 10. Implement error handling and logging


  - [ ] 10.1 Create TTSError sealed class

    - Define error types: ModelLoadError, SynthesisError, AudioPlaybackError, etc.
    - _Requirements: 6.1, 6.2, 6.3, 6.4_


  - [ ] 10.2 Create TTSErrorHandler
    - Implement handle method for each error type
    - Add user notifications for errors
    - Implement fallback strategies
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

  - [ ] 10.3 Add comprehensive logging
    - Log model loading events
    - Log synthesis operations
    - Log audio playback events
    - _Requirements: 6.1, 6.2, 6.3_

- [ ] 11. Create embedded voice models catalog


  - [ ] 11.1 Create piper_models.json resource
    - List available Piper voice models with metadata
    - Include download URLs for models and configs
    - Add checksums for integrity verification
    - _Requirements: 2.1, 2.5_

  - [ ] 11.2 Select default voice models
    - Choose high-quality English voice as default
    - Include at least one model per major language
    - _Requirements: 2.1_

- [ ] 12. Optimize performance and resource usage

  - [ ] 12.1 Implement lazy model loading
    - Load models only when needed
    - Unload models after 5 minutes of inactivity

    - _Requirements: 7.2, 7.3_


  - [ ] 12.2 Add memory usage monitoring
    - Track synthesizer memory usage
    - Limit to 500MB maximum
    - _Requirements: 7.1_

  - [ ] 12.3 Optimize text chunking
    - Split long paragraphs into 500-character chunks
    - Process chunks asynchronously
    - _Requirements: 1.3, 7.4_

  - [ ] 12.4 Enable hardware acceleration
    - Configure ONNX Runtime to use CPU SIMD
    - Detect and use GPU if available

    - _Requirements: 7.5_

- [ ] 13. Add platform-specific configurations


  - [ ] 13.1 Configure Windows audio backend
    - Use WASAPI for low-latency audio
    - Bundle required DLLs
    - _Requirements: 1.2_

  - [ ] 13.2 Configure macOS audio backend
    - Use Core Audio for playback
    - Handle Gatekeeper signing requirements
    - _Requirements: 1.2_

  - [ ] 13.3 Configure Linux audio backend
    - Support both ALSA and PulseAudio
    - Handle different audio server configurations
    - _Requirements: 1.2_

- [ ] 14. Create user documentation


  - [ ] 14.1 Write TTS setup guide
    - Document how to download voice models
    - Explain voice selection and settings
    - _Requirements: 2.1, 2.2, 2.4_

  - [ ] 14.2 Create troubleshooting guide
    - Document common issues and solutions
    - Explain fallback mode
    - _Requirements: 6.1, 6.2, 6.3, 6.5_

  - [ ] 14.3 Update README with Piper TTS information
    - Add feature description
    - List supported platforms
    - _Requirements: 1.1, 3.1_
