package com.ner.landslide.data.remote.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// ─── FastAPI Prediction API ────────────────────────────────────────────────────

data class PredictionRequestDto(
    @SerializedName("rainfall_mm") val rainfallMm: Double? = null,
    @SerializedName("slope_deg") val slopeDeg: Double? = null,
    @SerializedName("soil_moisture_pct") val soilMoisturePct: Double? = null,
    @SerializedName("antecedent_rain_3d") val antecedentRain3d: Double? = null,
    @SerializedName("elevation") val elevation: Double? = null,
    @SerializedName("slope") val slope: Double? = null,
    @SerializedName("rainfall_previous_1d") val rainfallPrevious1d: Double? = null,
    @SerializedName("rainfall_previous_3d") val rainfallPrevious3d: Double? = null,
    @SerializedName("rainfall_previous_7d") val rainfallPrevious7d: Double? = null,
    @SerializedName("lithology_group") val lithologyGroup: String? = null,
    @SerializedName("land_cover") val landCover: String? = null,
    @SerializedName("latitude") val latitude: Double = 0.0,
    @SerializedName("longitude") val longitude: Double = 0.0
)

data class PredictionResponseDto(
    @SerializedName("risk_level") val riskLevel: String,
    @SerializedName("probability") val probability: Double,
    @SerializedName("confidence") val confidence: Double? = null,
    @SerializedName("factors") val factors: Map<String, Double> = emptyMap(),
    @SerializedName("feature_importances") val featureImportances: Map<String, Double> = emptyMap(),
    @SerializedName("sample_factors") val sampleFactors: Map<String, Any> = emptyMap(),
    @SerializedName("recommendation") val recommendation: String = "",
    @SerializedName("is_mock") val isMock: Boolean = false,
    @SerializedName("model_version") val modelVersion: String? = null
)

data class FeatureExtractionResponseDto(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("date") val date: String,
    @SerializedName("location_name") val locationName: String? = null,
    @SerializedName("elevation") val elevation: Double,
    @SerializedName("slope") val slope: Double,
    @SerializedName("rainfall_previous_1d") val rainfallPrevious1d: Double,
    @SerializedName("rainfall_previous_3d") val rainfallPrevious3d: Double,
    @SerializedName("rainfall_previous_7d") val rainfallPrevious7d: Double,
    @SerializedName("lithology_group") val lithologyGroup: String,
    @SerializedName("land_cover") val landCover: String,
    @SerializedName("source") val source: Map<String, String> = emptyMap()
)

interface PredictionApi {
    @POST("api/v1/predict")
    suspend fun predictRisk(@Body request: PredictionRequestDto): PredictionResponseDto

    @GET("api/v1/predict/extract-features")
    suspend fun extractFeatures(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("date") date: String? = null
    ): FeatureExtractionResponseDto

    @GET("api/v1/health")
    suspend fun healthCheck(): Map<String, String>
}

// ─── Open-Meteo Weather API ────────────────────────────────────────────────────

data class OpenMeteoResponse(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("hourly") val hourly: HourlyDto
)

data class HourlyDto(
    @SerializedName("time") val time: List<String>,
    @SerializedName("precipitation") val precipitation: List<Double>,
    @SerializedName("temperature_2m") val temperature: List<Double>,
    @SerializedName("relative_humidity_2m") val humidity: List<Double>,
    @SerializedName("wind_speed_10m") val windSpeed: List<Double>
)

interface WeatherApi {
    @GET("v1/forecast")
    suspend fun getWeatherForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("hourly") hourly: String = "precipitation,temperature_2m,relative_humidity_2m,wind_speed_10m",
        @Query("forecast_days") forecastDays: Int = 3,
        @Query("timezone") timezone: String = "Asia/Kolkata"
    ): OpenMeteoResponse
}
