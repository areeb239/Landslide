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
    companion object {
        const val NER_MIN_LAT = 21.5
        const val NER_MAX_LAT = 29.8
        const val NER_MIN_LON = 87.5
        const val NER_MAX_LON = 97.5

        const val INDIA_MIN_LAT = 6.5
        const val INDIA_MAX_LAT = 37.5
        const val INDIA_MIN_LON = 68.0
        const val INDIA_MAX_LON = 97.5

        const val DEFAULT_NER_LAT = 27.3389
        const val DEFAULT_NER_LON = 88.6065
        const val DEFAULT_NER_NAME = "Gangtok, East Sikkim (NER)"

        fun isWithinNER(lat: Double, lon: Double): Boolean {
            return lat in NER_MIN_LAT..NER_MAX_LAT && lon in NER_MIN_LON..NER_MAX_LON
        }

        fun isWithinIndia(lat: Double, lon: Double): Boolean {
            return lat in INDIA_MIN_LAT..INDIA_MAX_LAT && lon in INDIA_MIN_LON..INDIA_MAX_LON
        }
    }

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
        // Tier 1: Try Android Geocoder with IO dispatcher & 2.5s timeout
        val geocoded = withContext(Dispatchers.IO) {
            try {
                withTimeoutOrNull(2500L) {
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
                }
            } catch (e: Exception) {
                null
            }
        }

        if (geocoded != null) return geocoded

        // Tier 2: Offline Fallback mapping to nearest Northeast India district / town centroid
        return offlineRegionalLookup(latitude, longitude)
    }

    suspend fun forwardGeocode(query: String): Pair<Double, Double>? = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext null
        try {
            if (Geocoder.isPresent()) {
                val geocoder = Geocoder(context, Locale.ENGLISH)
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(query, 1)
                if (!addresses.isNullOrEmpty()) {
                    val a = addresses[0]
                    return@withContext Pair(a.latitude, a.longitude)
                }
            }
        } catch (e: Exception) {
            // Fall through to regional centroids
        }

        val matched = offlineCentroids.firstOrNull {
            query.contains(it.locality, ignoreCase = true) ||
            query.contains(it.district, ignoreCase = true) ||
            it.locality.contains(query, ignoreCase = true)
        }
        if (matched != null) {
            Pair(matched.lat, matched.lon)
        } else null
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
        RegionalCentroid("Agartala", "West Tripura", "Tripura", 23.8315, 91.2868),
        // Western Ghats & Southern Hills
        RegionalCentroid("Wayanad", "Wayanad", "Kerala", 11.6854, 76.1320),
        RegionalCentroid("Munnar", "Idukki", "Kerala", 10.0889, 77.0595),
        RegionalCentroid("Idukki", "Idukki", "Kerala", 9.8494, 76.9806),
        RegionalCentroid("Mahabaleshwar", "Satara", "Maharashtra", 17.9237, 73.6586),
        RegionalCentroid("Lonavala", "Pune", "Maharashtra", 18.7557, 73.4091),
        RegionalCentroid("Ooty", "Nilgiris", "Tamil Nadu", 11.4102, 76.6950),
        RegionalCentroid("Coorg", "Kodagu", "Karnataka", 12.4244, 75.7382),
        // Western & Central Himalayas
        RegionalCentroid("Shimla", "Shimla", "Himachal Pradesh", 31.1048, 77.1734),
        RegionalCentroid("Manali", "Kullu", "Himachal Pradesh", 32.2396, 77.1887),
        RegionalCentroid("Dharamshala", "Kangra", "Himachal Pradesh", 32.2190, 76.3234),
        RegionalCentroid("Chamoli", "Chamoli", "Uttarakhand", 30.5526, 79.5658),
        RegionalCentroid("Joshimath", "Chamoli", "Uttarakhand", 30.5562, 79.5649),
        RegionalCentroid("Rishikesh", "Dehradun", "Uttarakhand", 30.0869, 78.2676),
        RegionalCentroid("Dehradun", "Dehradun", "Uttarakhand", 30.3165, 78.0322),
        RegionalCentroid("Nainital", "Nainital", "Uttarakhand", 29.3919, 79.4542),
        RegionalCentroid("Srinagar", "Srinagar", "Jammu and Kashmir", 34.0837, 74.7973),
        // Major Urban & Plain Reference Baselines
        RegionalCentroid("Lucknow", "Lucknow", "Uttar Pradesh", 26.8467, 80.9462),
        RegionalCentroid("New Delhi", "New Delhi", "Delhi", 28.6139, 77.2090),
        RegionalCentroid("Patna", "Patna", "Bihar", 25.5941, 85.1376),
        RegionalCentroid("Kolkata", "Kolkata", "West Bengal", 22.5726, 88.3639)
    )

    fun offlineRegionalLookup(lat: Double, lon: Double): ResolvedLocation {
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
            val hasInternet = caps?.let {
                it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ||
                it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                it.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            } ?: true
            _isOnline.value = hasInternet
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ||
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
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

    fun notifyNetworkSuccess() {
        if (!_isOnline.value) {
            _isOnline.value = true
        }
    }

    fun isCurrentlyOnline(): Boolean {
        val active = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(active) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ||
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
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
