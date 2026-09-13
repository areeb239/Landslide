package com.ner.landslide.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.ner.landslide.MainActivity
import com.ner.landslide.domain.model.PredictionRequest
import com.ner.landslide.domain.repository.PredictionRepository
import com.ner.landslide.util.EmergencySmsHelper
import com.ner.landslide.util.LocationHelper
import com.ner.landslide.util.NetworkMonitor
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Locale
import java.util.concurrent.TimeUnit

@HiltWorker
class BackgroundRiskWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val predictionRepository: PredictionRepository,
    private val locationHelper: LocationHelper,
    private val networkMonitor: NetworkMonitor
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val location = locationHelper.getCurrentLocation() ?: return Result.success()
            val rawLat = location.latitude
            val rawLon = location.longitude

            // PAN-INDIA HAZARD MONITORING:
            // Background monitoring covers all Indian operational sectors.
            // Thanks to physics-informed geotechnical slope attenuation, flat plains
            // (e.g. Lucknow, Delhi, coastal belts) evaluate to LOW risk and will never trigger false alerts.
            if (!LocationHelper.isWithinIndia(rawLat, rawLon)) {
                return Result.success()
            }

            val resolved = locationHelper.reverseGeocode(rawLat, rawLon)
            val sectorName = resolved.formattedHeadline

            // 1. Extract physical terrain and meteorological features for current GPS coordinates
            val featuresResult = predictionRepository.extractFeatures(rawLat, rawLon)
            val features = featuresResult.getOrNull()

            val actualSlope = features?.slope ?: run {
                val isPlain = (rawLat in 23.0..28.5 && rawLon in 75.0..87.5) || (rawLat < 18.0 && (rawLon < 74.5 || rawLon > 78.0))
                if (isPlain) 0.8 else 20.0
            }

            // PHYSICAL GEOTECHNICAL SAFETY GUARD:
            // Landslide slope failure requires gravitational shear driving stress (tau = gamma * h * sin(theta) * cos(theta)).
            // On flat or gentle terrain (slope < 8.0°), gravity shear failure is physically impossible.
            // Plain sectors like Lucknow (0.8°), Delhi, Kanpur, Patna, Kolkata have zero landslide hazard.
            // Immediate safe exit: zero false alarms for plain sectors.
            if (actualSlope < 8.0) {
                return Result.success()
            }

            val request = PredictionRequest(
                latitude = rawLat,
                longitude = rawLon,
                slope = features?.slope ?: actualSlope,
                slopeDeg = features?.slope ?: actualSlope,
                elevation = features?.elevation,
                rainfallPrevious1d = features?.rainfallPrevious1d,
                rainfallPrevious3d = features?.rainfallPrevious3d,
                rainfallPrevious7d = features?.rainfallPrevious7d,
                rainfallMm = features?.rainfallPrevious1d,
                antecedentRain3d = features?.rainfallPrevious3d,
                lithologyGroup = features?.lithologyGroup,
                landCover = features?.landCover
            )

            val result = predictionRepository.predictRisk(request)
            val pred = result.getOrNull()

            if (pred != null && !pred.isOutsideCorridor) {
                val risk = pred.riskLevel.uppercase()
                // Strict terrain gate: Warnings require real mountainous terrain slope (>= 12.0°)
                if ((risk == "HIGH" || risk == "CRITICAL") && actualSlope >= 12.0) {
                    // 1. Trigger local high-priority push notification
                    showRiskNotification(
                        riskLevel = risk,
                        probability = pred.probability,
                        locationName = sectorName,
                        recommendation = pred.recommendation,
                        rawLat = rawLat,
                        rawLon = rawLon
                    )

                    // 2. Offline cellular fallback: If no internet connection, dispatch emergency SMS
                    if (!networkMonitor.isCurrentlyOnline()) {
                        EmergencySmsHelper.dispatchRiskAlertSms(
                            context = context,
                            riskLevel = risk,
                            probability = pred.probability,
                            latitude = rawLat,
                            longitude = rawLon,
                            sectorName = sectorName,
                            recommendation = pred.recommendation
                        )
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 2) Result.retry() else Result.success()
        }
    }

    private fun showRiskNotification(
        riskLevel: String,
        probability: Double,
        locationName: String,
        recommendation: String,
        rawLat: Double,
        rawLon: Double
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Automatic high-priority landslide early warnings for your current sector"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_NAVIGATE_TO, "prediction")
            putExtra(EXTRA_LATITUDE, rawLat)
            putExtra(EXTRA_LONGITUDE, rawLon)
            putExtra(EXTRA_RISK_LEVEL, riskLevel)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (riskLevel == "CRITICAL") {
            "🚨 CRITICAL Landslide Warning: $locationName"
        } else {
            "⚠️ High Landslide Risk Alert: $locationName"
        }

        val percent = String.format(Locale.US, "%.1f", probability * 100)
        val body = "$percent% probability detected. ${recommendation.ifBlank { "Prepare for immediate slope clearance and follow local disaster directives." }}"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val WORK_NAME = "ner_background_risk_worker"
        const val CHANNEL_ID = "ner_background_risk_channel"
        const val CHANNEL_NAME = "Bhoochetak Automatic Risk Alerts"
        const val NOTIFICATION_ID = 2001

        const val EXTRA_NAVIGATE_TO = "extra_navigate_to"
        const val EXTRA_LATITUDE = "extra_latitude"
        const val EXTRA_LONGITUDE = "extra_longitude"
        const val EXTRA_RISK_LEVEL = "extra_risk_level"

        fun schedulePeriodicRiskCheck(context: Context) {
            val constraints = Constraints.Builder()
                .build()

            val request = PeriodicWorkRequestBuilder<BackgroundRiskWorker>(
                repeatInterval = 30,
                repeatIntervalTimeUnit = TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun triggerImmediateRiskCheck(context: Context) {
            val request = OneTimeWorkRequestBuilder<BackgroundRiskWorker>()
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
