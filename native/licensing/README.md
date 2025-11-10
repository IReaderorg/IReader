# Licensing and Compliance

This directory contains licensing documentation and compliance verification tools for IReader.

## Files

### License Documentation

- **`ATTRIBUTION.md`** - Attribution notices for all third-party components
- **`THIRD_PARTY_LICENSES.txt`** - Full text of all third-party licenses
- **`voice_license_template.json`** - Template for documenting voice model licenses

### Compliance Tools

- **`verify_licenses.sh`** - Script to verify licensing compliance
- **`license_compliance_report.txt`** - Generated compliance report (created by verify script)

## Usage

### Verify License Compliance

Run the verification script before each release:

```bash
cd native/licensing
./verify_licenses.sh
```

This will:
1. Check for required license files
2. Verify native library licenses
3. Check Kotlin/JVM dependencies
4. Verify voice model licenses
5. Check GPL compliance
6. Verify installer packages include licenses
7. Generate a compliance report

### Add New Voice Model

When adding a new voice model:

1. Copy `voice_license_template.json` to a new file
2. Fill in all license information
3. Verify the license permits commercial use and redistribution
4. Add license information to the voice catalog
5. Update `THIRD_PARTY_LICENSES.txt` if needed

Example:
```bash
cp voice_license_template.json voices/en-us-amy-low-license.json
# Edit the file with voice-specific information
```

### Update Third-Party Licenses

When adding a new dependency:

1. Identify the license type
2. Obtain the full license text
3. Add to `THIRD_PARTY_LICENSES.txt`
4. Update `ATTRIBUTION.md`
5. Run `verify_licenses.sh` to confirm

## License Types

### Permissive Licenses (Compatible)

These licenses allow commercial use and redistribution:

- **MIT License** - Very permissive, requires attribution
- **Apache License 2.0** - Permissive, includes patent grant
- **BSD Licenses** - Permissive, various versions
- **CC-BY** - Creative Commons with attribution

### Copyleft Licenses (Special Handling)

These licenses require source code availability:

- **GPL v3** - Strong copyleft, requires source distribution
- **LGPL** - Lesser GPL, allows dynamic linking
- **MPL 2.0** - Weak copyleft, file-level

### Incompatible Licenses

Avoid these licenses:

- **CC-BY-NC** - Non-commercial restriction
- **Proprietary** - No redistribution rights
- **GPL v2 only** - Incompatible with GPL v3

## GPL Compliance (espeak-ng)

espeak-ng is licensed under GPL v3. To comply:

1. **Include License Text**: ✓ In THIRD_PARTY_LICENSES.txt
2. **Provide Source Code**: Make espeak-ng source available
3. **Document Modifications**: If we modify espeak-ng
4. **Preserve Notices**: Keep all copyright notices

### Source Code Availability

For GPL compliance, we provide:

1. Link to original espeak-ng repository
2. Version information
3. Build instructions
4. Any modifications (if applicable)

## Voice Model Licenses

Voice models have varying licenses. Common types:

### Piper Voice Models

Most Piper voices use:
- **MIT License** - Fully permissive
- **CC-BY-SA-4.0** - Share-alike requirement
- **CC-BY-4.0** - Attribution only

### Verification Checklist

For each voice model, verify:

- [ ] License type identified
- [ ] Commercial use permitted
- [ ] Redistribution permitted
- [ ] Attribution requirements documented
- [ ] Training data license compatible
- [ ] License information in catalog

## Installer Compliance

Ensure all installers include:

### Windows (MSI)
- [ ] LICENSE file
- [ ] THIRD_PARTY_LICENSES.txt
- [ ] ATTRIBUTION.md
- [ ] License acceptance dialog

### macOS (DMG)
- [ ] LICENSE file in app bundle
- [ ] THIRD_PARTY_LICENSES.txt in app bundle
- [ ] About dialog with attributions

### Linux (DEB/RPM)
- [ ] LICENSE in /usr/share/doc/ireader/
- [ ] THIRD_PARTY_LICENSES.txt in /usr/share/doc/ireader/
- [ ] Copyright file with summary

## Compliance Checklist

Before each release:

- [ ] Run `verify_licenses.sh`
- [ ] Review compliance report
- [ ] Verify all license files present
- [ ] Check installer packages
- [ ] Verify voice model licenses
- [ ] Update THIRD_PARTY_LICENSES.txt if needed
- [ ] Test About dialog shows attributions
- [ ] Verify GPL source code availability
- [ ] Check copyright notices preserved
- [ ] Document any new dependencies

## Common Issues

### Missing License Files

**Problem**: License files not included in distribution

**Solution**:
```bash
# Verify files exist
ls -la ../../LICENSE
ls -la ../../THIRD_PARTY_LICENSES.txt

# Check installer configurations
grep -r "LICENSE" ../installers/
```

### GPL Compliance

**Problem**: espeak-ng source code not available

**Solution**:
1. Link to official espeak-ng repository
2. Document version used
3. Provide build instructions
4. Note any modifications

### Voice Model Licenses

**Problem**: Voice license unclear or restrictive

**Solution**:
1. Contact voice model author
2. Review training data license
3. Document restrictions
4. Consider alternative voices

## Resources

### License Information

- SPDX License List: https://spdx.org/licenses/
- Choose a License: https://choosealicense.com/
- TLDRLegal: https://tldrlegal.com/

### GPL Compliance

- GNU GPL FAQ: https://www.gnu.org/licenses/gpl-faq.html
- GPL Compliance Guide: https://www.softwarefreedom.org/resources/

### Creative Commons

- CC License Chooser: https://creativecommons.org/choose/
- CC FAQ: https://creativecommons.org/faq/

## Contact

For licensing questions or compliance issues:

- **Email**: legal@ireader.org
- **GitHub**: https://github.com/yourusername/ireader/issues
- **Documentation**: https://ireader.org/docs/licensing

## Updates

This documentation should be reviewed and updated:

- Before each release
- When adding new dependencies
- When adding new voice models
- When license terms change
- Annually for general review

---

*Last Updated: November 10, 2025*
