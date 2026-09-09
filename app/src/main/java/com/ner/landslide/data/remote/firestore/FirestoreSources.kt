package com.ner.landslide.data.remote.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ner.landslide.domain.model.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AlertFirestoreSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        val defaultAlerts = listOf(
            Alert(
                id = "alert_nh10",
                title = "Severe Slope Instability Warning - NH-10",
                description = "High rainfall detected in Sevoke-Kalimpong section. Active debris slips between 29th Mile and Teesta Bazar. Nighttime transit restricted.",
                severity = AlertSeverity.HIGH,
                affectedDistrict = "Kalimpong & Darjeeling",
                affectedVillages = listOf("Sevoke", "29th Mile", "Teesta Bazar"),
                latitude = 27.1765,
                longitude = 88.5321,
                issuedAt = System.currentTimeMillis() - 1800000L,
                isActive = true
            ),
            Alert(
                id = "alert_dzongu",
                title = "Critical Landslide Watch - Dzongu Valley",
                description = "Pore water pressure sensors exceeding threshold (18.5 kPa). Soil saturation at 86%. Debris flow warning in effect.",
                severity = AlertSeverity.CRITICAL,
                affectedDistrict = "North Sikkim",
                affectedVillages = listOf("Passingdang", "Lingzya", "Sakyong"),
                latitude = 27.5210,
                longitude = 88.5412,
                issuedAt = System.currentTimeMillis() - 3600000L,
                isActive = true
            ),
            Alert(
                id = "alert_haflong",
                title = "Moderate Risk Advisory - Haflong Corridor",
                description = "Antecedent 3-day rainfall at 124 mm. Slope cutting zones along Lumding-Badarpur section actively monitored.",
                severity = AlertSeverity.MODERATE,
                affectedDistrict = "Dima Hasao",
                affectedVillages = listOf("Haflong", "Jatinga", "Mahur"),
                latitude = 25.1824,
                longitude = 93.0182,
                issuedAt = System.currentTimeMillis() - 7200000L,
                isActive = true
            )
        )
    }

    fun getActiveAlerts(): Flow<List<Alert>> = callbackFlow {
        // Emit initial defaults immediately so UI is never stuck loading
        trySend(defaultAlerts)
        val listener = firestore.collection("alerts")
            .whereEqualTo("isActive", true)
            .orderBy("issuedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(defaultAlerts)
                    return@addSnapshotListener
                }
                val alerts = snapshot.documents.mapNotNull { doc ->
                    try {
                        Alert(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            description = doc.getString("description") ?: "",
                            severity = AlertSeverity.valueOf(
                                doc.getString("severity") ?: "LOW"
                            ),
                            affectedDistrict = doc.getString("affectedDistrict") ?: "",
                            affectedVillages = (doc.get("affectedVillages") as? List<*>)
                                ?.filterIsInstance<String>() ?: emptyList(),
                            latitude = doc.getDouble("latitude") ?: 0.0,
                            longitude = doc.getDouble("longitude") ?: 0.0,
                            issuedAt = doc.getLong("issuedAt") ?: 0L,
                            isActive = doc.getBoolean("isActive") ?: true
                        )
                    } catch (e: Exception) { null }
                }
                trySend(if (alerts.isEmpty()) defaultAlerts else alerts)
            }
        awaitClose { listener.remove() }
    }

    suspend fun broadcastAlert(alert: Alert): Result<Unit> = runCatching {
        val data = mapOf(
            "title" to alert.title,
            "description" to alert.description,
            "severity" to alert.severity.name,
            "affectedDistrict" to alert.affectedDistrict,
            "affectedVillages" to alert.affectedVillages,
            "latitude" to alert.latitude,
            "longitude" to alert.longitude,
            "issuedAt" to System.currentTimeMillis(),
            "isActive" to true
        )
        kotlinx.coroutines.withTimeoutOrNull(2500L) {
            firestore.collection("alerts").add(data).await()
        }
        Unit
    }
}

class ReportFirestoreSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun getAllReports(): Flow<List<IncidentReport>> = callbackFlow {
        val listener = firestore.collection("reports")
            .orderBy("reportedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val reports = snapshot.documents.mapNotNull { doc ->
                    try {
                        IncidentReport(
                            id = doc.id,
                            reporterUid = doc.getString("reporterUid") ?: "",
                            reporterName = doc.getString("reporterName") ?: "",
                            incidentType = IncidentType.valueOf(
                                doc.getString("incidentType") ?: "OTHER"
                            ),
                            severity = AlertSeverity.valueOf(
                                doc.getString("severity") ?: "LOW"
                            ),
                            description = doc.getString("description") ?: "",
                            latitude = doc.getDouble("latitude") ?: 0.0,
                            longitude = doc.getDouble("longitude") ?: 0.0,
                            photoUrls = (doc.get("photoUrls") as? List<*>)
                                ?.filterIsInstance<String>() ?: emptyList(),
                            videoUrl = doc.getString("videoUrl") ?: "",
                            district = doc.getString("district") ?: "",
                            village = doc.getString("village") ?: "",
                            reportedAt = doc.getLong("reportedAt") ?: 0L,
                            isSynced = true
                        )
                    } catch (e: Exception) { null }
                }
                trySend(reports)
            }
        awaitClose { listener.remove() }
    }

    suspend fun submitReport(report: IncidentReport): Result<Unit> = runCatching {
        val data = mapOf(
            "reporterUid" to report.reporterUid,
            "reporterName" to report.reporterName,
            "incidentType" to report.incidentType.name,
            "severity" to report.severity.name,
            "description" to report.description,
            "latitude" to report.latitude,
            "longitude" to report.longitude,
            "photoUrls" to report.photoUrls,
            "videoUrl" to report.videoUrl,
            "district" to report.district,
            "village" to report.village,
            "reportedAt" to report.reportedAt
        )
        firestore.collection("reports").add(data).await()
        Unit
    }
}

class SOSFirestoreSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun getAllSOSAlerts(): Flow<List<SOSAlert>> = callbackFlow {
        val listener = firestore.collection("sos_alerts")
            .whereEqualTo("isResolved", false)
            .orderBy("triggeredAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val sos = snapshot.documents.mapNotNull { doc ->
                    try {
                        SOSAlert(
                            id = doc.id,
                            uid = doc.getString("uid") ?: "",
                            name = doc.getString("name") ?: "",
                            latitude = doc.getDouble("latitude") ?: 0.0,
                            longitude = doc.getDouble("longitude") ?: 0.0,
                            message = doc.getString("message") ?: "SOS — Need Help!",
                            triggeredAt = doc.getLong("triggeredAt") ?: 0L,
                            isResolved = doc.getBoolean("isResolved") ?: false
                        )
                    } catch (e: Exception) { null }
                }
                trySend(sos)
            }
        awaitClose { listener.remove() }
    }

    suspend fun triggerSOS(sos: SOSAlert): Result<Unit> = runCatching {
        val data = mapOf(
            "uid" to sos.uid,
            "name" to sos.name,
            "latitude" to sos.latitude,
            "longitude" to sos.longitude,
            "message" to sos.message,
            "triggeredAt" to System.currentTimeMillis(),
            "isResolved" to false
        )
        kotlinx.coroutines.withTimeoutOrNull(2500L) {
            firestore.collection("sos_alerts").document(sos.uid).set(data).await()
        }
        Unit
    }

    suspend fun resolveSOSAlert(sosId: String): Result<Unit> = runCatching {
        kotlinx.coroutines.withTimeoutOrNull(2500L) {
            firestore.collection("sos_alerts").document(sosId)
                .update("isResolved", true).await()
        }
        Unit
    }
}

class RiskZoneFirestoreSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        val defaultZones = listOf(
            RiskZone(
                id = "zone_dzongu",
                name = "Dzongu High Threat Zone",
                severity = AlertSeverity.CRITICAL,
                polygonPoints = listOf(
                    LatLng(27.510, 88.520),
                    LatLng(27.535, 88.530),
                    LatLng(27.530, 88.560),
                    LatLng(27.505, 88.545)
                ),
                district = "North Sikkim",
                lastUpdated = System.currentTimeMillis()
            ),
            RiskZone(
                id = "zone_sevoke",
                name = "Sevoke - Kalimpong Escarpment",
                severity = AlertSeverity.HIGH,
                polygonPoints = listOf(
                    LatLng(27.160, 88.510),
                    LatLng(27.190, 88.525),
                    LatLng(27.185, 88.555),
                    LatLng(27.155, 88.540)
                ),
                district = "Kalimpong",
                lastUpdated = System.currentTimeMillis()
            ),
            RiskZone(
                id = "zone_haflong",
                name = "Haflong Dima Hasao Hill Slopes",
                severity = AlertSeverity.MODERATE,
                polygonPoints = listOf(
                    LatLng(25.160, 93.000),
                    LatLng(25.200, 93.015),
                    LatLng(25.195, 93.040),
                    LatLng(25.155, 93.025)
                ),
                district = "Dima Hasao",
                lastUpdated = System.currentTimeMillis()
            )
        )

        val defaultRoads = listOf(
            RoadSegment(
                id = "road_nh10",
                name = "NH-10 Sevoke to Gangtok (29th Mile)",
                status = RoadStatus.BLOCKED,
                points = listOf(
                    LatLng(27.165, 88.520),
                    LatLng(27.1765, 88.5321),
                    LatLng(27.195, 88.545)
                ),
                blockageReason = "Debris avalanche at 29th Mile",
                reportedAt = System.currentTimeMillis()
            ),
            RoadSegment(
                id = "road_nh29",
                name = "NH-29 Kohima - Dimapur Corridor",
                status = RoadStatus.PARTIALLY_BLOCKED,
                points = listOf(
                    LatLng(25.660, 94.090),
                    LatLng(25.6751, 94.1086),
                    LatLng(25.690, 94.125)
                ),
                blockageReason = "Single-lane traffic due to mud slide",
                reportedAt = System.currentTimeMillis()
            )
        )
    }

    fun getRiskZones(): Flow<List<RiskZone>> = callbackFlow {
        trySend(defaultZones)
        val listener = firestore.collection("risk_zones")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(defaultZones)
                    return@addSnapshotListener
                }
                val zones = snapshot.documents.mapNotNull { doc ->
                    try {
                        @Suppress("UNCHECKED_CAST")
                        val rawPoints = doc.get("polygonPoints") as? List<Map<String, Double>>
                        val points = rawPoints?.map {
                            LatLng(it["latitude"] ?: 0.0, it["longitude"] ?: 0.0)
                        } ?: emptyList()
                        RiskZone(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            severity = AlertSeverity.valueOf(
                                doc.getString("severity") ?: "LOW"
                            ),
                            polygonPoints = points,
                            district = doc.getString("district") ?: "",
                            lastUpdated = doc.getLong("lastUpdated") ?: 0L
                        )
                    } catch (e: Exception) { null }
                }
                trySend(if (zones.isEmpty()) defaultZones else zones)
            }
        awaitClose { listener.remove() }
    }

    fun getRoadSegments(): Flow<List<RoadSegment>> = callbackFlow {
        trySend(defaultRoads)
        val listener = firestore.collection("road_segments")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(defaultRoads)
                    return@addSnapshotListener
                }
                val segments = snapshot.documents.mapNotNull { doc ->
                    try {
                        @Suppress("UNCHECKED_CAST")
                        val rawPoints = doc.get("points") as? List<Map<String, Double>>
                        val points = rawPoints?.map {
                            LatLng(it["latitude"] ?: 0.0, it["longitude"] ?: 0.0)
                        } ?: emptyList()
                        RoadSegment(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            status = RoadStatus.valueOf(
                                doc.getString("status") ?: "UNKNOWN"
                            ),
                            points = points,
                            blockageReason = doc.getString("blockageReason") ?: "",
                            reportedAt = doc.getLong("reportedAt") ?: 0L
                        )
                    } catch (e: Exception) { null }
                }
                trySend(if (segments.isEmpty()) defaultRoads else segments)
            }
        awaitClose { listener.remove() }
    }
}
