package com.ner.landslide.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class SmsDispatchResult {
    data object DirectSmsSent : SmsDispatchResult()
    data object IntentOpened : SmsDispatchResult()
    data class Failed(val reason: String) : SmsDispatchResult()
}

object EmergencySmsHelper {

    const val DEFAULT_EMERGENCY_NUMBER = "112"
    private const val PREFS_NAME = "ner_emergency_prefs"
    private const val KEY_CUSTOM_CONTACT = "custom_emergency_contact"

    fun getEmergencyContacts(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val custom = prefs.getString(KEY_CUSTOM_CONTACT, null)?.trim()
        val list = mutableListOf(DEFAULT_EMERGENCY_NUMBER)
        if (!custom.isNullOrBlank() && custom != DEFAULT_EMERGENCY_NUMBER) {
            list.add(custom)
        }
        return list
    }

    fun setCustomEmergencyContact(context: Context, phone: String?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CUSTOM_CONTACT, phone?.trim()).apply()
    }

    fun buildEmergencyMessage(
        userName: String,
        userPhone: String?,
        latitude: Double,
        longitude: Double,
        sectorName: String,
        timestamp: Long = System.currentTimeMillis()
    ): String {
        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
        val timeStr = sdf.format(Date(timestamp))
        val mapsUrl = "https://maps.google.com/?q=${String.format(Locale.US, "%.6f,%.6f", latitude, longitude)}"

        return buildString {
            append("EMERGENCY SOS — BHOOCHETAK\n")
            append("Victim: ${userName.ifBlank { "Citizen" }}\n")
            if (!userPhone.isNullOrBlank()) {
                append("Phone: $userPhone\n")
            }
            append("Sector: ${sectorName.ifBlank { "Northeast India" }}\n")
            append("Coordinates: ${String.format(Locale.US, "%.5f, %.5f", latitude, longitude)}\n")
            append("Live Map: $mapsUrl\n")
            append("Time: $timeStr\n")
            append("Distress: Immediate landslide rescue needed!")
        }
    }

    fun dispatchEmergencySms(
        context: Context,
        userName: String,
        userPhone: String?,
        latitude: Double,
        longitude: Double,
        sectorName: String,
        targetNumbers: List<String> = getEmergencyContacts(context)
    ): SmsDispatchResult {
        val message = buildEmergencyMessage(
            userName = userName,
            userPhone = userPhone,
            latitude = latitude,
            longitude = longitude,
            sectorName = sectorName
        )

        val hasSendSmsPerm = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        if (hasSendSmsPerm) {
            return try {
                val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.getSystemService(SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }

                val parts = smsManager.divideMessage(message)
                for (number in targetNumbers) {
                    smsManager.sendMultipartTextMessage(number, null, parts, null, null)
                }
                SmsDispatchResult.DirectSmsSent
            } catch (e: Exception) {
                // Fall back to SMS Intent if direct background dispatch fails
                openSmsIntent(context, targetNumbers, message)
            }
        } else {
            return openSmsIntent(context, targetNumbers, message)
        }
    }

    fun openSmsIntent(
        context: Context,
        targetNumbers: List<String>,
        message: String
    ): SmsDispatchResult {
        return try {
            val primaryNumber = targetNumbers.firstOrNull() ?: DEFAULT_EMERGENCY_NUMBER
            val uri = Uri.parse("smsto:$primaryNumber")
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", message)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            SmsDispatchResult.IntentOpened
        } catch (e: Exception) {
            SmsDispatchResult.Failed(e.message ?: "Could not open SMS application")
        }
    }
}
