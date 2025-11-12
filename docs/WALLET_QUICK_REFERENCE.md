# Wallet Integration - Quick Reference

## For Users

### Connect Wallet
1. Settings → Web3 Profile → Connect Wallet
2. Browser opens → Click "Connect MetaMask"
3. Approve in MetaMask → Done!

### Sign Messages
1. MetaMask popup appears automatically
2. Review message
3. Click "Sign"
4. Done!

### Disconnect
1. In browser page: Click "Disconnect"
2. Or close IReader app

## For Developers

### Production Mode (Default)
```bash
# Just run the app - uses MetaMask
./gradlew run
```

### Development Mode
```bash
# Use local keys for testing
export WALLET_USE_LOCAL_KEYS=true
./gradlew run
```

### Test Server
```bash
# Check if server is running
curl http://localhost:48923/health

# Open wallet page manually
open http://localhost:48923/wallet
```

### Key Files
```
domain/src/desktopMain/kotlin/ireader/domain/services/
├── BrowserWalletServer.kt           # HTTP server
├── WalletIntegrationManager.desktop.kt  # Main integration
└── DesktopWalletKeyManager.kt       # Key storage (dev mode)
```

### Environment Variables
```bash
WALLET_USE_LOCAL_KEYS=true   # Use development mode
```

## API Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/wallet` | Wallet connection page |
| POST | `/connect` | Wallet connected |
| POST | `/disconnect` | Wallet disconnected |
| POST | `/request-signature` | Request signature |
| GET | `/request-signature` | Poll for requests |
| POST | `/signature` | Send signature |
| GET | `/health` | Health check |

## Common Issues

| Issue | Solution |
|-------|----------|
| Port in use | Close other apps or change port |
| MetaMask not found | Install MetaMask extension |
| Timeout | Connect/sign faster, check MetaMask is unlocked |
| Browser doesn't open | Open `http://localhost:48923/wallet` manually |

## Security Checklist

- ✅ Private keys stay in MetaMask
- ✅ User approves every signature
- ✅ Local-only communication
- ✅ Temporary sessions
- ✅ Checksummed addresses

## Support

- 📖 Full docs: `/docs/BROWSER_WALLET_INTEGRATION.md`
- 🚀 Setup guide: `/docs/WALLET_SETUP_GUIDE.md`
- 🏗️ Implementation: `/docs/PRODUCTION_WALLET_IMPLEMENTATION.md`
