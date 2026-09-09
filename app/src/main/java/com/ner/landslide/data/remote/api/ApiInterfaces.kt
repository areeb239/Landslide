package com.ner.landslide.data.remote.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// ─── FastAPI Prediction API ────────────────────────────────────────────────────

data class PredictionRequestDto(
    @SerializedName("rainfall_mm") val rainfallMm: Double,
    @SerializedName("slope_deg") val slopeDeg: Double,
    @SerializedName("soil_moisture_pct") val soilMoisturePct: Double,
    @SerializedName("antecedent_rain_3d") val antecedentRain3d: Double,
    @SerializedName("latitude") val latitude: Double = 0.0,
    @SerializedName("longitude") val longitude: Double = 0.0
)

data class PredictionResponseDto(
    @SerializedName("risk_level") val riskLevel: String,
    @SerializedName("probability") val probability: Double,
    @SerializedName("confidence") val confidence: Double,
    @SerializedName("factors") val factors: Map<String, Double>,
    @SerializedName("recommendation") val recommendation: String
)

interface PredictionApi {
    @POST("api/v1/predict")
    suspend fun predictRisk(@Body request: PredictionRequestDto): PredictionResponseDto

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
