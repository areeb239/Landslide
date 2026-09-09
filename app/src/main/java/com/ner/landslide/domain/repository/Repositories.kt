package com.ner.landslide.domain.repository

import com.ner.landslide.domain.model.*
import kotlinx.coroutines.flow.Flow

interface AlertRepository {
    fun getActiveAlerts(): Flow<List<Alert>>
    suspend fun broadcastAlert(alert: Alert): Result<Unit>
}

interface ReportRepository {
    suspend fun submitReport(report: IncidentReport): Result<Unit>
    fun getAllReports(): Flow<List<IncidentReport>>
    fun getPendingOfflineReports(): Flow<List<IncidentReport>>
    suspend fun syncPendingReports(): Result<Unit>
}

interface RiskZoneRepository {
    fun getRiskZones(): Flow<List<RiskZone>>
    fun getRoadSegments(): Flow<List<RoadSegment>>
}

interface PredictionRepository {
    suspend fun predictRisk(request: PredictionRequest): Result<PredictionResult>
}

interface WeatherRepository {
    suspend fun getWeatherForecast(latitude: Double, longitude: Double): Result<WeatherForecast>
}

interface UserRepository {
    suspend fun getCurrentUser(): User?
    suspend fun saveUser(user: User): Result<Unit>
    suspend fun updateFcmToken(token: String): Result<Unit>
    suspend fun signOut(): Result<Unit> = Result.success(Unit)
}

interface SOSRepository {
    suspend fun triggerSOS(sosAlert: SOSAlert): Result<Unit>
    fun getAllSOSAlerts(): Flow<List<SOSAlert>>
    suspend fun resolveSOSAlert(sosId: String): Result<Unit>
}
