# Release Management

This directory contains tools and documentation for creating and publishing IReader releases.

## Files

### Scripts

- **`create_release.sh`** - Creates release package with all artifacts
- **`publish_release.sh`** - Publishes release to GitHub and CDN

### Documentation

- **`RELEASE_CHECKLIST.md`** - Complete checklist for releases
- **`README.md`** - This file

## Quick Start

### Create a Release

1. **Prepare the release:**
   ```bash
   # Update version numbers in code
   # Update changelog
   # Commit all changes
   ```

2. **Run QA tests:**
   ```bash
   cd ../qa
   ./run_full_test_suite.sh
   ./regression_test_suite.sh
   ```

3. **Verify licenses:**
   ```bash
   cd ../licensing
   ./verify_licenses.sh
   ```

4. **Create release package:**
   ```bash
   cd ../release
   ./create_release.sh 1.0.0
   ```

5. **Build platform installers:**
   ```bash
   # Windows
   powershell -File ../installers/windows/build_msi.ps1
   
   # macOS
   ../installers/macos/create_dmg.sh 1.0.0
   
   # Linux
   ../installers/linux/build_deb.sh 1.0.0
   ../installers/linux/build_rpm.sh 1.0.0
   ```

6. **Test installers:**
   ```bash
   ../installers/test_installers.sh
   ```

7. **Publish release:**
   ```bash
   ./publish_release.sh 1.0.0
   ```

## Release Process

### 1. Pre-Release Phase

**Goal**: Ensure code is ready for release

**Tasks**:
- Complete all planned features
- Fix all critical bugs
- Update documentation
- Run full test suite
- Verify license compliance

**Duration**: 1-2 weeks before release

### 2. Release Candidate Phase

**Goal**: Create and test release candidate

**Tasks**:
- Create release branch
- Build release candidate
- Perform thorough testing
- Fix any issues found
- Update release notes

**Duration**: 3-7 days before release

### 3. Release Creation Phase

**Goal**: Create final release artifacts

**Tasks**:
- Run `create_release.sh`
- Build all platform installers
- Calculate checksums
- Generate release notes
- Create version tags

**Duration**: 1 day

### 4. Publication Phase

**Goal**: Publish release to users

**Tasks**:
- Create GitHub release
- Upload artifacts
- Update CDN
- Update documentation
- Announce release

**Duration**: 1 day

### 5. Post-Release Phase

**Goal**: Monitor and support release

**Tasks**:
- Monitor for issues
- Respond to user feedback
- Prepare hotfixes if needed
- Update metrics

**Duration**: Ongoing

## Release Artifacts

A complete release includes:

### Source Code
- Source archive (`.tar.gz`)
- Git tag (`v{VERSION}`)

### Installers
- Windows MSI (`.msi`)
- macOS DMG (`.dmg`)
- Linux DEB (`.deb`)
- Linux RPM (`.rpm`)

### Documentation
- Release notes
- User guide
- Developer guide
- API documentation

### Verification
- Checksums file (SHA256)
- Signatures (if applicable)

## Version Numbering

IReader follows Semantic Versioning (semver):

```
MAJOR.MINOR.PATCH
```

- **MAJOR**: Breaking changes (e.g., 1.0.0 → 2.0.0)
- **MINOR**: New features, backward compatible (e.g., 1.0.0 → 1.1.0)
- **PATCH**: Bug fixes, backward compatible (e.g., 1.0.0 → 1.0.1)

### Examples

- `1.0.0` - Initial release
- `1.1.0` - Added new voice models
- `1.1.1` - Fixed synthesis bug
- `2.0.0` - Major UI redesign (breaking changes)

## Release Types

### Major Release

**When**: Significant changes, breaking API changes

**Includes**:
- Major new features
- Breaking changes
- API changes
- UI redesign

**Testing**: Extensive, all platforms

**Timeline**: 6-12 months

### Minor Release

**When**: New features, no breaking changes

**Includes**:
- New features
- Improvements
- Non-breaking changes

**Testing**: Full test suite

**Timeline**: 1-3 months

### Patch Release

**When**: Bug fixes only

**Includes**:
- Bug fixes
- Security fixes
- Performance improvements

**Testing**: Regression tests

**Timeline**: As needed

### Hotfix Release

**When**: Critical bugs in production

**Includes**:
- Critical bug fix
- Security fix

**Testing**: Targeted testing

**Timeline**: Immediate (hours to days)

## Release Channels

### Stable

- Production-ready releases
- Thoroughly tested
- Recommended for all users
- Published to main download page

### Beta

- Feature-complete, testing phase
- May have minor bugs
- For early adopters
- Published as pre-release

### Alpha

- Development builds
- Unstable, for testing only
- For developers and testers
- Not publicly released

## Scripts Usage

### create_release.sh

Creates a complete release package.

**Usage**:
```bash
./create_release.sh [VERSION] [OUTPUT_DIR]
```

**Arguments**:
- `VERSION` - Release version (default: 1.0.0)
- `OUTPUT_DIR` - Output directory (default: ../../build/release)

**Example**:
```bash
./create_release.sh 1.2.0 /tmp/release
```

**Output**:
- `ireader-{VERSION}.tar.gz` - Source archive
- `ireader-{VERSION}/` - Package directory
- `ireader-{VERSION}-checksums.txt` - Checksums
- `RELEASE_SUMMARY.txt` - Release summary

### publish_release.sh

Publishes release to GitHub and optionally to CDN.

**Usage**:
```bash
./publish_release.sh [VERSION] [RELEASE_DIR]
```

**Arguments**:
- `VERSION` - Release version (default: 1.0.0)
- `RELEASE_DIR` - Release directory (default: ../../build/release)

**Environment Variables**:
- `CDN_HOST` - CDN server hostname (e.g., user@cdn.ireader.org)
- `CDN_PATH` - CDN path (e.g., /var/www/cdn)

**Example**:
```bash
export CDN_HOST=user@cdn.ireader.org
export CDN_PATH=/var/www/cdn
./publish_release.sh 1.2.0
```

**Requirements**:
- GitHub CLI (`gh`) installed and authenticated
- SSH access to CDN (if uploading to CDN)

## Checklist

Use `RELEASE_CHECKLIST.md` for each release:

1. Print or copy the checklist
2. Complete each item
3. Get sign-offs from required roles
4. Archive completed checklist

## Troubleshooting

### Build Fails

**Problem**: `create_release.sh` fails during build

**Solution**:
```bash
# Clean build
cd ../..
./gradlew clean build --stacktrace

# Check for errors
./gradlew build --info
```

### Missing Artifacts

**Problem**: Some files missing from release package

**Solution**:
```bash
# Verify build output
ls -la build/libs/
ls -la domain/src/desktopMain/resources/native/

# Rebuild if needed
./gradlew clean build
```

### GitHub Release Fails

**Problem**: `publish_release.sh` fails to create release

**Solution**:
```bash
# Check authentication
gh auth status

# Re-authenticate if needed
gh auth login

# Check if release already exists
gh release view v1.0.0

# Delete if needed
gh release delete v1.0.0 -y
```

### CDN Upload Fails

**Problem**: Files don't upload to CDN

**Solution**:
```bash
# Test SSH connection
ssh $CDN_HOST "echo 'Connection OK'"

# Check permissions
ssh $CDN_HOST "ls -la $CDN_PATH"

# Manual upload
scp file.tar.gz $CDN_HOST:$CDN_PATH/
```

## Best Practices

### Before Release

1. **Test thoroughly** - Run all test suites
2. **Update docs** - Ensure documentation is current
3. **Review changes** - Check all commits since last release
4. **Verify licenses** - Run license verification
5. **Get approvals** - Obtain necessary sign-offs

### During Release

1. **Follow checklist** - Don't skip steps
2. **Test installers** - Verify on clean systems
3. **Verify checksums** - Ensure file integrity
4. **Double-check versions** - Confirm version numbers
5. **Keep backups** - Save all artifacts

### After Release

1. **Monitor closely** - Watch for issues
2. **Respond quickly** - Address problems promptly
3. **Gather feedback** - Listen to users
4. **Document issues** - Track problems for next release
5. **Plan improvements** - Learn from the process

## Automation

Consider automating:

- Version number updates
- Changelog generation
- Build process
- Testing
- Release creation
- Publication

Use CI/CD tools like:
- GitHub Actions
- GitLab CI
- Jenkins
- CircleCI

## Support

For questions about releases:

- **Email**: release@ireader.org
- **GitHub**: https://github.com/yourusername/ireader/issues
- **Documentation**: https://ireader.org/docs/releases

---

*Last Updated: November 10, 2025*
