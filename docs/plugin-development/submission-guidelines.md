# Plugin Submission Guidelines

Guidelines for submitting plugins to the IReader Plugin Marketplace.

## Before You Submit

### Requirements Checklist

- [ ] Plugin is fully functional and tested
- [ ] Manifest is complete and valid
- [ ] All required permissions are declared
- [ ] Plugin follows [best practices](best-practices.md)
- [ ] Code is well-documented
- [ ] No hardcoded secrets or API keys
- [ ] Plugin works on all declared platforms
- [ ] Icon and screenshots are provided
- [ ] Description is clear and accurate
- [ ] Pricing is set (if applicable)
- [ ] You have tested the plugin thoroughly

### Quality Standards

Your plugin must meet these standards:

1. **Functionality**: Works as described without crashes
2. **Performance**: Doesn't significantly slow down the app
3. **Security**: Follows security best practices
4. **User Experience**: Intuitive and well-designed
5. **Documentation**: Clear usage instructions
6. **Code Quality**: Clean, maintainable code

## Submission Process

### 1. Create Developer Account

1. Go to https://plugins.ireader.app/developer
2. Sign up with email or GitHub
3. Complete developer profile
4. Agree to Developer Terms

### 2. Prepare Plugin Package

Ensure your `.iplugin` file is ready:

```bash
./gradlew validatePlugin --plugin=MyPlugin
./gradlew packagePlugin --plugin=MyPlugin
```

### 3. Create Marketplace Listing

Log in to the developer portal and create a new plugin listing:

#### Basic Information

- **Plugin Name**: Clear, descriptive name (max 50 chars)
- **Short Description**: One-line summary (max 100 chars)
- **Full Description**: Detailed description (max 2000 chars)
- **Category**: Choose appropriate category
- **Tags**: Add relevant tags (max 5)

#### Media Assets

- **Icon**: 512x512px PNG with transparency
- **Screenshots**: 1080x1920px PNG (3-5 images)
- **Feature Graphic**: 1024x500px PNG (optional)
- **Video**: YouTube/Vimeo link (optional)

#### Technical Details

- **Plugin Package**: Upload `.iplugin` file
- **Version**: Automatically extracted from manifest
- **Supported Platforms**: Automatically extracted
- **Required Permissions**: Automatically extracted
- **Min IReader Version**: Automatically extracted

#### Pricing (if applicable)

- **Pricing Model**: Free, Premium, or Freemium
- **Price**: Set price in USD (converted to local currencies)
- **Trial Period**: Days of free trial (optional)
- **In-Plugin Purchases**: Define purchasable features

#### Support Information

- **Website**: Your plugin's website
- **Support Email**: Contact email for support
- **Privacy Policy**: Link to privacy policy (required if collecting data)
- **Source Code**: Link to repository (optional, recommended for open source)

### 4. Submit for Review

Click "Submit for Review" when ready.

## Review Process

### What We Check

1. **Functionality**: Plugin works as described
2. **Security**: No malicious code or vulnerabilities
3. **Performance**: Acceptable resource usage
4. **User Experience**: Intuitive and polished
5. **Content**: Appropriate and legal content
6. **Compliance**: Follows guidelines and policies

### Review Timeline

- **Initial Review**: 3-5 business days
- **Re-review** (after changes): 1-2 business days
- **Expedited Review**: Available for critical bug fixes

### Possible Outcomes

#### Approved ✅
- Plugin is published to marketplace
- Users can discover and install it
- You receive email confirmation

#### Rejected ❌
- Plugin doesn't meet guidelines
- You receive detailed feedback
- Fix issues and resubmit

#### Needs Changes ⚠️
- Minor issues need addressing
- Make requested changes
- Resubmit for review

## Content Guidelines

### Allowed Content

- Themes and visual customizations
- Translation services
- Text-to-speech engines
- Reading enhancement features
- Library management tools
- Note-taking and annotation tools
- Reading statistics and analytics
- Social features (sharing, recommendations)
- Accessibility improvements

### Prohibited Content

- Malware, viruses, or malicious code
- Piracy or copyright infringement tools
- Spam or misleading content
- Adult or inappropriate content
- Privacy violations or data theft
- Cryptocurrency mining
- Excessive advertising
- Functionality that breaks IReader's core features

## Technical Requirements

### Code Requirements

1. **No Obfuscation**: Code must not be intentionally obfuscated
2. **No Reflection Abuse**: Limited use of reflection
3. **No Native Code**: Unless approved (security review required)
4. **Proper Error Handling**: All errors handled gracefully
5. **Resource Cleanup**: Proper cleanup in `cleanup()` method

### Performance Requirements

1. **Startup Time**: Plugin initialization < 500ms
2. **Memory Usage**: < 50MB for typical usage
3. **Network Usage**: Reasonable bandwidth consumption
4. **Battery Impact**: Minimal battery drain
5. **Storage**: Reasonable storage usage

### Security Requirements

1. **HTTPS Only**: All network requests use HTTPS
2. **Input Validation**: All user inputs validated
3. **Permission Justification**: Clear reason for each permission
4. **Data Encryption**: Sensitive data encrypted
5. **No Hardcoded Secrets**: API keys user-provided or securely stored

## Monetization Guidelines

### Pricing

- **Free Plugins**: No restrictions
- **Premium Plugins**: $0.99 - $49.99 USD
- **In-Plugin Purchases**: $0.99 - $19.99 USD per item
- **Subscriptions**: Not currently supported

### Trial Periods

- Recommended: 7 days
- Maximum: 30 days
- Must provide full functionality during trial

### Refunds

- Users can request refunds within 48 hours
- Refunds processed automatically
- You receive payment minus refunds

### Revenue Share

- **70% to developer**
- **30% to IReader** (covers hosting, payment processing, support)

## Updates

### Updating Your Plugin

1. Increment version number
2. Update changelog
3. Upload new `.iplugin` file
4. Submit for review (expedited for bug fixes)

### Update Guidelines

- **Bug Fixes**: Expedited review
- **New Features**: Standard review
- **Breaking Changes**: Require major version bump
- **Security Fixes**: Highest priority review

### Deprecation

If deprecating features:

1. Announce in changelog
2. Provide migration guide
3. Give users 30 days notice
4. Maintain backward compatibility when possible

## Marketing Your Plugin

### Marketplace Optimization

1. **Clear Name**: Descriptive and searchable
2. **Good Description**: Highlight key features
3. **Quality Screenshots**: Show plugin in action
4. **Regular Updates**: Keep plugin current
5. **Respond to Reviews**: Engage with users

### Promotion

- Share on social media
- Write blog posts
- Create tutorial videos
- Engage in community forums
- Offer launch discounts

## Support and Maintenance

### User Support

You are responsible for:

- Responding to user inquiries
- Fixing reported bugs
- Providing documentation
- Maintaining compatibility with IReader updates

### Response Times

- **Critical Bugs**: 24 hours
- **General Issues**: 3 business days
- **Feature Requests**: Best effort

### Maintenance

- Update for new IReader versions within 30 days
- Fix critical bugs within 7 days
- Maintain plugin for at least 1 year after publication

## Removal Policy

Plugins may be removed for:

- Violation of guidelines
- Security vulnerabilities (if not fixed promptly)
- Abandonment (no updates for 1 year)
- User complaints (if not addressed)
- Legal issues

You will receive warning before removal (except for security issues).

## Appeals

If your plugin is rejected:

1. Review feedback carefully
2. Make necessary changes
3. Resubmit with explanation of changes
4. Contact developer support if you disagree with decision

## Developer Support

### Resources

- **Documentation**: https://docs.ireader.app/plugins
- **Developer Forum**: https://forum.ireader.app/developers
- **Email Support**: plugins@ireader.app
- **Discord**: https://discord.gg/ireader

### Getting Help

1. Check documentation first
2. Search forum for similar issues
3. Ask in developer Discord
4. Email support for account/submission issues

## Legal

### Developer Agreement

By submitting a plugin, you agree to:

- IReader Developer Terms of Service
- Plugin Marketplace Guidelines
- Privacy Policy
- Revenue Share Agreement

### Intellectual Property

- You retain ownership of your plugin
- You grant IReader license to distribute
- You must have rights to all included content
- Respect third-party licenses and trademarks

### Liability

- You are responsible for your plugin's functionality
- You must handle user data responsibly
- You must comply with applicable laws
- IReader is not liable for plugin issues

## Checklist Before Submission

- [ ] Plugin tested on all target platforms
- [ ] All features work correctly
- [ ] No crashes or major bugs
- [ ] Performance is acceptable
- [ ] Security best practices followed
- [ ] Manifest is complete and accurate
- [ ] Icon and screenshots provided
- [ ] Description is clear and accurate
- [ ] Pricing set correctly (if applicable)
- [ ] Support information provided
- [ ] Privacy policy provided (if needed)
- [ ] Code is clean and documented
- [ ] Changelog is up to date
- [ ] You've read and understood these guidelines

## After Approval

Once approved:

1. **Monitor Analytics**: Track downloads and usage
2. **Read Reviews**: Respond to user feedback
3. **Fix Bugs**: Address issues promptly
4. **Add Features**: Improve based on feedback
5. **Promote**: Market your plugin
6. **Engage Community**: Build user base

Good luck with your plugin submission! 🚀
