# Browser Wallet Integration (Desktop)

## Overview

IReader Desktop now supports **real browser wallet integration** for production use. Instead of auto-generating keys, users can connect their actual MetaMask or other Ethereum wallets.

## How It Works

1. **Local HTTP Server**: IReader starts a local server on `http://localhost:48923`
2. **Browser Connection**: Opens a web page where users connect their MetaMask wallet
3. **Real-time Communication**: Desktop app and browser communicate via HTTP polling
4. **Signature Requests**: When authentication is needed, the browser prompts MetaMask for signature
5. **Secure**: Private keys never leave the browser wallet - only signatures are shared

## User Flow

### First Time Setup

1. Launch IReader Desktop
2. Navigate to Settings → Web3 Profile
3. Click "Connect Wallet"
4. Browser opens automatically to wallet connection page
5. Click "Connect MetaMask" in browser
6. Approve connection in MetaMask popup
7. Return to IReader - wallet is now connected!

### Authentication

1. When authentication is needed, IReader sends a signature request
2. MetaMask popup appears in browser (keep the wallet page open)
3. Review and sign the message in MetaMask
4. Signature is automatically sent to IReader
5. Authentication completes!

## Requirements

- **MetaMask** browser extension installed (Chrome, Firefox, Brave, Edge)
- **Browser** must remain open while using IReader
- **Port 48923** must be available (not blocked by firewall)

## Supported Wallets

- ✅ MetaMask (primary support)
- ✅ Any wallet supporting `window.ethereum` API (Brave Wallet, etc.)

## Development Mode

For testing without a real wallet, set environment variable:

```bash
WALLET_USE_LOCAL_KEYS=true
```

This will use auto-generated local keys instead of browser wallets.

## Troubleshooting

### "Failed to start server"
- **Cause**: Port 48923 is already in use
- **Solution**: Close other applications using that port, or restart IReader

### "MetaMask not detected"
- **Cause**: MetaMask extension not installed
- **Solution**: Install MetaMask from https://metamask.io

### "Timeout waiting for wallet connection"
- **Cause**: User didn't connect wallet within 60 seconds
- **Solution**: Try again and connect faster, or check MetaMask is unlocked

### "Signature request failed"
- **Cause**: User rejected signature in MetaMask, or browser page was closed
- **Solution**: Keep browser page open and approve signature requests

### Browser page doesn't open
- **Cause**: No default browser configured
- **Solution**: Manually open `http://localhost:48923/wallet` in your browser

## Security Notes

- ✅ **Private keys never leave MetaMask** - only signatures are shared
- ✅ **Local server only** - no external connections
- ✅ **User approval required** - every signature must be approved in MetaMask
- ✅ **Session-based** - connection is temporary and can be disconnected anytime
- ⚠️ **Keep browser page open** - closing it disconnects the wallet

## Architecture

```
┌─────────────────┐         HTTP          ┌──────────────────┐
│                 │◄─────────────────────►│                  │
│  IReader Desktop│    localhost:48923    │  Browser Page    │
│                 │                        │                  │
└─────────────────┘                        └────────┬─────────┘
                                                    │
                                                    │ window.ethereum
                                                    │
                                           ┌────────▼─────────┐
                                           │                  │
                                           │    MetaMask      │
                                           │  (Private Keys)  │
                                           │                  │
                                           └──────────────────┘
```

## API Endpoints

The local server exposes these endpoints:

- `GET /wallet` - Wallet connection page (HTML)
- `POST /connect` - Wallet connection notification
- `POST /disconnect` - Wallet disconnection
- `POST /request-signature` - Request signature from browser
- `GET /request-signature` - Browser polls for pending requests
- `POST /signature` - Browser sends completed signature
- `GET /health` - Health check

## Future Enhancements

- [ ] WebSocket support for real-time communication (no polling)
- [ ] WalletConnect support for mobile wallet scanning
- [ ] Multi-wallet support (switch between wallets)
- [ ] Transaction signing (not just messages)
- [ ] Network switching support
- [ ] Persistent sessions across app restarts
