package ireader.presentation.core.ui

import androidx.compose.runtime.Composable

/**
 * Whether the given base URL points at a local (LAN) address that needs the
 * Android ACCESS_LOCAL_NETWORK runtime permission (Android 17+ / API 37+).
 *
 * Detects private IPv4 ranges (10/8, 172.16/12, 192.168/16), loopback, link-local
 * (169.254/16), IPv6 unique-local (fc00::/7) and loopback (::1). Hostnames such
 * as `localhost` or `*.local` are treated as local as well. Public hostnames /
 * IPs return false.
 *
 * Platform-agnostic: on Desktop/iOS this is always effectively fine, but the
 * detection is shared so UI can drive the Android permission request uniformly.
 */
fun isLocalNetworkUrl(url: String): Boolean {
    val host = extractHost(url)?.lowercase()?.trimEnd('.') ?: return false
    if (host.isEmpty()) return false

    if (host == "localhost" || host.endsWith(".local")) return true
    if (host == "::1" || host == "[::1]") return true
    if (host.startsWith("fc") || host.startsWith("fd")) return true // IPv6 ULA fc00::/7

    val parts = host.split(".")
    if (parts.size != 4) return false // not an IPv4 literal (e.g. public hostname)
    val octets = parts.mapNotNull { it.toIntOrNull() }
    if (octets.size != 4) return false
    val (a, b, _, _) = octets
    return when {
        a == 10 -> true                       // 10.0.0.0/8
        a == 127 -> true                      // loopback
        a == 172 && b in 16..31 -> true       // 172.16.0.0/12
        a == 192 && b == 168 -> true          // 192.168.0.0/16
        a == 169 && b == 254 -> true          // 169.254.0.0/16 link-local
        else -> false
    }
}

private fun extractHost(url: String): String? {
    val trimmed = url.trim()
    if (trimmed.isEmpty()) return null
    // Strip scheme
    val afterScheme = trimmed.substringAfter("://", missingDelimiterValue = trimmed)
    // Strip port, path, query, userinfo
    val hostPort = afterScheme.substringBefore('/').substringBefore('?').substringBefore('#')
    val host = hostPort.substringAfterLast('@')
    return when {
        host.startsWith("[") -> host.substringBefore("]").removePrefix("[") // IPv6 literal
        else -> host.substringBefore(':')
    }.ifBlank { null }
}

/**
 * Rationale text shown before requesting ACCESS_LOCAL_NETWORK (issue #295).
 * Keeps the permission request self-explanatory in-app.
 */
const val LOCAL_NETWORK_PERMISSION_RATIONALE =
    "Allow local network access so IReader can reach your local AI translation server (e.g. http://192.168.x.x). This uses the Android 'Nearby devices' permission."

/**
 * Requests ACCESS_LOCAL_NETWORK (Nearby Devices) on Android 17+ when needed.
 *
 * Android only. On other platforms this is a no-op (returns true).
 * When [url] is not a local URL, or the permission is already granted, or the
 * device runs Android < 17, no dialog is shown and true is returned.
 *
 * @param url the endpoint URL about to be used (e.g. configured OpenAI base URL).
 * @param onResult called with true when the permission is (or becomes) granted,
 *   false when denied. Always invoked (on the main thread for Android).
 */
@Composable
expect fun rememberLocalNetworkPermissionRequest(url: String): (onResult: (Boolean) -> Unit) -> Unit
