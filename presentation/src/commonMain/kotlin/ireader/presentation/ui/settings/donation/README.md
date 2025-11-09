# Cryptocurrency Donation Feature

This module implements the cryptocurrency donation page for IReader, allowing users to support development through Bitcoin, Ethereum, and Litecoin donations.

## Files

### DonationScreen.kt
The main composable screen that displays:
- Explanation of why donations are needed
- Cryptocurrency disclaimer
- Wallet cards for Bitcoin, Ethereum, and Litecoin
- Copy-to-clipboard functionality
- QR code display dialog

### DonationConfig.kt
Configuration object containing:
- Wallet addresses for each cryptocurrency
- Disclaimer text
- **Important**: Update these addresses with actual wallet addresses before deployment

### QRCodeGenerator.kt (Common)
Expect/actual pattern for platform-specific QR code generation.

### QRCodeGenerator.android.kt
Android implementation using ZXing library to generate QR codes from wallet addresses.

### QRCodeGenerator.desktop.kt
Desktop implementation using ZXing library with Skia bitmap conversion.

## Navigation

The donation screen is accessible from:
- Settings → More → Support Development

Navigation is wired through:
- `MoreScreenSpec.kt` - Adds navigation handler
- `DonationScreenSpec.kt` - Screen specification for Voyager navigation

## Dependencies

Added to `gradle/libs.versions.toml`:
```toml
zxing = "3.5.3"
zxing-core = { module = "com.google.zxing:core", version.ref = "zxing" }
```

Added to `presentation/build.gradle.kts`:
```kotlin
implementation(libs.zxing.core)
```

## Configuration

### Before Deployment

1. **Update Wallet Addresses** in `DonationConfig.kt`:
   ```kotlin
   const val BITCOIN_ADDRESS = "your_actual_bitcoin_address"
   const val ETHEREUM_ADDRESS = "your_actual_ethereum_address"
   const val LITECOIN_ADDRESS = "your_actual_litecoin_address"
   ```

2. **Consider Remote Config**: For easier updates without app releases, consider moving wallet addresses to a remote configuration service (e.g., Firebase Remote Config, Supabase).

3. **Test QR Codes**: Verify that generated QR codes scan correctly with popular crypto wallet apps.

## Features

### Implemented
- ✅ Wallet address display for Bitcoin, Ethereum, Litecoin
- ✅ QR code generation using ZXing
- ✅ Copy-to-clipboard with toast confirmation
- ✅ Explanation section for donation purpose
- ✅ Cryptocurrency disclaimer
- ✅ QR code enlargement on tap
- ✅ Navigation from Settings
- ✅ Platform-specific implementations (Android/Desktop)

### Future Enhancements (Other Tasks)
- Wallet integration with deep links (Task 13)
- Donation triggers (Task 12)
- Fund-a-Feature progress (Task 14)
- Supporter badge system (Tasks 15-16)

## Security Considerations

- No private keys are stored or handled
- Wallet addresses are display-only
- Clear disclaimer about non-refundable donations
- No user financial data is collected

## Testing

To test the donation screen:
1. Navigate to Settings → More → Support Development
2. Verify all three wallet addresses display correctly
3. Test copy-to-clipboard functionality
4. Test QR code generation and enlargement
5. Verify disclaimer is visible and clear

## Requirements Satisfied

This implementation satisfies the following requirements from the spec:
- 18.1: Navigation from Settings
- 18.2: DonationScreen with wallet addresses
- 18.3: QR code generation
- 18.4: Copy-to-clipboard functionality
- 18.5: Explanation section
- 18.6: Wallet address configuration
- 18.7: QR code enlargement
- 18.8: Cryptocurrency disclaimer
