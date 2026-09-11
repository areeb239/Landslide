package com.ner.landslide.domain.model

// User roles
enum class UserRole { CITIZEN, FIELD_OFFICER, ADMIN }

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: UserRole = UserRole.CITIZEN,
    val fcmToken: String = ""
)

// Alert severity
enum class AlertSeverity { LOW, MODERATE, HIGH, CRITICAL }

data class Alert(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val severity: AlertSeverity = AlertSeverity.LOW,
    val affectedDistrict: String = "",
    val affectedVillages: List<String> = emptyList(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val issuedAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

// Risk zone on the GIS map
data class RiskZone(
    val id: String = "",
    val name: String = "",
    val severity: AlertSeverity = AlertSeverity.LOW,
    val polygonPoints: List<LatLng> = emptyList(),
    val district: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
)

data class LatLng(val latitude: Double, val longitude: Double)

// Road connectivity status
enum class RoadStatus { OPEN, BLOCKED, PARTIALLY_BLOCKED, UNKNOWN }

data class RoadSegment(
    val id: String = "",
    val name: String = "",
    val status: RoadStatus = RoadStatus.UNKNOWN,
    val points: List<LatLng> = emptyList(),
    val blockageReason: String = "",
    val reportedAt: Long = System.currentTimeMillis()
)

// Incident report submitted by field officer / citizen
data class IncidentReport(
    val id: String = "",
    val reporterUid: String = "",
    val reporterName: String = "",
    val incidentType: IncidentType = IncidentType.LANDSLIDE,
    val severity: AlertSeverity = AlertSeverity.MODERATE,
    val description: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val photoUrls: List<String> = emptyList(),
    val videoUrl: String = "",
    val district: String = "",
    val village: String = "",
    val reportedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

enum class IncidentType {
    LANDSLIDE, CRACK, ROAD_BLOCKAGE, FLASH_FLOOD, SLOPE_MOVEMENT, OTHER
}

// SOS alert
data class SOSAlert(
    val id: String = "",
    val uid: String = "",
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val message: String = "SOS — Need Help!",
    val triggeredAt: Long = System.currentTimeMillis(),
    val isResolved: Boolean = false
)

// AI Prediction from FastAPI
data class PredictionRequest(
    val rainfallMm: Double = 0.0,
    val slopeDeg: Double = 35.0,
    val soilMoisturePct: Double = 75.0,
    val antecedentRain3d: Double = 120.0,
    val elevation: Double = 1450.0,
    val slope: Double = 35.0,
    val rainfallPrevious1d: Double = 45.0,
    val rainfallPrevious3d: Double = 120.0,
    val rainfallPrevious7d: Double = 250.0,
    val lithologyGroup: String = "Metamorphic rocks",
    val landCover: String = "Tree cover",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

object ModelConstants {
    val LITHOLOGY_GROUPS = listOf(
        "Metamorphic rocks",
        "Siliciclastic sedimentary rocks",
        "Unconsolidated sediments",
        "Carbonate sedimentary rocks",
        "Mixed sedimentary rocks",
        "Acid plutonic rocks",
        "Basic plutonic rocks",
        "Intermediate volcanic rocks",
        "Basic volcanic rocks"
    )

    val LAND_COVER_CLASSES = listOf(
        "Tree cover",
        "Grassland",
        "Cropland",
        "Built-up",
        "Bare/sparse vegetation",
        "Shrubland",
        "Herbaceous wetland",
        "Snow/ice",
        "Permanent water"
    )
}

data class FeatureExtractionResult(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val date: String = "",
    val locationName: String? = null,
    val elevation: Double = 1450.0,
    val slope: Double = 35.0,
    val rainfallPrevious1d: Double = 25.0,
    val rainfallPrevious3d: Double = 75.0,
    val rainfallPrevious7d: Double = 160.0,
    val lithologyGroup: String = "Metamorphic rocks",
    val landCover: String = "Tree cover",
    val source: Map<String, String> = emptyMap()
)

data class PredictionResult(
    val riskLevel: String = "",          // LOW / MODERATE / HIGH / CRITICAL
    val probability: Double = 0.0,       // 0.0 – 1.0
    val confidence: Double = 0.0,        // Deprecated; use probability
    val factors: Map<String, Double> = emptyMap(), // XAI factor importance
    val featureImportances: Map<String, Double> = emptyMap(),
    val sampleFactors: Map<String, Any> = emptyMap(),
    val recommendation: String = "",
    val isMock: Boolean = false,         // true when backend is unavailable, uses fallback
    val modelVersion: String = "Bhoochetak-XGBoost (bhurakshak_pipeline.pkl)"
)

// Weather (Open-Meteo)
data class HourlyWeather(
    val time: String = "",
    val rainfallMm: Double = 0.0,
    val temperature: Double = 0.0,
    val humidity: Double = 0.0,
    val windSpeedKmh: Double = 0.0,
    val rainProbability: Int = 0,
    val uvIndex: Double = 0.0,
    val windDirectionDeg: Double = 0.0,
    val weatherCode: Int = 0
)

data class WeatherForecast(
    val district: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val hourlyData: List<HourlyWeather> = emptyList(),
    val fetchedAt: Long = System.currentTimeMillis()
)
