package com.ner.landslide.domain.usecase

import com.ner.landslide.domain.model.*
import com.ner.landslide.domain.repository.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActiveAlertsUseCase @Inject constructor(
    private val repo: AlertRepository
) {
    operator fun invoke(): Flow<List<Alert>> = repo.getActiveAlerts()
}

class BroadcastAlertUseCase @Inject constructor(
    private val repo: AlertRepository
) {
    suspend operator fun invoke(alert: Alert): Result<Unit> = repo.broadcastAlert(alert)
}

class SubmitReportUseCase @Inject constructor(
    private val repo: ReportRepository
) {
    suspend operator fun invoke(report: IncidentReport): Result<Unit> = repo.submitReport(report)
}

class GetAllReportsUseCase @Inject constructor(
    private val repo: ReportRepository
) {
    operator fun invoke(): Flow<List<IncidentReport>> = repo.getAllReports()
}

class SyncOfflineReportsUseCase @Inject constructor(
    private val repo: ReportRepository
) {
    suspend operator fun invoke(): Result<Unit> = repo.syncPendingReports()
}

class GetRiskZonesUseCase @Inject constructor(
    private val repo: RiskZoneRepository
) {
    operator fun invoke(): Flow<List<RiskZone>> = repo.getRiskZones()
}

class GetRoadSegmentsUseCase @Inject constructor(
    private val repo: RiskZoneRepository
) {
    operator fun invoke(): Flow<List<RoadSegment>> = repo.getRoadSegments()
}

class PredictRiskUseCase @Inject constructor(
    private val repo: PredictionRepository
) {
    suspend operator fun invoke(request: PredictionRequest): Result<PredictionResult> =
        repo.predictRisk(request)
}

class ExtractFeaturesUseCase @Inject constructor(
    private val repo: PredictionRepository
) {
    suspend operator fun invoke(lat: Double, lon: Double, date: String? = null): Result<FeatureExtractionResult> =
        repo.extractFeatures(lat, lon, date)
}

class GetWeatherForecastUseCase @Inject constructor(
    private val repo: WeatherRepository
) {
    suspend operator fun invoke(lat: Double, lng: Double): Result<WeatherForecast> =
        repo.getWeatherForecast(lat, lng)
}

class TriggerSOSUseCase @Inject constructor(
    private val repo: SOSRepository
) {
    suspend operator fun invoke(sos: SOSAlert): Result<Unit> = repo.triggerSOS(sos)
}

class GetAllSOSAlertsUseCase @Inject constructor(
    private val repo: SOSRepository
) {
    operator fun invoke(): Flow<List<SOSAlert>> = repo.getAllSOSAlerts()
}

class ResolveSOSUseCase @Inject constructor(
    private val repo: SOSRepository
) {
    suspend operator fun invoke(sosId: String): Result<Unit> = repo.resolveSOSAlert(sosId)
}

class GetCurrentUserUseCase @Inject constructor(
    private val repo: UserRepository
) {
    suspend operator fun invoke(): User? = repo.getCurrentUser()
}
