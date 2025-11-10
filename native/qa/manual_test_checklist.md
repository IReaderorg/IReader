# Manual Testing Checklist

This checklist should be completed before each release to ensure all features work correctly.

## Test Environment

- **Tester Name**: _______________
- **Date**: _______________
- **Version**: _______________
- **Platform**: ☐ Windows ☐ macOS ☐ Linux
- **Architecture**: ☐ x64 ☐ ARM64

## 1. Installation Tests

### Windows
- [ ] MSI installer runs without errors
- [ ] Application installs to correct location
- [ ] Start menu shortcut created
- [ ] Desktop shortcut created (if selected)
- [ ] Application launches from shortcuts
- [ ] Uninstaller works correctly
- [ ] No files left after uninstall

### macOS
- [ ] DMG mounts correctly
- [ ] Application can be dragged to Applications folder
- [ ] Application launches without security warnings
- [ ] Code signature is valid
- [ ] Application appears in Launchpad
- [ ] Application can be moved to Trash
- [ ] No files left after deletion

### Linux (DEB)
- [ ] Package installs with `dpkg -i`
- [ ] Dependencies are satisfied
- [ ] Application appears in application menu
- [ ] Desktop entry works
- [ ] Application launches from terminal
- [ ] Package removes cleanly with `dpkg -r`

### Linux (RPM)
- [ ] Package installs with `rpm -i`
- [ ] Dependencies are satisfied
- [ ] Application appears in application menu
- [ ] Desktop entry works
- [ ] Application launches from terminal
- [ ] Package removes cleanly with `rpm -e`

## 2. First Launch Tests

- [ ] Application launches successfully
- [ ] Welcome screen appears (if applicable)
- [ ] Default settings are reasonable
- [ ] No error dialogs on first launch
- [ ] Application window is properly sized
- [ ] Application icon displays correctly

## 3. TTS Functionality Tests

### Voice Model Management
- [ ] Voice catalog loads successfully
- [ ] Voice list displays correctly
- [ ] Voice metadata is accurate (language, gender, quality)
- [ ] Voice download works
- [ ] Download progress is displayed
- [ ] Download can be cancelled
- [ ] Downloaded voices appear in list
- [ ] Voice deletion works
- [ ] Storage usage is displayed correctly

### Text-to-Speech
- [ ] TTS initializes without errors
- [ ] Short text synthesis works (< 100 chars)
- [ ] Long text synthesis works (> 1000 chars)
- [ ] Audio plays correctly
- [ ] Audio quality is acceptable
- [ ] No audio glitches or artifacts
- [ ] Synthesis completes in reasonable time

### TTS Controls
- [ ] Play button starts TTS
- [ ] Pause button pauses TTS
- [ ] Resume continues from pause point
- [ ] Stop button stops TTS
- [ ] Skip forward works
- [ ] Skip backward works
- [ ] Progress bar updates correctly
- [ ] Time display is accurate

### Speech Parameters
- [ ] Speech rate adjustment works (0.5x - 2.0x)
- [ ] Rate changes apply immediately
- [ ] Slower rates sound natural
- [ ] Faster rates are intelligible
- [ ] Rate persists across sessions

### Multi-Language Support
- [ ] English voices work correctly
- [ ] Spanish voices work correctly
- [ ] French voices work correctly
- [ ] German voices work correctly
- [ ] Chinese voices work correctly
- [ ] Japanese voices work correctly
- [ ] Language detection works (if implemented)
- [ ] Voice switching works for multilingual text

## 4. eBook Reader Tests

### File Loading
- [ ] EPUB files load correctly
- [ ] PDF files load correctly
- [ ] Text files load correctly
- [ ] Large files load without hanging
- [ ] Corrupted files are handled gracefully
- [ ] File metadata displays correctly

### Reading Experience
- [ ] Text renders correctly
- [ ] Images display properly
- [ ] Page navigation works
- [ ] Table of contents works
- [ ] Bookmarks can be created
- [ ] Bookmarks can be deleted
- [ ] Reading position is saved
- [ ] Reading position is restored on relaunch

### TTS Integration
- [ ] TTS can be started from reader
- [ ] Currently spoken text is highlighted
- [ ] Highlight follows speech
- [ ] Page turns automatically when needed
- [ ] TTS stops at chapter end (if configured)
- [ ] TTS can resume after interruption

## 5. User Interface Tests

### General UI
- [ ] All buttons are clickable
- [ ] All menus work correctly
- [ ] Tooltips display on hover
- [ ] Keyboard shortcuts work
- [ ] Window can be resized
- [ ] Window can be maximized
- [ ] Window can be minimized
- [ ] Application can be closed

### Settings
- [ ] Settings dialog opens
- [ ] Settings can be changed
- [ ] Settings are saved
- [ ] Settings persist across sessions
- [ ] Default settings can be restored

### Themes
- [ ] Light theme works
- [ ] Dark theme works
- [ ] Theme switching works
- [ ] Theme persists across sessions
- [ ] All UI elements respect theme

### Accessibility
- [ ] Screen reader compatibility (if applicable)
- [ ] Keyboard navigation works
- [ ] High contrast mode works
- [ ] Font size can be adjusted
- [ ] UI is readable at different sizes

## 6. Performance Tests

### Startup Performance
- [ ] Application starts in < 5 seconds
- [ ] No visible lag during startup
- [ ] Splash screen displays (if applicable)

### Runtime Performance
- [ ] UI remains responsive during TTS
- [ ] No memory leaks during extended use
- [ ] CPU usage is reasonable
- [ ] Application doesn't freeze
- [ ] Large files don't cause slowdown

### Resource Usage
- [ ] Memory usage is acceptable (< 500 MB per voice)
- [ ] Disk usage is reasonable
- [ ] Network usage is minimal (only for downloads)
- [ ] Battery usage is acceptable (laptop/mobile)

## 7. Error Handling Tests

### Network Errors
- [ ] Offline mode works
- [ ] Download failures are handled gracefully
- [ ] Network errors show helpful messages
- [ ] Application doesn't crash on network errors

### File Errors
- [ ] Missing files are handled gracefully
- [ ] Corrupted files show error messages
- [ ] Permission errors are handled
- [ ] Disk full errors are handled

### TTS Errors
- [ ] Missing voice models are handled
- [ ] Synthesis errors show messages
- [ ] Application doesn't crash on TTS errors
- [ ] Fallback behavior works

## 8. Integration Tests

### System Integration
- [ ] File associations work (if configured)
- [ ] System tray integration works (if applicable)
- [ ] Notification system works
- [ ] Audio output device selection works
- [ ] Multiple audio devices are supported

### Cross-Platform Features
- [ ] Settings sync works (if implemented)
- [ ] Cloud storage integration works (if implemented)
- [ ] Import/export works

## 9. Security Tests

### Library Verification
- [ ] Native libraries are verified on load
- [ ] Tampered libraries are rejected
- [ ] Verification errors show messages

### Input Validation
- [ ] Long text inputs are handled
- [ ] Special characters are handled
- [ ] File path validation works
- [ ] No crashes from malformed input

### Privacy
- [ ] No data sent without permission
- [ ] Analytics can be disabled (if applicable)
- [ ] User data is stored securely
- [ ] No sensitive data in logs

## 10. Regression Tests

### Previous Issues
- [ ] All previously reported bugs are fixed
- [ ] No regressions from previous version
- [ ] New features don't break existing features

### Edge Cases
- [ ] Empty text input is handled
- [ ] Very long text is handled
- [ ] Special characters in filenames work
- [ ] Unicode text is handled correctly
- [ ] Multiple instances can run (if supported)

## 11. Documentation Tests

### User Documentation
- [ ] User guide is accurate
- [ ] Screenshots are up-to-date
- [ ] Examples work as described
- [ ] FAQ answers common questions

### Developer Documentation
- [ ] API documentation is accurate
- [ ] Code examples compile
- [ ] Build instructions work
- [ ] Troubleshooting guide is helpful

## 12. Release Artifacts Tests

### Installers
- [ ] Installer checksums are correct
- [ ] Installer signatures are valid (if applicable)
- [ ] Installer size is reasonable
- [ ] Installer includes all required files

### Documentation
- [ ] README is included
- [ ] LICENSE is included
- [ ] THIRD_PARTY_LICENSES.txt is included
- [ ] Release notes are accurate

### Metadata
- [ ] Version number is correct
- [ ] Build date is correct
- [ ] Copyright notices are present
- [ ] Attribution is complete

## Sign-Off

### Tester
- **Name**: _______________
- **Signature**: _______________
- **Date**: _______________

### Reviewer
- **Name**: _______________
- **Signature**: _______________
- **Date**: _______________

### Release Manager
- **Name**: _______________
- **Signature**: _______________
- **Date**: _______________

## Notes

Use this space to document any issues found during testing:

_______________________________________________________________________________
_______________________________________________________________________________
_______________________________________________________________________________
_______________________________________________________________________________
_______________________________________________________________________________

## Overall Assessment

- [ ] **PASS** - All critical tests passed, ready for release
- [ ] **PASS WITH NOTES** - Minor issues found, acceptable for release
- [ ] **FAIL** - Critical issues found, not ready for release

**Recommendation**: _______________________________________________________________
