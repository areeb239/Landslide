package com.ner.landslide.util

import android.content.Context
import android.location.Address
import android.location.Geocoder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class SelectedLocation(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val isGpsLocation: Boolean,
    val district: String = "",
    val state: String = ""
)

data class LocationSearchResult(
    val name: String,
    val state: String,
    val latitude: Double,
    val longitude: Double,
    val category: String = "Popular Sector" // "Hotspot", "Himalayas", "Western Ghats", "City", etc.
)

@Singleton
class AppLocationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locationHelper: LocationHelper
) {
    private val prefs = context.getSharedPreferences("bhoochetak_location_prefs", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Initial default: restore saved location or fallback to Gangtok (Sikkim)
    private val _selectedLocation: MutableStateFlow<SelectedLocation>

    init {
        val savedName = prefs.getString("loc_name", "Gangtok, Sikkim") ?: "Gangtok, Sikkim"
        val savedLat = prefs.getFloat("loc_lat", 27.3389f).toDouble()
        val savedLon = prefs.getFloat("loc_lon", 88.6065f).toDouble()
        val savedIsGps = prefs.getBoolean("loc_is_gps", false)
        val savedDist = prefs.getString("loc_dist", "East Sikkim") ?: "East Sikkim"
        val savedState = prefs.getString("loc_state", "Sikkim") ?: "Sikkim"

        _selectedLocation = MutableStateFlow(
            SelectedLocation(
                name = savedName,
                latitude = savedLat,
                longitude = savedLon,
                isGpsLocation = savedIsGps,
                district = savedDist,
                state = savedState
            )
        )

        // If user previously selected GPS, or on first clean launch, attempt to auto-sync with real GPS
        if (savedIsGps || !prefs.contains("loc_name")) {
            scope.launch {
                switchToCurrentGpsLocation()
            }
        }
    }

    val selectedLocation: StateFlow<SelectedLocation> = _selectedLocation.asStateFlow()

    suspend fun switchToCurrentGpsLocation(): Boolean {
        val loc = locationHelper.getCurrentLocation()
        if (loc != null) {
            val resolved = locationHelper.reverseGeocode(loc.latitude, loc.longitude)
            val cleanName = resolved.formattedHeadline.ifBlank {
                "${resolved.locality}, ${resolved.district}"
            }.ifBlank { "Current GPS Location" }

            val newLoc = SelectedLocation(
                name = cleanName,
                latitude = loc.latitude,
                longitude = loc.longitude,
                isGpsLocation = true,
                district = resolved.district,
                state = resolved.state
            )

            persistLocation(newLoc)
            _selectedLocation.value = newLoc
            return true
        }
        return false
    }

    fun setCustomLocation(name: String, latitude: Double, longitude: Double, district: String = "", state: String = "") {
        val newLoc = SelectedLocation(
            name = name,
            latitude = latitude,
            longitude = longitude,
            isGpsLocation = false,
            district = district,
            state = state
        )
        persistLocation(newLoc)
        _selectedLocation.value = newLoc
    }

    private fun persistLocation(loc: SelectedLocation) {
        prefs.edit()
            .putString("loc_name", loc.name)
            .putFloat("loc_lat", loc.latitude.toFloat())
            .putFloat("loc_lon", loc.longitude.toFloat())
            .putBoolean("loc_is_gps", loc.isGpsLocation)
            .putString("loc_dist", loc.district)
            .putString("loc_state", loc.state)
            .apply()
    }

    /**
     * Search locations combining curated offline Indian hotspots and Android forward geocoding.
     * Never exposes raw coordinates to the UI.
     */
    suspend fun searchLocations(query: String): List<LocationSearchResult> = withContext(Dispatchers.IO) {
        val q = query.trim().lowercase()
        if (q.isBlank()) return@withContext POPULAR_SECTOR_HOTSPOTS

        // 1. Fast offline instant match across curated locations
        val offlineMatches = POPULAR_SECTOR_HOTSPOTS.filter {
            it.name.lowercase().contains(q) ||
            it.state.lowercase().contains(q) ||
            it.category.lowercase().contains(q)
        }

        // 2. If user query has >= 3 chars, supplement with Android Geocoder for any arbitrary town/city
        val geocodedMatches = mutableListOf<LocationSearchResult>()
        if (q.length >= 3) {
            try {
                withTimeoutOrNull(2000L) {
                    if (Geocoder.isPresent()) {
                        val geocoder = Geocoder(context, Locale.ENGLISH)
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocationName("$query, India", 5)
                        addresses?.forEach { addr ->
                            val locality = addr.locality ?: addr.subLocality ?: addr.featureName ?: ""
                            val district = addr.subAdminArea ?: ""
                            val state = addr.adminArea ?: "India"
                            val displayName = buildString {
                                if (locality.isNotBlank()) append(locality)
                                if (district.isNotBlank() && district != locality) {
                                    if (isNotEmpty()) append(", ")
                                    append(district)
                                }
                                if (state.isNotBlank() && state != district) {
                                    if (isNotEmpty()) append(", ")
                                    append(state)
                                }
                            }.trim()

                            if (displayName.isNotBlank() && offlineMatches.none { it.name.equals(displayName, ignoreCase = true) }) {
                                geocodedMatches.add(
                                    LocationSearchResult(
                                        name = displayName,
                                        state = state,
                                        latitude = addr.latitude,
                                        longitude = addr.longitude,
                                        category = "Search Result"
                                    )
                                )
                            }
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        (offlineMatches + geocodedMatches).distinctBy { it.name }
    }

    companion object {
        val POPULAR_SECTOR_HOTSPOTS = listOf(
            // Eastern Himalayas & Northeast
            LocationSearchResult("Gangtok", "Sikkim", 27.3389, 88.6065, "Himalayas"),
            LocationSearchResult("Darjeeling", "West Bengal", 27.0410, 88.2663, "Himalayas"),
            LocationSearchResult("Kalimpong", "West Bengal", 27.0594, 88.4695, "Himalayas"),
            LocationSearchResult("Shillong", "Meghalaya", 25.5788, 91.8933, "Northeast"),
            LocationSearchResult("Cherrapunji (Sohra)", "Meghalaya", 25.2702, 91.7323, "Northeast"),
            LocationSearchResult("Guwahati", "Assam", 26.1445, 91.7362, "Northeast"),
            LocationSearchResult("Itanagar", "Arunachal Pradesh", 27.0844, 93.6053, "Northeast"),
            LocationSearchResult("Tawang", "Arunachal Pradesh", 27.5861, 91.8594, "Himalayas"),
            LocationSearchResult("Kohima", "Nagaland", 25.6751, 94.1086, "Northeast"),
            LocationSearchResult("Aizawl", "Mizoram", 23.7271, 92.7176, "Northeast"),
            LocationSearchResult("Imphal", "Manipur", 24.8170, 93.9368, "Northeast"),
            LocationSearchResult("Agartala", "Tripura", 23.8315, 91.2868, "Northeast"),

            // Western Himalayas & Northern Hills
            LocationSearchResult("Shimla", "Himachal Pradesh", 31.1048, 77.1734, "Himalayas"),
            LocationSearchResult("Manali", "Himachal Pradesh", 32.2432, 77.1892, "Himalayas"),
            LocationSearchResult("Dharamshala", "Himachal Pradesh", 32.2190, 76.3234, "Himalayas"),
            LocationSearchResult("Kullu", "Himachal Pradesh", 31.9579, 77.1095, "Himalayas"),
            LocationSearchResult("Chamoli / Joshimath", "Uttarakhand", 30.5526, 79.5658, "Himalayas"),
            LocationSearchResult("Rudraprayag / Kedarnath", "Uttarakhand", 30.2844, 78.9811, "Himalayas"),
            LocationSearchResult("Uttarkashi", "Uttarakhand", 30.7268, 78.4354, "Himalayas"),
            LocationSearchResult("Nainital", "Uttarakhand", 29.3919, 79.4542, "Himalayas"),
            LocationSearchResult("Mussoorie", "Uttarakhand", 30.4598, 78.0644, "Himalayas"),
            LocationSearchResult("Dehradun", "Uttarakhand", 30.3165, 78.0322, "Foothills"),
            LocationSearchResult("Rishikesh", "Uttarakhand", 30.0869, 78.2676, "Foothills"),
            LocationSearchResult("Srinagar", "Jammu & Kashmir", 34.0837, 74.7973, "Himalayas"),

            // Western Ghats & Southern Ranges
            LocationSearchResult("Wayanad", "Kerala", 11.6854, 76.1320, "Western Ghats"),
            LocationSearchResult("Munnar", "Kerala", 10.0889, 77.0595, "Western Ghats"),
            LocationSearchResult("Idukki", "Kerala", 9.8494, 76.9806, "Western Ghats"),
            LocationSearchResult("Ooty (Udhagamandalam)", "Tamil Nadu", 11.4102, 76.6950, "Western Ghats"),
            LocationSearchResult("Kodaikanal", "Tamil Nadu", 10.2381, 77.4892, "Western Ghats"),
            LocationSearchResult("Coorg (Kodagu)", "Karnataka", 12.3375, 75.8069, "Western Ghats"),
            LocationSearchResult("Chikkamagaluru", "Karnataka", 13.3161, 75.7720, "Western Ghats"),
            LocationSearchResult("Mahabaleshwar", "Maharashtra", 17.9307, 73.6477, "Western Ghats"),
            LocationSearchResult("Lonavala", "Maharashtra", 18.7557, 73.4091, "Western Ghats"),

            // Plain Hubs & Major Sectors
            LocationSearchResult("Lucknow", "Uttar Pradesh", 26.8467, 80.9462, "Plains"),
            LocationSearchResult("Kanpur", "Uttar Pradesh", 26.4499, 80.3319, "Plains"),
            LocationSearchResult("Varanasi", "Uttar Pradesh", 25.3176, 82.9739, "Plains"),
            LocationSearchResult("Patna", "Bihar", 25.5941, 85.1376, "Plains"),
            LocationSearchResult("New Delhi", "Delhi", 28.6139, 77.2090, "Metropolitan"),
            LocationSearchResult("Kolkata", "West Bengal", 22.5726, 88.3639, "Metropolitan"),
            LocationSearchResult("Mumbai", "Maharashtra", 19.0760, 72.8777, "Metropolitan"),
            LocationSearchResult("Bengaluru", "Karnataka", 12.9716, 77.5946, "Metropolitan")
        )
    }
}
