package com.ner.landslide.util

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class ResolvedLocation(
    val locality: String,
    val district: String,
    val state: String,
    val formattedName: String
) {
    val formattedHeadline: String get() = formattedName
    val area: String get() = locality
}

@Singleton
class LocationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun isGpsEnabled(): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @Suppress("MissingPermission")
    suspend fun getCurrentLocation(): Location? = try {
        val cts = CancellationTokenSource()
        fusedLocationClient
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
            .await()
    } catch (e: Exception) {
        null
    }

    suspend fun reverseGeocode(latitude: Double, longitude: Double): ResolvedLocation {
        // Tier 1: Try Android Geocoder with IO dispatcher
        val geocoded = withContext(Dispatchers.IO) {
            try {
                if (Geocoder.isPresent()) {
                    val geocoder = Geocoder(context, Locale.ENGLISH)
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val addr: Address = addresses[0]
                        val loc = addr.locality ?: addr.subLocality ?: addr.featureName ?: ""
                        val dist = addr.subAdminArea ?: addr.adminArea ?: ""
                        val st = addr.adminArea ?: ""
                        val name = buildString {
                            if (loc.isNotBlank()) append("$loc, ")
                            if (dist.isNotBlank() && dist != loc) append("$dist, ")
                            if (st.isNotBlank()) append(st) else append("NER")
                        }.trim().trimEnd(',')
                        ResolvedLocation(
                            locality = loc.ifBlank { dist }.ifBlank { "Local Sector" },
                            district = dist.ifBlank { "Regional District" },
                            state = st.ifBlank { "Northeast India" },
                            formattedName = name.ifBlank { "Local Monitored Sector" }
                        )
                    } else null
                } else null
            } catch (e: Exception) {
                null
            }
        }

        if (geocoded != null) return geocoded

        // Tier 2: Offline Fallback mapping to nearest Northeast India district / town centroid
        return offlineRegionalLookup(latitude, longitude)
    }

    private data class RegionalCentroid(
        val locality: String,
        val district: String,
        val state: String,
        val lat: Double,
        val lon: Double
    )

    private val offlineCentroids = listOf(
        // Sikkim
        RegionalCentroid("Gangtok", "East Sikkim", "Sikkim", 27.3389, 88.6065),
        RegionalCentroid("Namchi", "South Sikkim", "Sikkim", 27.1706, 88.3547),
        RegionalCentroid("Dzongu", "North Sikkim", "Sikkim", 27.5210, 88.5412),
        RegionalCentroid("Geyzing", "West Sikkim", "Sikkim", 27.2886, 88.2443),
        RegionalCentroid("Pelling", "West Sikkim", "Sikkim", 27.3000, 88.2333),
        RegionalCentroid("Mangan", "North Sikkim", "Sikkim", 27.5029, 88.5284),
        // West Bengal (Hill Tracts)
        RegionalCentroid("Darjeeling", "Darjeeling", "West Bengal", 27.0410, 88.2663),
        RegionalCentroid("Kalimpong", "Kalimpong", "West Bengal", 27.0667, 88.4667),
        RegionalCentroid("Sevoke", "Darjeeling", "West Bengal", 26.8833, 88.4667),
        RegionalCentroid("Kurseong", "Darjeeling", "West Bengal", 26.8814, 88.2778),
        // Assam
        RegionalCentroid("Guwahati", "Kamrup Metropolitan", "Assam", 26.1445, 91.7362),
        RegionalCentroid("Haflong", "Dima Hasao", "Assam", 25.1824, 93.0182),
        RegionalCentroid("Silchar", "Cachar", "Assam", 24.8333, 92.8000),
        RegionalCentroid("Tezpur", "Sonitpur", "Assam", 26.6333, 92.7936),
        RegionalCentroid("Dibrugarh", "Dibrugarh", "Assam", 27.4728, 94.9120),
        RegionalCentroid("Jorhat", "Jorhat", "Assam", 26.7509, 94.2037),
        RegionalCentroid("Majuli", "Majuli", "Assam", 26.9500, 94.2200),
        RegionalCentroid("Kaziranga", "Golaghat", "Assam", 26.5800, 93.1700),
        // Meghalaya
        RegionalCentroid("Shillong", "East Khasi Hills", "Meghalaya", 25.5788, 91.8933),
        RegionalCentroid("Cherrapunji", "East Khasi Hills", "Meghalaya", 25.2700, 91.7300),
        RegionalCentroid("Mawphlang", "East Khasi Hills", "Meghalaya", 25.4500, 91.7500),
        RegionalCentroid("Tura", "West Garo Hills", "Meghalaya", 25.5144, 90.2206),
        RegionalCentroid("Jowai", "West Jaintia Hills", "Meghalaya", 25.4500, 92.2000),
        // Arunachal Pradesh
        RegionalCentroid("Itanagar", "Papum Pare", "Arunachal Pradesh", 27.0844, 93.6053),
        RegionalCentroid("Tawang", "Tawang", "Arunachal Pradesh", 27.5861, 91.8594),
        RegionalCentroid("Bomdila", "West Kameng", "Arunachal Pradesh", 27.2645, 92.4227),
        RegionalCentroid("Pasighat", "East Siang", "Arunachal Pradesh", 28.0667, 95.3333),
        // Nagaland
        RegionalCentroid("Kohima", "Kohima", "Nagaland", 25.6751, 94.1086),
        RegionalCentroid("Dimapur", "Dimapur", "Nagaland", 25.9094, 93.7266),
        RegionalCentroid("Mokokchung", "Mokokchung", "Nagaland", 26.3250, 94.5200),
        // Mizoram
        RegionalCentroid("Aizawl", "Aizawl", "Mizoram", 23.7271, 92.7176),
        RegionalCentroid("Lunglei", "Lunglei", "Mizoram", 22.8900, 92.7400),
        RegionalCentroid("Champhai", "Champhai", "Mizoram", 23.4750, 93.3300),
        // Manipur
        RegionalCentroid("Imphal", "Imphal West", "Manipur", 24.8170, 93.9368),
        RegionalCentroid("Churachandpur", "Churachandpur", "Manipur", 24.3333, 93.6833),
        // Tripura
        RegionalCentroid("Agartala", "West Tripura", "Tripura", 23.8315, 91.2868)
    )

    private fun offlineRegionalLookup(lat: Double, lon: Double): ResolvedLocation {
        var closest = offlineCentroids[0]
        var minD2 = Double.MAX_VALUE
        for (c in offlineCentroids) {
            val dLat = lat - c.lat
            val dLon = lon - c.lon
            val d2 = dLat * dLat + dLon * dLon
            if (d2 < minD2) {
                minD2 = d2
                closest = c
            }
        }
        val formatted = "${closest.locality}, ${closest.district} (${closest.state})"
        return ResolvedLocation(
            locality = closest.locality,
            district = closest.district,
            state = closest.state,
            formattedName = formatted
        )
    }
}

@Singleton
class NetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        @Volatile
        private var instance: NetworkMonitor? = null

        fun getInstance(context: Context): NetworkMonitor {
            return instance ?: synchronized(this) {
                instance ?: NetworkMonitor(context.applicationContext).also { instance = it }
            }
        }
    }

    private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _isOnline = MutableStateFlow(isCurrentlyOnline())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            val caps = cm.getNetworkCapabilities(network)
            val hasInternet = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: true
            _isOnline.value = hasInternet
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            _isOnline.value = hasInternet
        }

        override fun onLost(network: Network) {
            _isOnline.value = false
        }

        override fun onUnavailable() {
            _isOnline.value = false
        }
    }

    init {
        instance = this
        try {
            cm.registerDefaultNetworkCallback(networkCallback)
        } catch (e: Exception) {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            cm.registerNetworkCallback(request, networkCallback)
        }

        // Active background heartbeat every 2.5 seconds to guarantee immediate sync
        // across fast airplane mode toggles or OEM callback pauses
        scope.launch {
            while (isActive) {
                delay(2500L)
                val current = isCurrentlyOnline()
                if (_isOnline.value != current) {
                    _isOnline.value = current
                }
            }
        }
    }

    fun refresh() {
        _isOnline.value = isCurrentlyOnline()
    }

    fun isCurrentlyOnline(): Boolean {
        val active = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(active) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

// Time formatting utilities
fun Long.toRelativeTimeString(): String {
    val now = System.currentTimeMillis()
    val diff = now - this
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        else -> "${diff / 86_400_000}d ago"
    }
}
