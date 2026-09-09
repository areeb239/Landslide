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

// ─── Database ─────────────────────────────────────────────────────────────────

@Database(
    entities = [PendingReportEntity::class],
    version = 1,
    exportSchema = false
)
abstract class NERDatabase : RoomDatabase() {
    abstract fun pendingReportDao(): PendingReportDao
}
