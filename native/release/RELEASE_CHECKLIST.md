# Release Checklist

Complete this checklist before publishing a new release of IReader.

## Pre-Release

### Code Preparation
- [ ] All features complete and merged to main branch
- [ ] All tests passing
- [ ] No known critical bugs
- [ ] Code reviewed and approved
- [ ] Version number updated in all files
- [ ] Changelog updated

### Documentation
- [ ] README.md updated
- [ ] User guide updated
- [ ] Developer guide updated
- [ ] API documentation updated
- [ ] Release notes written
- [ ] Known issues documented

### Testing
- [ ] Full test suite passes (`native/qa/run_full_test_suite.sh`)
- [ ] Regression tests pass (`native/qa/regression_test_suite.sh`)
- [ ] Manual testing complete (`native/qa/manual_test_checklist.md`)
- [ ] Performance tests meet targets
- [ ] Cross-platform testing complete:
  - [ ] Windows 10/11 (x64)
  - [ ] macOS Intel (x64)
  - [ ] macOS Apple Silicon (ARM64)
  - [ ] Ubuntu 20.04+ (x64)
  - [ ] Fedora (x64)

### License Compliance
- [ ] License verification passes (`native/licensing/verify_licenses.sh`)
- [ ] All license files included
- [ ] Third-party licenses documented
- [ ] Attribution notices complete
- [ ] Voice model licenses verified

### Build Artifacts
- [ ] Project builds successfully
- [ ] Native libraries compiled for all platforms
- [ ] Windows MSI installer created
- [ ] macOS DMG created and signed
- [ ] Linux DEB package created
- [ ] Linux RPM package created
- [ ] All installers tested on clean systems

## Release Creation

### Version Control
- [ ] Create release branch: `release/v{VERSION}`
- [ ] Update version numbers
- [ ] Commit all changes
- [ ] Create and push Git tag: `v{VERSION}`

### Package Creation
- [ ] Run `./create_release.sh {VERSION}`
- [ ] Verify package contents
- [ ] Test source archive extraction
- [ ] Verify checksums
- [ ] Review release notes
- [ ] Review release summary

### Quality Assurance
- [ ] Install from each installer type
- [ ] Test basic functionality
- [ ] Verify TTS works
- [ ] Check voice downloads
- [ ] Test on clean systems
- [ ] Verify uninstall works

## Publication

### GitHub Release
- [ ] Create GitHub release (draft)
- [ ] Upload source archive
- [ ] Upload checksums file
- [ ] Upload Windows installer
- [ ] Upload macOS installer
- [ ] Upload Linux DEB package
- [ ] Upload Linux RPM package
- [ ] Review release page
- [ ] Publish release (remove draft status)

### CDN Distribution
- [ ] Upload to primary CDN
- [ ] Upload to mirror servers
- [ ] Update voice catalog
- [ ] Verify download links
- [ ] Test download speeds

### Documentation Updates
- [ ] Update website download page
- [ ] Update documentation version
- [ ] Update changelog
- [ ] Update FAQ if needed
- [ ] Update screenshots if needed

## Post-Release

### Announcements
- [ ] GitHub release announcement
- [ ] Website news post
- [ ] Social media posts:
  - [ ] Twitter/X
  - [ ] Reddit
  - [ ] Discord
  - [ ] Other platforms
- [ ] Email newsletter (if applicable)
- [ ] Blog post (if applicable)

### Monitoring
- [ ] Monitor GitHub issues
- [ ] Monitor download statistics
- [ ] Monitor error reports
- [ ] Monitor user feedback
- [ ] Check CDN performance
- [ ] Verify all download links work

### Cleanup
- [ ] Merge release branch to main
- [ ] Delete release branch
- [ ] Archive old releases (if needed)
- [ ] Update project board
- [ ] Close completed milestones

## Rollback Plan

If critical issues are discovered:

1. **Immediate Actions**
   - [ ] Mark release as pre-release on GitHub
   - [ ] Add warning to download page
   - [ ] Post announcement about issue

2. **Fix or Rollback**
   - [ ] Assess severity of issue
   - [ ] Decide: hotfix or rollback
   - [ ] If hotfix: create patch release
   - [ ] If rollback: restore previous version

3. **Communication**
   - [ ] Notify users of issue
   - [ ] Provide workaround if available
   - [ ] Announce fix/rollback timeline

## Version Numbers

Follow Semantic Versioning (semver):
- **MAJOR.MINOR.PATCH** (e.g., 1.0.0)
- **MAJOR**: Breaking changes
- **MINOR**: New features (backward compatible)
- **PATCH**: Bug fixes (backward compatible)

## Release Schedule

Typical release cycle:
- **Major releases**: Every 6-12 months
- **Minor releases**: Every 1-3 months
- **Patch releases**: As needed for critical bugs

## Sign-Off

### Release Manager
- **Name**: _______________
- **Date**: _______________
- **Signature**: _______________

### QA Lead
- **Name**: _______________
- **Date**: _______________
- **Signature**: _______________

### Project Lead
- **Name**: _______________
- **Date**: _______________
- **Signature**: _______________

## Notes

Document any deviations from the checklist or special considerations:

_______________________________________________________________________________
_______________________________________________________________________________
_______________________________________________________________________________
_______________________________________________________________________________

## Release Status

- [ ] **READY** - All items complete, ready to publish
- [ ] **BLOCKED** - Issues preventing release
- [ ] **PUBLISHED** - Release is live

**Status Notes**: _______________________________________________________________

---

*Template Version: 1.0*
*Last Updated: November 10, 2025*
