package org.ireader.infinity.core.data.network.models

import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.dnsoverhttps.DnsOverHttps
import java.net.InetAddress

/**
 * Based on https://github.com/square/okhttp/blob/ef5d0c83f7bbd3a0c0534e7ca23cbc4ee7550f3b/okhttp-dnsoverhttps/src/test/java/okhttp3/dnsoverhttps/DohProviders.java
 */

const val PREF_DOH_CLOUDFLARE = 1
const val PREF_DOH_GOOGLE = 2
const val PREF_DOH_ADGUARD = 3
const val PREF_DOH_SHECAN = 4

fun OkHttpClient.Builder.dohCloudflare() = dns(
    DnsOverHttps.Builder().client(build())
        .url("https://cloudflare-dns.com/dns-query".toHttpUrl())
        .bootstrapDnsHosts(
            InetAddress.getByName("162.159.36.1"),
            InetAddress.getByName("162.159.46.1"),
            InetAddress.getByName("1.1.1.1"),
            InetAddress.getByName("1.0.0.1"),
            InetAddress.getByName("162.159.132.53"),
            InetAddress.getByName("2606:4700:4700::1111"),
            InetAddress.getByName("2606:4700:4700::1001"),
            InetAddress.getByName("2606:4700:4700::0064"),
            InetAddress.getByName("2606:4700:4700::6400")
        )
        .build()
)

fun OkHttpClient.Builder.dohGoogle() = dns(
    DnsOverHttps.Builder().client(build())
        .url("https://dns.google/dns-query".toHttpUrl())
        .bootstrapDnsHosts(
            InetAddress.getByName("8.8.4.4"),
            InetAddress.getByName("8.8.8.8")
        )
        .build()
)

// AdGuard "Default" DNS works too but for the sake of making sure no site is blacklisted, i picked "Unfiltered"
fun OkHttpClient.Builder.dohAdGuard() = dns(
    DnsOverHttps.Builder().client(build())
        .url("https://dns-unfiltered.adguard.com/dns-query".toHttpUrl())
        .bootstrapDnsHosts(
            InetAddress.getByName("94.140.14.140"),
            InetAddress.getByName("94.140.14.141"),
            InetAddress.getByName("2a10:50c0::1:ff"),
            InetAddress.getByName("2a10:50c0::2:ff"),
        )
        .build()
)

fun OkHttpClient.Builder.dohShecan() = dns(
    DnsOverHttps.Builder().client(build())
        .url("https://dns.shecan.ir/dns-query".toHttpUrl())
        .bootstrapDnsHosts(
            InetAddress.getByName("178.22.122.100"),
            InetAddress.getByName("185.51.200.2"),
        )
        .build()
)

val dnsOverHttps = listOf<Dns>(
    Dns.Cloudflare,
    Dns.Google,
    Dns.AdGuard,
    Dns.Disable
)

sealed class Dns(val title: String, val prefCode: Int) {
    object Cloudflare : Dns("CloudFlare", PREF_DOH_CLOUDFLARE)
    object Google : Dns("Google", PREF_DOH_GOOGLE)
    object AdGuard : Dns("AdGuard", PREF_DOH_ADGUARD)
    object Disable : Dns("Disable", 0)
}