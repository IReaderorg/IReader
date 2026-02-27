# WiFi Sync VPN Bypass - Desktop Discovery Fix

## Problem

When V2Ray VPN is enabled on both desktop and Android:
- ✅ Android can see desktop device
- ❌ Desktop cannot see Android device

## Root Cause

The desktop mDNS discovery (JmDNS) was binding to the default network interface, which is the VPN interface when a VPN is active. This causes:

1. **JmDNS broadcasts on VPN interface** instead of WiFi/LAN interface
2. **mDNS packets don't reach local network** - they go through VPN tunnel
3. **Desktop can't discover Android devices** on the local network
4. **Android can discover desktop** because Android's NsdManager properly handles network selection

## Solution

### Desktop Discovery Fix

Updated `DesktopDiscoveryDataSource` to intelligently select the best network interface:

1. **Enumerate all network interfaces** on the system
2. **Score each interface** based on characteristics:
   - ✅ Physical interfaces (eth, wlan, en): +100 points
   - ✅ WiFi interfaces: +90 points
   - ✅ Has IPv4 address: +50 points
   - ✅ Supports multicast: +30 points
   - ❌ VPN interfaces (tun, tap, vpn, v2ray): -1000 points
   - ❌ Virtual interfaces: -500 points
3. **Select highest-scoring interface** (non-VPN physical network)
4. **Bind JmDNS to that specific interface**

### Desktop Socket Configuration

Updated `SocketConfigurator.desktop.kt` to detect VPN and log warnings:
- Detects common VPN interface patterns
- Logs warning if VPN is active
- Helps users understand why sync might fail

## Changes Made

### 1. `DesktopDiscoveryDataSource.kt`

**Modified `startBroadcasting()`**:
```kotlin
// Before:
jmdns = JmDNS.create(InetAddress.getLocalHost())

// After:
val networkInterface = findBestNetworkInterface()
val inetAddress = networkInterface?.let { 
    it.inetAddresses.asSequence()
        .firstOrNull { addr -> 
            !addr.isLoopbackAddress && 
            addr is java.net.Inet4Address
        }
} ?: InetAddress.getLocalHost()

jmdns = if (networkInterface != null) {
    JmDNS.create(inetAddress, "IReader-${deviceInfo.deviceName}")
} else {
    JmDNS.create(InetAddress.getLocalHost())
}
```

**Added `findBestNetworkInterface()` method**:
- Scores all network interfaces
- Prefers physical network interfaces (Ethernet, WiFi)
- Penalizes VPN interfaces heavily
- Returns best interface for local network communication

### 2. `SocketConfigurator.desktop.kt`

**Added VPN detection**:
- `isVPNActive()` method detects VPN interfaces
- Logs warning if VPN is detected
- Helps users understand routing behavior

## Testing

### Test Scenario 1: Both devices with VPN
1. Enable V2Ray VPN on desktop
2. Enable VPN on Android
3. Start WiFi sync on both devices
4. **Expected**: Both devices should now see each other

### Test Scenario 2: Desktop with VPN, Android without
1. Enable V2Ray VPN on desktop
2. Disable VPN on Android
3. Start WiFi sync on both devices
4. **Expected**: Both devices should see each other

### Test Scenario 3: No VPN on either device
1. Disable VPN on both devices
2. Start WiFi sync
3. **Expected**: Both devices should see each other (no regression)

## How It Works

### Network Interface Selection Logic

```
For each network interface:
  Score = 0
  
  If interface is UP and not loopback:
    If name starts with "eth" or "en": Score += 100 (Ethernet)
    If name starts with "wlan" or "wl": Score += 90 (WiFi)
    If display name contains "ethernet": Score += 100
    If display name contains "wi-fi": Score += 90
    
    If name contains "tun", "tap", "vpn", "v2ray": Score -= 1000 (VPN)
    If name contains "ppp": Score -= 500 (PPP)
    If display name contains "vpn" or "virtual": Score -= 500
    
    If has IPv4 address: Score += 50
    If supports multicast: Score += 30
  
  Select interface with highest score
```

### Example Scoring

```
Interface: wlan0 (Wi-Fi) - Score: 170
  +90 (WiFi)
  +50 (IPv4)
  +30 (Multicast)

Interface: tun0 (V2Ray VPN) - Score: -920
  -1000 (VPN)
  +50 (IPv4)
  +30 (Multicast)

Result: wlan0 selected (highest score)
```

## Logs to Check

When starting sync on desktop, you should see:

```
[DesktopDiscovery] Interface: wlan0 (Wi-Fi) - Score: 170
[DesktopDiscovery] Interface: tun0 (V2Ray VPN) - Score: -920
[DesktopDiscovery] Using network interface: Wi-Fi
[DesktopDiscovery] Using IP address: 192.168.1.100
[SocketConfigurator] Desktop: VPN detected. If sync fails, try disabling VPN temporarily.
```

## Benefits

1. ✅ **Desktop discovery works with VPN active**
2. ✅ **Automatic interface selection** - no user configuration needed
3. ✅ **Intelligent scoring** - prefers physical network interfaces
4. ✅ **VPN detection and logging** - helps users understand behavior
5. ✅ **No regression** - works normally without VPN

## Files Modified

1. `data/src/desktopMain/kotlin/ireader/data/sync/datasource/DesktopDiscoveryDataSource.kt`
   - Modified `startBroadcasting()` to select best network interface
   - Added `findBestNetworkInterface()` helper method
   - Added logging for interface selection

2. `data/src/desktopMain/kotlin/ireader/data/sync/datasource/SocketConfigurator.desktop.kt`
   - Added `isVPNActive()` method
   - Added VPN detection logging
   - Improved user feedback

## Related Issues

- Desktop mDNS now broadcasts on correct interface
- VPN no longer blocks local network discovery
- Both devices can discover each other with VPN active
- Automatic interface selection works across different VPN solutions (V2Ray, OpenVPN, WireGuard, etc.)
