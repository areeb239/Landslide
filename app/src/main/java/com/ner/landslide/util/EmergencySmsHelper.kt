package com.ner.landslide.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class EmergencyContact(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phoneNumber: String,
    val relation: String = "Family"
)

sealed class SmsDispatchResult {
    data object DirectSmsSent : SmsDispatchResult()
    data object IntentOpened : SmsDispatchResult()
    data class Failed(val reason: String) : SmsDispatchResult()
}

object EmergencySmsHelper {

    const val DEFAULT_EMERGENCY_NUMBER = "112"
    private const val PREFS_NAME = "ner_emergency_prefs"
    private const val KEY_CUSTOM_CONTACT = "custom_emergency_contact"
    private const val KEY_CONTACTS_LIST_JSON = "emergency_contacts_list_json"
    private val gson = Gson()

    fun getSavedContacts(context: Context): List<EmergencyContact> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_CONTACTS_LIST_JSON, null)
        if (!json.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<EmergencyContact>>() {}.type
                val list: List<EmergencyContact>? = gson.fromJson(json, type)
                if (list != null) return list
            } catch (e: Exception) {
                android.util.Log.e("EmergencySmsHelper", "Failed to parse saved contacts JSON", e)
            }
        }

        // Migrate legacy single contact if present
        val legacyContact = prefs.getString(KEY_CUSTOM_CONTACT, null)?.trim()
        if (!legacyContact.isNullOrBlank() && legacyContact != DEFAULT_EMERGENCY_NUMBER) {
            val migrated = listOf(EmergencyContact(name = "Primary Contact", phoneNumber = legacyContact, relation = "Family"))
            saveContacts(context, migrated)
            return migrated
        }
        return emptyList()
    }

    fun saveContacts(context: Context, contacts: List<EmergencyContact>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = gson.toJson(contacts)
        prefs.edit().putString(KEY_CONTACTS_LIST_JSON, json).apply()
    }

    fun addContact(context: Context, contact: EmergencyContact): List<EmergencyContact> {
        val current = getSavedContacts(context).toMutableList()
        current.add(contact)
        saveContacts(context, current)
        return current
    }

    fun removeContact(context: Context, contactId: String): List<EmergencyContact> {
        val updated = getSavedContacts(context).filterNot { it.id == contactId }
        saveContacts(context, updated)
        return updated
    }

    fun getEmergencyPhoneNumbers(context: Context): List<String> {
        val saved = getSavedContacts(context)
        val list = mutableListOf<String>()
        for (contact in saved) {
            val clean = contact.phoneNumber.trim()
            if (clean.isNotBlank() && clean !in list) {
                list.add(clean)
            }
        }
        if (DEFAULT_EMERGENCY_NUMBER !in list) {
            list.add(DEFAULT_EMERGENCY_NUMBER)
        }
        return list
    }

    // Backwards-compatible alias for existing callers
    fun getEmergencyContacts(context: Context): List<String> = getEmergencyPhoneNumbers(context)

    fun setCustomEmergencyContact(context: Context, phone: String?) {
        if (phone.isNullOrBlank()) return
        val contacts = getSavedContacts(context).toMutableList()
        if (contacts.none { it.phoneNumber.trim() == phone.trim() }) {
            contacts.add(EmergencyContact(name = "Emergency Contact", phoneNumber = phone.trim(), relation = "Family"))
            saveContacts(context, contacts)
        }
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
            append("Distress: Immediate landslide rescue needed! Offline distress beacon.")
        }
    }

    fun dispatchEmergencySms(
        context: Context,
        userName: String,
        userPhone: String?,
        latitude: Double,
        longitude: Double,
        sectorName: String,
        targetNumbers: List<String> = getEmergencyPhoneNumbers(context)
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

        android.util.Log.i("EmergencySmsHelper", "Dispatching emergency SMS. Targets: $targetNumbers, PermGranted: $hasSendSmsPerm")

        if (hasSendSmsPerm) {
            return try {
                val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.getSystemService(SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }

                val parts = smsManager.divideMessage(message)
                var sentCount = 0
                for (number in targetNumbers) {
                    val cleanNumber = number.trim()
                    if (cleanNumber.isNotBlank()) {
                        smsManager.sendMultipartTextMessage(cleanNumber, null, parts, null, null)
                        sentCount++
                    }
                }
                if (sentCount > 0) {
                    SmsDispatchResult.DirectSmsSent
                } else {
                    openSmsIntent(context, targetNumbers, message)
                }
            } catch (e: Exception) {
                android.util.Log.e("EmergencySmsHelper", "Direct background SMS dispatch failed, opening SMS client", e)
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
        val cleanNumbers = targetNumbers.map { it.trim() }.filter { it.isNotBlank() }
        if (cleanNumbers.isEmpty()) {
            return SmsDispatchResult.Failed("No valid recipient phone numbers configured")
        }

        // Prioritize actual user contact if available, or 112
        val primaryContact = cleanNumbers.firstOrNull { it != DEFAULT_EMERGENCY_NUMBER } ?: cleanNumbers.first()

        return try {
            val separator = if (Build.MANUFACTURER.contains("samsung", ignoreCase = true)) "," else ";"
            val recipients = cleanNumbers.joinToString(separator = separator)
            val uri = Uri.parse("smsto:$recipients")
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", message)
                putExtra(Intent.EXTRA_TEXT, message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            context.startActivity(intent)
            SmsDispatchResult.IntentOpened
        } catch (e: Exception) {
            // Fallback to primary single recipient directly
            try {
                val uri = Uri.parse("smsto:$primaryContact")
                val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                    putExtra("sms_body", message)
                    putExtra(Intent.EXTRA_TEXT, message)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                context.startActivity(intent)
                SmsDispatchResult.IntentOpened
            } catch (ex: Exception) {
                try {
                    val uri = Uri.parse("sms:$primaryContact")
                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                        putExtra("sms_body", message)
                        putExtra(Intent.EXTRA_TEXT, message)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    context.startActivity(intent)
                    SmsDispatchResult.IntentOpened
                } catch (finalEx: Exception) {
                    SmsDispatchResult.Failed(finalEx.message ?: "Could not open SMS application")
                }
            }
        }
    }

    fun buildRiskAlertMessage(
        riskLevel: String,
        probability: Double,
        latitude: Double,
        longitude: Double,
        sectorName: String,
        recommendation: String = "",
        timestamp: Long = System.currentTimeMillis()
    ): String {
        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
        val timeStr = sdf.format(Date(timestamp))
        val mapsUrl = "https://maps.google.com/?q=${String.format(Locale.US, "%.6f,%.6f", latitude, longitude)}"

        return buildString {
            append("BHOOCHETAK — LANDSLIDE ALERT\n")
            append("Hazard Level: ${riskLevel.uppercase()} (${String.format(Locale.US, "%.1f", probability * 100)}% Risk)\n")
            append("Sector: ${sectorName.ifBlank { "Northeast India" }}\n")
            append("Coordinates: ${String.format(Locale.US, "%.5f, %.5f", latitude, longitude)}\n")
            append("Map: $mapsUrl\n")
            append("Time: $timeStr\n")
            if (recommendation.isNotBlank()) {
                append("Directive: $recommendation\n")
            }
            append("Automated alert sent via offline cellular fallback.")
        }
    }

    fun dispatchRiskAlertSms(
        context: Context,
        riskLevel: String,
        probability: Double,
        latitude: Double,
        longitude: Double,
        sectorName: String,
        recommendation: String = "",
        targetNumbers: List<String> = getEmergencyPhoneNumbers(context)
    ): SmsDispatchResult {
        val message = buildRiskAlertMessage(
            riskLevel = riskLevel,
            probability = probability,
            latitude = latitude,
            longitude = longitude,
            sectorName = sectorName,
            recommendation = recommendation
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
                    val cleanNumber = number.trim()
                    if (cleanNumber.isNotBlank()) {
                        smsManager.sendMultipartTextMessage(cleanNumber, null, parts, null, null)
                    }
                }
                SmsDispatchResult.DirectSmsSent
            } catch (e: Exception) {
                SmsDispatchResult.Failed(e.message ?: "SMS dispatch failed")
            }
        } else {
            return SmsDispatchResult.Failed("SEND_SMS permission not granted")
        }
    }
}

