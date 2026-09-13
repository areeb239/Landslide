package com.ner.landslide.presentation.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.ner.landslide.presentation.ui.theme.BhurakshakTheme
import com.ner.landslide.util.EmergencyContact
import com.ner.landslide.util.EmergencySmsHelper

@Composable
fun EmergencyContactsDialog(
    onDismissRequest: () -> Unit,
    onContactsUpdated: () -> Unit = {}
) {
    val context = LocalContext.current
    val colors = BhurakshakTheme.colors

    var contacts by remember {
        mutableStateOf(EmergencySmsHelper.getSavedContacts(context))
    }

    var hasSmsPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasSmsPermission = granted
    }

    var showAddCard by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var selectedRelation by remember { mutableStateOf("Family") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val relationOptions = listOf("Family", "Friend", "Neighbor", "SDRF / Rescue", "Other")

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(16.dp),
            color = colors.bgSurface,
            border = BorderStroke(1.dp, colors.borderDefault)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colors.critical.copy(alpha = 0.15f))
                                .border(1.dp, colors.critical, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.PermPhoneMsg,
                                contentDescription = null,
                                tint = colors.critical,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Emergency SOS Contacts",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Offline Cellular Distress Network",
                                fontSize = 10.5.sp,
                                color = colors.textSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textSecondary)
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Scrollable content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Cellular Explanation Banner
                    item {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = colors.accent.copy(alpha = 0.08f),
                            border = BorderStroke(0.8.dp, colors.accent.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.CellTower,
                                    contentDescription = null,
                                    tint = colors.accent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "When internet is unavailable, triggering SOS will send an emergency SMS with your live GPS coordinates & Google Maps pin to all saved numbers via cellular network.",
                                    fontSize = 10.5.sp,
                                    color = colors.textPrimary,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    // SMS Permission Status Banner
                    item {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (hasSmsPermission) colors.accent.copy(alpha = 0.1f) else colors.warning.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, if (hasSmsPermission) colors.accent.copy(alpha = 0.3f) else colors.warning.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        if (hasSmsPermission) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (hasSmsPermission) colors.accent else colors.warning,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Column {
                                        Text(
                                            text = if (hasSmsPermission) "Background SMS Active" else "SMS Permission Required",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (hasSmsPermission) colors.accent else colors.warning
                                        )
                                        Text(
                                            text = if (hasSmsPermission)
                                                "Distress beacons transmit automatically in background"
                                            else
                                                "Required for automatic offline message delivery",
                                            fontSize = 9.5.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                }

                                if (!hasSmsPermission) {
                                    Button(
                                        onClick = { permissionLauncher.launch(Manifest.permission.SEND_SMS) },
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = colors.warning),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("Grant", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }

                    // Pinned Default Official Emergency Responder
                    item {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = colors.bgBase,
                            border = BorderStroke(1.dp, colors.borderDefault),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(colors.critical.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "112",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp,
                                        color = colors.critical
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "112 — National Emergency Line",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textPrimary
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = colors.critical.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                "OFFICIAL",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Black,
                                                color = colors.critical,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "SDRF / NDRF Disaster Response • Always notified",
                                        fontSize = 10.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                        }
                    }

                    // Personal Contacts Section Header
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "PERSONAL CONTACTS (${contacts.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textSecondary,
                                letterSpacing = 0.6.sp
                            )

                            if (!showAddCard) {
                                TextButton(
                                    onClick = {
                                        showAddCard = true
                                        validationError = null
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp), tint = colors.accent)
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        "Add Number",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.accent
                                    )
                                }
                            }
                        }
                    }

                    // Add Contact Form Card (Expandable)
                    item {
                        AnimatedVisibility(
                            visible = showAddCard,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = colors.bgBase,
                                border = BorderStroke(1.2.dp, colors.accent.copy(alpha = 0.6f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Add Emergency Contact",
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.accent
                                        )
                                        IconButton(
                                            onClick = {
                                                showAddCard = false
                                                validationError = null
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp), tint = colors.textSecondary)
                                        }
                                    }

                                    // Name Field
                                    OutlinedTextField(
                                        value = nameInput,
                                        onValueChange = {
                                            nameInput = it
                                            validationError = null
                                        },
                                        label = { Text("Contact Name (e.g. Dad, Sister)", fontSize = 11.sp) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        textStyle = MaterialTheme.typography.bodySmall.copy(color = colors.textPrimary)
                                    )

                                    // Phone Field
                                    OutlinedTextField(
                                        value = phoneInput,
                                        onValueChange = {
                                            phoneInput = it
                                            validationError = null
                                        },
                                        label = { Text("Phone Number (e.g. +91 98765 43210)", fontSize = 11.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        textStyle = MaterialTheme.typography.bodySmall.copy(color = colors.textPrimary)
                                    )

                                    // Relationship Chips
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = "Relationship:",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = colors.textSecondary
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            relationOptions.forEach { rel ->
                                                val isSelected = selectedRelation == rel
                                                Surface(
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = if (isSelected) colors.accent else colors.bgSurface,
                                                    border = BorderStroke(1.dp, if (isSelected) colors.accent else colors.borderDefault),
                                                    modifier = Modifier.clickable { selectedRelation = rel }
                                                ) {
                                                    Text(
                                                        text = rel,
                                                        fontSize = 10.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) Color.White else colors.textPrimary,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    if (validationError != null) {
                                        Text(
                                            text = validationError ?: "",
                                            fontSize = 10.5.sp,
                                            color = colors.critical,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    // Save button
                                    Button(
                                        onClick = {
                                            val cleanName = nameInput.trim()
                                            val cleanPhone = phoneInput.trim()
                                            if (cleanName.isBlank()) {
                                                validationError = "Please enter a contact name"
                                                return@Button
                                            }
                                            val digitsOnly = cleanPhone.replace(Regex("[^0-9+]"), "")
                                            if (digitsOnly.length < 5) {
                                                validationError = "Please enter a valid phone number (min 5 digits)"
                                                return@Button
                                            }

                                            val newContact = EmergencyContact(
                                                name = cleanName,
                                                phoneNumber = cleanPhone,
                                                relation = selectedRelation
                                            )
                                            val updated = EmergencySmsHelper.addContact(context, newContact)
                                            contacts = updated
                                            onContactsUpdated()
                                            nameInput = ""
                                            phoneInput = ""
                                            showAddCard = false
                                            validationError = null
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                                        modifier = Modifier.fillMaxWidth().height(38.dp)
                                    ) {
                                        Icon(Icons.Default.Save, null, modifier = Modifier.size(14.dp), tint = Color.White)
                                        Spacer(Modifier.width(6.dp))
                                        Text("Save Emergency Contact", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }

                    // Empty state if no custom contacts
                    if (contacts.isEmpty() && !showAddCard) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = colors.bgBase,
                                border = BorderStroke(1.dp, colors.borderDefault),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.PersonAdd,
                                        contentDescription = null,
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Text(
                                        text = "No Personal Contacts Added",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "Add family members, neighbors, or colleagues to receive your live coordinates & distress beacon when offline.",
                                        fontSize = 10.5.sp,
                                        color = colors.textSecondary,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        lineHeight = 14.sp
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Button(
                                        onClick = {
                                            showAddCard = true
                                            validationError = null
                                        },
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp), tint = Color.White)
                                        Spacer(Modifier.width(4.dp))
                                        Text("Add First Contact", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }

                    // Render List of Saved Contacts
                    items(contacts, key = { it.id }) { contact ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = colors.bgBase,
                            border = BorderStroke(1.dp, colors.borderDefault),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(colors.accent.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = contact.name.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = colors.accent
                                        )
                                    }

                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = contact.name,
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.textPrimary
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = colors.accent.copy(alpha = 0.12f)
                                            ) {
                                                Text(
                                                    text = contact.relation,
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = colors.accent,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = contact.phoneNumber,
                                            fontSize = 11.sp,
                                            color = colors.textSecondary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        val updated = EmergencySmsHelper.removeContact(context, contact.id)
                                        contacts = updated
                                        onContactsUpdated()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = "Delete Contact",
                                        tint = colors.critical,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Bottom Done Button
                Button(
                    onClick = onDismissRequest,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.bgBase),
                    border = BorderStroke(1.dp, colors.borderDefault),
                    modifier = Modifier.fillMaxWidth().height(40.dp)
                ) {
                    Text(
                        text = "Done",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }
            }
        }
    }
}
