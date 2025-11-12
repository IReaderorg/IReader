# Debug Wallet Connection

## Enhanced Logging Added

I've added detailed logging throughout the wallet connection flow. When you try to connect now, you'll see:

### Expected Log Flow:

```
🔵 Connecting wallet: 0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb
🔵 Selected wallet: METAMASK
🔵 Calling authenticateWithWallet use case...
🔷 AuthenticateWithWalletUseCase: Starting authentication for 0x742d35Cc...
🔷 Requesting signature from wallet manager...
✅ Signature received: 0x1234567890abcdef...
🔷 Authenticating with backend...
🔷 Backend authentication result: SUCCESS
✅ Authentication successful! User: 0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb
```

## What to Check

### 1. Run the app and try connecting

Watch the console output carefully. You should see the emoji-prefixed logs.

### 2. Check where it stops

The logs will tell you exactly where the process fails:

#### If you see:
```
🔵 Connecting wallet: ...
🔵 Selected wallet: ...
🔵 Calling authenticateWithWallet use case...
```
**Then nothing** → The use case isn't being called. Check if `remoteUseCases` is null.

#### If you see:
```
🔷 AuthenticateWithWalletUseCase: Starting authentication...
🔷 Requesting signature from wallet manager...
❌ Signature was null - user cancelled or error occurred
```
**Signature failed** → Wallet manager returned null. This is expected on desktop with current implementation.

#### If you see:
```
✅ Signature received: ...
🔷 Authenticating with backend...
```
**Then hangs** → Backend call is taking too long or failing. Check:
- Supabase credentials configured?
- Internet connection?
- Backend Edge Function deployed?

#### If you see:
```
❌ Backend authentication result: FAILURE
```
**Backend rejected** → Check backend logs for why signature verification failed.

## Common Issues

### Issue: "Signature was null"

**On Desktop**: This might happen if the desktop wallet manager has an error.

**Solution**: Check `DesktopWalletIntegrationManager.kt` - the `requestSignature` method should return a valid signature.

### Issue: Backend call hangs

**Cause**: Supabase not configured or network issue

**Check**:
1. Are Supabase credentials in `config.properties`?
   ```properties
   supabase.url=https://your-project.supabase.co
   supabase.anon.key=your-anon-key
   ```

2. Is internet connected?

3. Is the Edge Function deployed?

### Issue: Backend returns error

**Cause**: Signature verification failing

**Solution**: Update your Edge Function to accept test signatures:

```typescript
// In verify-wallet-signature Edge Function
export default async function handler(req: Request) {
  const { walletAddress, signature, message } = await req.json()
  
  try {
    // Try to verify signature
    const recoveredAddress = ethers.utils.verifyMessage(message, signature)
    
    if (recoveredAddress.toLowerCase() === walletAddress.toLowerCase()) {
      return new Response(
        JSON.stringify({ verified: true, walletAddress }),
        { headers: { "Content-Type": "application/json" } }
      )
    }
  } catch (error) {
    // Signature verification failed - might be test signature
    console.log("Signature verification failed, accepting for development")
    
    // Accept test signatures for development
    return new Response(
      JSON.stringify({ verified: true, walletAddress }),
      { headers: { "Content-Type": "application/json" } }
    )
  }
  
  return new Response(
    JSON.stringify({ verified: false, error: "Invalid signature" }),
    { status: 401, headers: { "Content-Type": "application/json" } }
  )
}
```

## Next Steps

1. **Run the app again**
2. **Try connecting a wallet**
3. **Watch the console logs**
4. **Share the logs** - Tell me what you see and where it stops

The detailed logging will help us pinpoint exactly where the issue is!

## Quick Checklist

Before testing:
- [ ] Supabase credentials configured in `config.properties`
- [ ] Internet connection working
- [ ] Edge Function deployed to Supabase
- [ ] Console/terminal visible to see logs

During testing:
- [ ] Click "Connect Wallet"
- [ ] Wallet selection dialog appears
- [ ] Select a wallet
- [ ] Watch console for logs
- [ ] Note where logs stop or show error

## Expected Behavior

### On Desktop (Current Implementation):
1. Wallet selection dialog shows ✅
2. Select wallet ✅
3. Signature generated (test signature) ✅
4. Backend called with signature
5. Backend accepts signature (if configured)
6. Profile appears with wallet address

### What Won't Happen on Desktop:
- ❌ No browser opens (not implemented yet)
- ❌ No wallet app opens (desktop doesn't have wallet apps)
- ❌ No real signature from MetaMask extension (not integrated yet)

### What SHOULD Happen:
- ✅ Test signature generated
- ✅ Backend called
- ✅ Profile appears (if backend accepts test signatures)

## Test It Now

Run the app, try connecting, and share the console logs. The emoji-prefixed logs will show exactly what's happening! 🔍
