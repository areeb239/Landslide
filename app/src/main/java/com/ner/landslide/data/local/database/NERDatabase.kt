package com.ner.landslide.data.local.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ─── Entity ───────────────────────────────────────────────────────────────────

@Entity(tableName = "pending_reports")
data class PendingReportEntity(
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,
    val reporterUid: String,
    val reporterName: String,
    val incidentType: String,
    val severity: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val photoUrlsJson: String = "[]",   // JSON array of local file URIs
    val videoUrl: String = "",
    val district: String = "",
    val village: String = "",
    val reportedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

@Entity(tableName = "pending_sos_alerts")
data class PendingSOSEntity(
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,
    val uid: String,
    val name: String,
    val phone: String = "",
    val latitude: Double,
    val longitude: Double,
    val sectorName: String = "",
    val message: String = "SOS — Need Help!",
    val triggeredAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val smsDispatched: Boolean = false
)

// ─── DAO ──────────────────────────────────────────────────────────────────────

@Dao
interface PendingReportDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: PendingReportEntity): Long

    @Query("SELECT * FROM pending_reports WHERE isSynced = 0 ORDER BY reportedAt DESC")
    fun getUnsyncedReports(): Flow<List<PendingReportEntity>>

    @Query("SELECT * FROM pending_reports ORDER BY reportedAt DESC")
    fun getAllReports(): Flow<List<PendingReportEntity>>

    @Query("UPDATE pending_reports SET isSynced = 1 WHERE localId = :localId")
    suspend fun markSynced(localId: Int)

    @Query("DELETE FROM pending_reports WHERE isSynced = 1")
    suspend fun deleteSyncedReports()
}

@Dao
interface PendingSOSDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSOS(sos: PendingSOSEntity): Long

    @Query("SELECT * FROM pending_sos_alerts WHERE isSynced = 0 ORDER BY triggeredAt DESC")
    fun getUnsyncedSOS(): Flow<List<PendingSOSEntity>>

    @Query("SELECT * FROM pending_sos_alerts WHERE isSynced = 0 ORDER BY triggeredAt DESC")
    suspend fun getUnsyncedSOSList(): List<PendingSOSEntity>

    @Query("UPDATE pending_sos_alerts SET isSynced = 1 WHERE localId = :localId")
    suspend fun markSynced(localId: Int)

    @Query("DELETE FROM pending_sos_alerts WHERE isSynced = 1")
    suspend fun deleteSyncedSOS()
}

// ─── Database ─────────────────────────────────────────────────────────────────

@Database(
    entities = [PendingReportEntity::class, PendingSOSEntity::class],
    version = 2,
    exportSchema = false
)
abstract class NERDatabase : RoomDatabase() {
    abstract fun pendingReportDao(): PendingReportDao
    abstract fun pendingSOSDao(): PendingSOSDao
}
