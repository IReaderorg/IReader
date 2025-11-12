# Wallet Setup Guide

## For End Users

### Quick Start (5 minutes)

#### 1. Install MetaMask

1. Open your browser (Chrome, Firefox, Brave, or Edge)
2. Go to https://metamask.io
3. Click "Download" and install the browser extension
4. Follow MetaMask setup wizard to create or import a wallet
5. **Important**: Write down your seed phrase and keep it safe!

#### 2. Connect to IReader

1. Launch IReader Desktop
2. Go to **Settings** → **Web3 Profile**
3. Click **"Connect Wallet"**
4. Your browser will open automatically
5. Click **"Connect MetaMask"** on the page
6. Approve the connection in MetaMask popup
7. Done! Your wallet is connected

#### 3. Authenticate

1. When prompted, check your browser
2. MetaMask will show a signature request
3. Review the message (it should say "Sign this message to authenticate with IReader")
4. Click **"Sign"** in MetaMask
5. Return to IReader - you're authenticated!

### Tips

- ✅ **Keep the browser page open** while using IReader
- ✅ **Bookmark the page** for easy access: `http://localhost:48923/wallet`
- ✅ **Use a dedicated browser profile** if you want to keep IReader separate
- ⚠️ **Never share your seed phrase** with anyone
- ⚠️ **Only sign messages you understand** - IReader will only ask you to sign authentication messages

## For Developers

### Development Mode

To test without a real wallet:

```bash
# Set environment variable before launching
export WALLET_USE_LOCAL_KEYS=true  # Linux/Mac
set WALLET_USE_LOCAL_KEYS=true     # Windows CMD
$env:WALLET_USE_LOCAL_KEYS="true"  # Windows PowerShell

# Then launch IReader
./gradlew run
```

This will use auto-generated local keys for testing.

### Production Mode (Default)

No environment variables needed. IReader will automatically:
1. Start local HTTP server on port 48923
2. Open browser for wallet connection
3. Use real MetaMask signatures

### Testing the Integration

1. **Test wallet connection**:
   ```bash
   curl http://localhost:48923/health
   # Should return: {"status":"ok"}
   ```

2. **Manually open wallet page**:
   ```
   http://localhost:48923/wallet
   ```

3. **Check server logs**:
   Look for these messages in console:
   ```
   ✅ BrowserWalletServer: Started on http://localhost:48923
   ✅ BrowserWalletServer: Wallet connected - 0x...
   ✅ Signature received from browser wallet
   ```

### Customization

#### Change Server Port

Edit `BrowserWalletServer.kt`:
```kotlin
class BrowserWalletServer(
    private val port: Int = 48923  // Change this
)
```

#### Customize Wallet Page

The HTML page is embedded in `BrowserWalletServer.kt` in the `createWalletConnectionPage()` function. You can:
- Change colors/styling
- Add your logo
- Modify instructions
- Add analytics

#### Add More Wallets

The current implementation supports any wallet with `window.ethereum` API. To add specific wallet detection:

```javascript
// In the HTML page
if (window.ethereum.isMetaMask) {
    console.log('MetaMask detected');
} else if (window.ethereum.isCoinbaseWallet) {
    console.log('Coinbase Wallet detected');
}
```

## Troubleshooting

### Common Issues

**Q: Browser doesn't open automatically**
- A: Manually open `http://localhost:48923/wallet` in your browser

**Q: "Port already in use" error**
- A: Another application is using port 48923. Close it or change the port in code

**Q: MetaMask doesn't show signature request**
- A: Make sure the browser page is open and MetaMask is unlocked

**Q: Connection lost after closing browser**
- A: This is expected. Keep the browser page open while using IReader

**Q: Want to use a different wallet address**
- A: Disconnect current wallet, switch accounts in MetaMask, then reconnect

### Advanced Debugging

Enable verbose logging:
```kotlin
// In BrowserWalletServer.kt, add more println statements
println("🔍 Debug: Request body = $body")
println("🔍 Debug: Parsed params = $params")
```

Check network traffic:
```bash
# Monitor HTTP requests
curl -v http://localhost:48923/health
```

## Security Best Practices

### For Users
1. ✅ Only connect wallets you control
2. ✅ Review every signature request carefully
3. ✅ Use a separate wallet for IReader (not your main holdings)
4. ✅ Keep MetaMask updated
5. ⚠️ Never enter your seed phrase in IReader or any website

### For Developers
1. ✅ Always use HTTPS in production (if hosting remotely)
2. ✅ Validate all signatures on the backend
3. ✅ Implement rate limiting for signature requests
4. ✅ Add CORS restrictions if needed
5. ✅ Log security events
6. ⚠️ Never log private keys or signatures in production

## Next Steps

- [ ] Set up your MetaMask wallet
- [ ] Connect to IReader
- [ ] Test authentication
- [ ] Explore Web3 features
- [ ] Join the community!

## Support

- 📖 Documentation: `/docs`
- 🐛 Issues: GitHub Issues
- 💬 Community: Discord/Telegram
- 📧 Email: support@ireader.app
