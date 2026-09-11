package com.ner.landslide.data.repository

import android.content.Context
import com.ner.landslide.data.local.database.NERDatabase
import com.ner.landslide.data.local.database.PendingReportEntity
import com.ner.landslide.data.local.database.PendingSOSEntity
import com.ner.landslide.data.remote.api.*
import com.ner.landslide.data.remote.firestore.*
import com.ner.landslide.domain.model.*
import com.ner.landslide.domain.repository.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// ─── Alert Repository ─────────────────────────────────────────────────────────

class AlertRepositoryImpl @Inject constructor(
    private val source: AlertFirestoreSource
) : AlertRepository {
    override fun getActiveAlerts(): Flow<List<Alert>> = source.getActiveAlerts()
    override suspend fun broadcastAlert(alert: Alert): Result<Unit> = source.broadcastAlert(alert)
}

// ─── Report Repository (offline-first) ────────────────────────────────────────

class ReportRepositoryImpl @Inject constructor(
    private val firestoreSource: ReportFirestoreSource,
    private val db: NERDatabase
) : ReportRepository {

    override suspend fun submitReport(report: IncidentReport): Result<Unit> = runCatching {
        // Always save offline first
        val entity = PendingReportEntity(
            reporterUid = report.reporterUid,
            reporterName = report.reporterName,
            incidentType = report.incidentType.name,
            severity = report.severity.name,
            description = report.description,
            latitude = report.latitude,
            longitude = report.longitude,
            district = report.district,
            village = report.village,
            reportedAt = report.reportedAt
        )
        val localId = db.pendingReportDao().insertReport(entity).toInt()
        // Then attempt live sync
        firestoreSource.submitReport(report).onSuccess {
            db.pendingReportDao().markSynced(localId)
        }
        Unit
    }

    override fun getAllReports(): Flow<List<IncidentReport>> = firestoreSource.getAllReports()

    override fun getPendingOfflineReports(): Flow<List<IncidentReport>> =
        db.pendingReportDao().getUnsyncedReports().map { entities ->
            entities.map { e ->
                IncidentReport(
                    reporterUid = e.reporterUid,
                    reporterName = e.reporterName,
                    incidentType = IncidentType.valueOf(e.incidentType),
                    severity = AlertSeverity.valueOf(e.severity),
                    description = e.description,
                    latitude = e.latitude,
                    longitude = e.longitude,
                    district = e.district,
                    village = e.village,
                    reportedAt = e.reportedAt,
                    isSynced = false
                )
            }
        }

    override suspend fun syncPendingReports(): Result<Unit> = runCatching {
        val pending = db.pendingReportDao().getUnsyncedReports().first()
        pending.forEach { entity ->
            val report = IncidentReport(
                reporterUid = entity.reporterUid,
                reporterName = entity.reporterName,
                incidentType = IncidentType.valueOf(entity.incidentType),
                severity = AlertSeverity.valueOf(entity.severity),
                description = entity.description,
                latitude = entity.latitude,
                longitude = entity.longitude,
                district = entity.district,
                village = entity.village,
                reportedAt = entity.reportedAt
            )
            firestoreSource.submitReport(report).onSuccess {
                db.pendingReportDao().markSynced(entity.localId)
            }
        }
    }
}

// ─── Risk Zone Repository ─────────────────────────────────────────────────────

class RiskZoneRepositoryImpl @Inject constructor(
    private val source: RiskZoneFirestoreSource
) : RiskZoneRepository {
    override fun getRiskZones(): Flow<List<RiskZone>> = source.getRiskZones()
    override fun getRoadSegments(): Flow<List<RoadSegment>> = source.getRoadSegments()
}

// ─── Prediction Repository (FastAPI) ─────────────────────────────────────────

class PredictionRepositoryImpl @Inject constructor(
    private val api: PredictionApi
) : PredictionRepository {

    override suspend fun predictRisk(request: PredictionRequest): Result<PredictionResult> =
        runCatching {
            val dto = api.predictRisk(
                PredictionRequestDto(
                    rainfallMm = request.rainfallPrevious1d.takeIf { it > 0 } ?: request.rainfallMm,
                    slopeDeg = request.slope.takeIf { it > 0 } ?: request.slopeDeg,
                    soilMoisturePct = request.soilMoisturePct,
                    antecedentRain3d = request.rainfallPrevious3d.takeIf { it > 0 } ?: request.antecedentRain3d,
                    elevation = request.elevation,
                    slope = request.slope.takeIf { it > 0 } ?: request.slopeDeg,
                    rainfallPrevious1d = request.rainfallPrevious1d.takeIf { it > 0 } ?: request.rainfallMm,
                    rainfallPrevious3d = request.rainfallPrevious3d.takeIf { it > 0 } ?: request.antecedentRain3d,
                    rainfallPrevious7d = request.rainfallPrevious7d,
                    lithologyGroup = request.lithologyGroup,
                    landCover = request.landCover,
                    latitude = request.latitude,
                    longitude = request.longitude
                )
            )
            PredictionResult(
                riskLevel = dto.riskLevel,
                probability = dto.probability,
                confidence = dto.confidence ?: dto.probability,
                factors = dto.factors,
                featureImportances = dto.featureImportances,
                sampleFactors = dto.sampleFactors,
                recommendation = dto.recommendation,
                isMock = dto.isMock,
                modelVersion = dto.modelVersion ?: "Bhoochetak-XGBoost (bhurakshak_pipeline.pkl)"
            )
        }.recoverCatching {
            // FastAPI not reachable — return mock response so demo never breaks
            mockPrediction(request)
        }

    override suspend fun extractFeatures(
        latitude: Double,
        longitude: Double,
        date: String?
    ): Result<FeatureExtractionResult> = runCatching {
        val dto = api.extractFeatures(latitude, longitude, date)
        FeatureExtractionResult(
            latitude = dto.latitude,
            longitude = dto.longitude,
            date = dto.date,
            locationName = dto.locationName,
            elevation = dto.elevation,
            slope = dto.slope,
            rainfallPrevious1d = dto.rainfallPrevious1d,
            rainfallPrevious3d = dto.rainfallPrevious3d,
            rainfallPrevious7d = dto.rainfallPrevious7d,
            lithologyGroup = dto.lithologyGroup,
            landCover = dto.landCover,
            source = dto.source
        )
    }.recoverCatching {
        mockExtractFeatures(latitude, longitude, date)
    }

    private fun mockExtractFeatures(
        lat: Double,
        lon: Double,
        date: String?
    ): FeatureExtractionResult {
        // Fallback preset lookup for demo continuity
        return when {
            kotlin.math.abs(lat - 27.33) < 0.2 -> FeatureExtractionResult(
                latitude = 27.33, longitude = 88.61, date = date ?: "2026-09-11",
                locationName = "Gangtok (Sikkim)", elevation = 1562.0, slope = 28.5,
                rainfallPrevious1d = 24.5, rainfallPrevious3d = 88.0, rainfallPrevious7d = 185.5,
                lithologyGroup = "Metamorphic rocks", landCover = "Tree cover"
            )
            kotlin.math.abs(lat - 25.57) < 0.2 -> FeatureExtractionResult(
                latitude = 25.57, longitude = 91.89, date = date ?: "2026-09-11",
                locationName = "Shillong (Meghalaya)", elevation = 1496.0, slope = 22.0,
                rainfallPrevious1d = 32.0, rainfallPrevious3d = 110.0, rainfallPrevious7d = 215.0,
                lithologyGroup = "Metamorphic rocks", landCover = "Tree cover"
            )
            kotlin.math.abs(lat - 26.14) < 0.2 -> FeatureExtractionResult(
                latitude = 26.14, longitude = 91.74, date = date ?: "2026-09-11",
                locationName = "Guwahati / Kamrup (Assam)", elevation = 55.0, slope = 16.5,
                rainfallPrevious1d = 5.0, rainfallPrevious3d = 18.0, rainfallPrevious7d = 42.0,
                lithologyGroup = "Unconsolidated sediments", landCover = "Built-up"
            )
            kotlin.math.abs(lat - 23.73) < 0.2 -> FeatureExtractionResult(
                latitude = 23.73, longitude = 92.71, date = date ?: "2026-09-11",
                locationName = "Aizawl (Mizoram)", elevation = 1132.0, slope = 34.0,
                rainfallPrevious1d = 18.0, rainfallPrevious3d = 65.0, rainfallPrevious7d = 140.0,
                lithologyGroup = "Siliciclastic sedimentary rocks", landCover = "Tree cover"
            )
            kotlin.math.abs(lat - 25.67) < 0.2 -> FeatureExtractionResult(
                latitude = 25.67, longitude = 94.11, date = date ?: "2026-09-11",
                locationName = "Kohima (Nagaland)", elevation = 1444.0, slope = 31.5,
                rainfallPrevious1d = 20.0, rainfallPrevious3d = 72.0, rainfallPrevious7d = 155.0,
                lithologyGroup = "Siliciclastic sedimentary rocks", landCover = "Tree cover"
            )
            kotlin.math.abs(lat - 27.08) < 0.2 -> FeatureExtractionResult(
                latitude = 27.08, longitude = 93.60, date = date ?: "2026-09-11",
                locationName = "Itanagar (Arunachal Pradesh)", elevation = 320.0, slope = 26.0,
                rainfallPrevious1d = 28.0, rainfallPrevious3d = 95.0, rainfallPrevious7d = 190.0,
                lithologyGroup = "Mixed sedimentary rocks", landCover = "Tree cover"
            )
            kotlin.math.abs(lat - 26.58) < 0.2 -> FeatureExtractionResult(
                latitude = 26.58, longitude = 93.17, date = date ?: "2026-09-11",
                locationName = "Kaziranga Foothills Buffer (Assam)", elevation = 85.0, slope = 4.5,
                rainfallPrevious1d = 1.5, rainfallPrevious3d = 4.5, rainfallPrevious7d = 12.0,
                lithologyGroup = "Unconsolidated sediments", landCover = "Tree cover"
            )
            kotlin.math.abs(lat - 26.95) < 0.2 -> FeatureExtractionResult(
                latitude = 26.95, longitude = 94.22, date = date ?: "2026-09-11",
                locationName = "Majuli Agricultural Plain (Assam)", elevation = 84.0, slope = 2.0,
                rainfallPrevious1d = 2.0, rainfallPrevious3d = 6.0, rainfallPrevious7d = 16.0,
                lithologyGroup = "Unconsolidated sediments", landCover = "Cropland"
            )
            kotlin.math.abs(lat - 25.45) < 0.2 -> FeatureExtractionResult(
                latitude = 25.45, longitude = 91.75, date = date ?: "2026-09-11",
                locationName = "Mawphlang Sacred Forest (Meghalaya)", elevation = 1620.0, slope = 15.0,
                rainfallPrevious1d = 2.5, rainfallPrevious3d = 7.0, rainfallPrevious7d = 16.0,
                lithologyGroup = "Metamorphic rocks", landCover = "Tree cover"
            )
            else -> FeatureExtractionResult(
                latitude = lat, longitude = lon, date = date ?: "2026-09-11",
                locationName = "Darjeeling Hill Tracts", elevation = 2042.0, slope = 38.0,
                rainfallPrevious1d = 35.0, rainfallPrevious3d = 125.0, rainfallPrevious7d = 240.0,
                lithologyGroup = "Metamorphic rocks", landCover = "Cropland"
            )
        }
    }

    private fun mockPrediction(request: PredictionRequest): PredictionResult {
        val rain1 = if (request.rainfallPrevious1d > 0) request.rainfallPrevious1d else request.rainfallMm
        val rain3 = if (request.rainfallPrevious3d > 0) request.rainfallPrevious3d else request.antecedentRain3d
        val rain7 = if (request.rainfallPrevious7d > 0) request.rainfallPrevious7d else (rain3 * 1.6)
        val slope = if (request.slope > 0) request.slope else request.slopeDeg

        val lithoModifier = when (request.lithologyGroup) {
            "Unconsolidated sediments" -> 1.25
            "Metamorphic rocks" -> 1.15
            "Siliciclastic sedimentary rocks" -> 1.10
            "Carbonate sedimentary rocks" -> 0.95
            "Acid plutonic rocks" -> 0.85
            else -> 1.0
        }

        val landCoverModifier = when (request.landCover) {
            "Bare/sparse vegetation" -> 1.30
            "Built-up" -> 1.20
            "Cropland" -> 1.10
            "Tree cover" -> 0.80
            else -> 1.0
        }

        val baseScore = (rain1 / 150.0 * 0.30 + rain3 / 280.0 * 0.25 + rain7 / 450.0 * 0.20 + slope / 55.0 * 0.25)
        val score = (baseScore * lithoModifier * landCoverModifier).coerceIn(0.02, 0.99)

        val level = when {
            score >= 0.75 -> "CRITICAL"
            score >= 0.50 -> "HIGH"
            score >= 0.28 -> "MODERATE"
            else -> "LOW"
        }
        return PredictionResult(
            riskLevel = level,
            probability = score,
            confidence = score,
            factors = mapOf(
                "land_cover: Built-up" to 0.1496,
                "rainfall_previous_7d" to 0.1307,
                "rainfall_previous_3d" to 0.1286,
                "land_cover: Cropland" to 0.0912,
                "elevation" to 0.0794,
                "land_cover: Tree cover" to 0.0564,
                "slope" to 0.0444,
                "rainfall_previous_1d" to 0.0387
            ),
            featureImportances = mapOf(
                "land_cover: Built-up" to 0.1496,
                "rainfall_previous_7d" to 0.1307,
                "rainfall_previous_3d" to 0.1286,
                "land_cover: Cropland" to 0.0912,
                "elevation" to 0.0794,
                "land_cover: Tree cover" to 0.0564,
                "slope" to 0.0444,
                "rainfall_previous_1d" to 0.0387
            ),
            recommendation = when (level) {
                "CRITICAL" -> "🚨 CRITICAL EVACUATION WARNING: High probability of slope failure and debris flow. Immediately evacuate downhill settlements."
                "HIGH" -> "⚠️ HIGH ALERT: Soil near saturation and slope shear stress elevated. Restrict night-time vehicular movement on mountain passes."
                "MODERATE" -> "⚡ MODERATE WATCH: Soil moisture accumulating. Monitor culverts and hill cuts."
                else -> "✅ NORMAL STATUS: Stable ground conditions. Slope safety factor within acceptable limits."
            },
            isMock = true,
            modelVersion = "Bhoochetak-Offline (Rule-Based Fallback)"
        )
    }
}

// ─── Weather Repository ────────────────────────────────────────────────────────

class WeatherRepositoryImpl @Inject constructor(
    private val api: WeatherApi
) : WeatherRepository {
    override suspend fun getWeatherForecast(
        latitude: Double,
        longitude: Double
    ): Result<WeatherForecast> = runCatching {
        val response = api.getWeatherForecast(latitude, longitude)
        val hourly = response.hourly
        val hourlyData = hourly.time.indices.map { i ->
            val rain = hourly.precipitation.getOrElse(i) { 0.0 }
            val hum = hourly.humidity.getOrElse(i) { 0.0 }
            val prob = hourly.precipitationProbability?.getOrNull(i)
                ?: if (rain > 0.0) minOf(95, (40 + (rain * 12)).toInt()) else if (hum > 70) ((hum - 60) * 1.5).toInt().coerceIn(0, 40) else 5
            val uv = hourly.uvIndex?.getOrNull(i) ?: run {
                val hourOfDay = try { hourly.time[i].substringAfter('T').take(2).toInt() } catch (e: Exception) { 12 }
                if (hourOfDay in 6..18) {
                    val peakDist = kotlin.math.abs(hourOfDay - 12)
                    maxOf(0.0, (7.0 - peakDist * 0.9) * (1.0 - (hum / 150.0).coerceIn(0.0, 0.7)))
                } else 0.0
            }
            val windDir = hourly.windDirection?.getOrNull(i) ?: 45.0
            val code = hourly.weatherCode?.getOrNull(i) ?: 0

            HourlyWeather(
                time = hourly.time[i],
                rainfallMm = rain,
                temperature = hourly.temperature.getOrElse(i) { 0.0 },
                humidity = hum,
                windSpeedKmh = hourly.windSpeed.getOrElse(i) { 0.0 },
                rainProbability = prob,
                uvIndex = uv,
                windDirectionDeg = windDir,
                weatherCode = code
            )
        }
        WeatherForecast(latitude = latitude, longitude = longitude, hourlyData = hourlyData)
    }
}

// ─── SOS Repository ────────────────────────────────────────────────────────────

class SOSRepositoryImpl @Inject constructor(
    private val source: SOSFirestoreSource,
    private val db: NERDatabase
) : SOSRepository {
    override suspend fun triggerSOS(sosAlert: SOSAlert): Result<Unit> = source.triggerSOS(sosAlert)
    override fun getAllSOSAlerts(): Flow<List<SOSAlert>> = source.getAllSOSAlerts()
    override suspend fun resolveSOSAlert(sosId: String): Result<Unit> = source.resolveSOSAlert(sosId)

    override suspend fun savePendingSOS(sosAlert: SOSAlert, sectorName: String, smsDispatched: Boolean): Long {
        val entity = PendingSOSEntity(
            uid = sosAlert.uid,
            name = sosAlert.name,
            latitude = sosAlert.latitude,
            longitude = sosAlert.longitude,
            sectorName = sectorName,
            message = sosAlert.message,
            triggeredAt = sosAlert.triggeredAt,
            isSynced = false,
            smsDispatched = smsDispatched
        )
        return db.pendingSOSDao().insertSOS(entity)
    }

    override suspend fun syncPendingSOS(): Result<Unit> = runCatching {
        val pending = db.pendingSOSDao().getUnsyncedSOSList()
        pending.forEach { entity ->
            val alert = SOSAlert(
                uid = entity.uid,
                name = entity.name,
                latitude = entity.latitude,
                longitude = entity.longitude,
                message = "${entity.message} [Offline Cellular/SMS Sync]",
                triggeredAt = entity.triggeredAt,
                isResolved = false
            )
            source.triggerSOS(alert).onSuccess {
                db.pendingSOSDao().markSynced(entity.localId)
            }
        }
    }
}

// ─── User Repository ──────────────────────────────────────────────────────────

class UserRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : UserRepository {

    private val prefs by lazy {
        context.getSharedPreferences("ner_user_prefs", Context.MODE_PRIVATE)
    }

    companion object {
        var activeDemoUser: User? = null

        fun isSessionActive(context: Context): Boolean {
            if (activeDemoUser != null) return true
            val sp = context.getSharedPreferences("ner_user_prefs", Context.MODE_PRIVATE)
            return !sp.getString("user_uid", null).isNullOrBlank()
        }
    }

    override suspend fun getCurrentUser(): User? {
        activeDemoUser?.let { return it }

        val cachedUid = prefs.getString("user_uid", null)
        if (!cachedUid.isNullOrBlank()) {
            val cachedUser = User(
                uid = cachedUid,
                name = prefs.getString("user_name", "") ?: "",
                email = prefs.getString("user_email", "") ?: "",
                role = runCatching {
                    UserRole.valueOf(prefs.getString("user_role", "CITIZEN") ?: "CITIZEN")
                }.getOrDefault(UserRole.CITIZEN),
                phone = prefs.getString("user_phone", "") ?: "",
                fcmToken = prefs.getString("user_fcm_token", "") ?: ""
            )
            activeDemoUser = cachedUser
            return cachedUser
        }

        val firebaseUser = auth.currentUser ?: return null
        return try {
            val doc = firestore.collection("users").document(firebaseUser.uid).get().await()
            val user = User(
                uid = firebaseUser.uid,
                name = doc.getString("name") ?: firebaseUser.displayName ?: "",
                email = firebaseUser.email ?: "",
                role = UserRole.valueOf(doc.getString("role") ?: "CITIZEN"),
                fcmToken = doc.getString("fcmToken") ?: ""
            )
            saveUser(user)
            user
        } catch (e: Exception) {
            User(
                uid = firebaseUser.uid,
                name = firebaseUser.displayName ?: "",
                email = firebaseUser.email ?: "",
                role = UserRole.CITIZEN
            )
        }
    }

    override suspend fun saveUser(user: User): Result<Unit> = runCatching {
        activeDemoUser = user
        prefs.edit()
            .putString("user_uid", user.uid)
            .putString("user_name", user.name)
            .putString("user_email", user.email)
            .putString("user_role", user.role.name)
            .putString("user_phone", user.phone)
            .putString("user_fcm_token", user.fcmToken)
            .apply()

        if (!user.uid.startsWith("demo_") && auth.currentUser != null) {
            try {
                kotlinx.coroutines.withTimeoutOrNull(2000L) {
                    val data = mapOf(
                        "name" to user.name,
                        "email" to user.email,
                        "role" to user.role.name,
                        "phone" to user.phone,
                        "fcmToken" to user.fcmToken
                    )
                    firestore.collection("users").document(user.uid).set(data).await()
                }
            } catch (_: Exception) {
                // Firestore sync is optional in demo/offline mode
            }
        }
        Unit
    }

    override suspend fun updateFcmToken(token: String): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid ?: activeDemoUser?.uid ?: return@runCatching
        try {
            firestore.collection("users").document(uid).update("fcmToken", token).await()
        } catch (_: Exception) {}
        Unit
    }

    override suspend fun signOut(): Result<Unit> = runCatching {
        activeDemoUser = null
        prefs.edit().clear().apply()
        try { auth.signOut() } catch (_: Exception) {}
        Unit
    }
}
