package com.ner.landslide.data.repository

import android.content.Context
import com.ner.landslide.data.local.database.NERDatabase
import com.ner.landslide.data.local.database.PendingReportEntity
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
                    rainfallMm = request.rainfallMm,
                    slopeDeg = request.slopeDeg,
                    soilMoisturePct = request.soilMoisturePct,
                    antecedentRain3d = request.antecedentRain3d,
                    latitude = request.latitude,
                    longitude = request.longitude
                )
            )
            PredictionResult(
                riskLevel = dto.riskLevel,
                probability = dto.probability,
                confidence = dto.confidence,
                factors = dto.factors,
                recommendation = dto.recommendation,
                isMock = false
            )
        }.recoverCatching {
            // FastAPI not reachable — return mock response so demo never breaks
            mockPrediction(request)
        }

    private fun mockPrediction(request: PredictionRequest): PredictionResult {
        val score = (request.rainfallMm / 300.0 + request.slopeDeg / 90.0 +
                request.soilMoisturePct / 100.0) / 3.0
        val level = when {
            score > 0.75 -> "CRITICAL"
            score > 0.55 -> "HIGH"
            score > 0.35 -> "MODERATE"
            else -> "LOW"
        }
        return PredictionResult(
            riskLevel = level,
            probability = score.coerceIn(0.0, 1.0),
            confidence = 0.72,
            factors = mapOf(
                "rainfall" to request.rainfallMm / 300.0,
                "slope_angle" to request.slopeDeg / 90.0,
                "soil_moisture" to request.soilMoisturePct / 100.0,
                "antecedent_rain" to request.antecedentRain3d / 500.0
            ),
            recommendation = if (level == "CRITICAL" || level == "HIGH")
                "Immediate evacuation of vulnerable zones recommended."
            else "Continue monitoring. No immediate action required.",
            isMock = true
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
            HourlyWeather(
                time = hourly.time[i],
                rainfallMm = hourly.precipitation.getOrElse(i) { 0.0 },
                temperature = hourly.temperature.getOrElse(i) { 0.0 },
                humidity = hourly.humidity.getOrElse(i) { 0.0 },
                windSpeedKmh = hourly.windSpeed.getOrElse(i) { 0.0 }
            )
        }
        WeatherForecast(latitude = latitude, longitude = longitude, hourlyData = hourlyData)
    }
}

// ─── SOS Repository ────────────────────────────────────────────────────────────

class SOSRepositoryImpl @Inject constructor(
    private val source: SOSFirestoreSource
) : SOSRepository {
    override suspend fun triggerSOS(sosAlert: SOSAlert): Result<Unit> = source.triggerSOS(sosAlert)
    override fun getAllSOSAlerts(): Flow<List<SOSAlert>> = source.getAllSOSAlerts()
    override suspend fun resolveSOSAlert(sosId: String): Result<Unit> = source.resolveSOSAlert(sosId)
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
