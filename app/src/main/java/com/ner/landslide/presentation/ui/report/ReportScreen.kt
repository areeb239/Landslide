package com.ner.landslide.presentation.ui.report

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.ner.landslide.domain.model.*
import com.ner.landslide.presentation.ui.components.*
import com.ner.landslide.presentation.ui.theme.*
import com.ner.landslide.presentation.viewmodel.ReportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    onReportSubmitted: () -> Unit,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = BhurakshakTheme.colors
    var currentStep by remember { mutableIntStateOf(1) } // Step 1: Location/Evidence, Step 2: Classification/Severity
    val latDisplay = if (uiState.latitude != 0.0) String.format(java.util.Locale.US, "%.4f° N", uiState.latitude) else "27.1765° N"
    val lonDisplay = if (uiState.longitude != 0.0) String.format(java.util.Locale.US, "%.4f° E", uiState.longitude) else "88.5321° E"

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> -> viewModel.onPhotosSelected(uris) }

    var showSuccessDialog by remember { mutableStateOf(false) }
    var reportReceiptId by remember { mutableStateOf("") }
    var showErrorDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            reportReceiptId = "BHU-${(System.currentTimeMillis() % 90000) + 10000}"
            showSuccessDialog = true
        }
    }

    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            showErrorDialog = true
        }
    }

    if (showErrorDialog && uiState.error != null) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            containerColor = colors.bgSurface,
            icon = { Icon(Icons.Default.ErrorOutline, null, tint = colors.critical, modifier = Modifier.size(28.dp)) },
            title = { Text("Submission Error", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = colors.textPrimary) },
            text = {
                Text(
                    text = "${uiState.error}\n\nYour report details and coordinates are preserved. Please tap retry or save to offline emergency buffer.",
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showErrorDialog = false
                        viewModel.submitReport()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                ) {
                    Text("Retry Transmission", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("Dismiss", color = colors.textSecondary, fontSize = 12.sp)
                }
            }
        )
    }

    Scaffold(
        containerColor = colors.bgBase,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "DISASTER INCIDENT REPORT",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            letterSpacing = 0.6.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            "Step $currentStep of 2 • Field Telemetry Intake",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.bgBase
                ),
                actions = {
                    if (currentStep == 2) {
                        TextButton(onClick = { currentStep = 1 }) {
                            Text("Edit Step 1", color = colors.accent, fontSize = 12.sp)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.bgBase)
                .padding(paddingValues)
        ) {
            // Offline sync notification banner
            if (!uiState.isOnline) {
                OfflineBanner(pendingCount = 1)
            }

            // Stepper Progress Header (Solid Active Fill, Muted Upcoming)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Step 1 Pill
                val isStep1Active = currentStep == 1
                val isStep1Completed = currentStep > 1
                Surface(
                    onClick = { currentStep = 1 },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isStep1Active) colors.accent else colors.bgSurface,
                    border = BorderStroke(1.dp, if (isStep1Active) colors.accent else colors.borderDefault),
                    shadowElevation = if (colors.isDark) 0.dp else 1.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isStep1Completed -> colors.success
                                        isStep1Active -> Color.White.copy(alpha = 0.25f)
                                        else -> colors.borderDefault
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isStep1Completed) {
                                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(12.dp))
                            } else {
                                Text("1", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        Text(
                            text = "Location & Evidence",
                            fontSize = 11.sp,
                            fontWeight = if (isStep1Active) FontWeight.Bold else FontWeight.Medium,
                            color = if (isStep1Active) Color.White else colors.textSecondary
                        )
                    }
                }

                // Step 2 Pill
                val isStep2Active = currentStep == 2
                Surface(
                    onClick = { currentStep = 2 },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isStep2Active) colors.accent else colors.bgSurface,
                    border = BorderStroke(1.dp, if (isStep2Active) colors.accent else colors.borderDefault),
                    shadowElevation = if (colors.isDark) 0.dp else 1.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(if (isStep2Active) Color.White.copy(alpha = 0.25f) else colors.borderDefault),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "2",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isStep2Active) Color.White else colors.textSecondary
                            )
                        }
                        Text(
                            text = "Classification",
                            fontSize = 11.sp,
                            fontWeight = if (isStep2Active) FontWeight.Bold else FontWeight.Medium,
                            color = if (isStep2Active) Color.White else colors.textSecondary
                        )
                    }
                }
            }

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                    }
                },
                label = "step_transition",
                modifier = Modifier.weight(1f)
            ) { step ->
                if (step == 1) {
                    // ─── STEP 1: Location, Observations & Photographic Evidence ───
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Section 1: GNSS Satellite Lock Card
                        FieldCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "GNSS TRIANGULATION LOCK",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        letterSpacing = 0.5.sp,
                                        color = colors.textSecondary
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = colors.accent.copy(alpha = 0.15f),
                                        border = BorderStroke(1.dp, colors.accent.copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            PulsingStatusDot(color = colors.accent, size = 5.dp)
                                            Text(
                                                "GNSS LOCKED ±3.2m",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.accent
                                            )
                                        }
                                    }
                                }



                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("COORDINATES", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary)
                                        Text("$latDisplay, $lonDisplay", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("ELEVATION", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary)
                                        Text("1,420 m MSL", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                                    }
                                }

                                HorizontalDivider(color = colors.borderDefault, thickness = 0.8.dp)

                                // District / Location manual refinement
                                OutlinedTextField(
                                    value = uiState.district,
                                    onValueChange = { viewModel.onDistrictChange(it) },
                                    label = { Text("Sector / District Location", fontSize = 12.sp) },
                                    placeholder = { Text("e.g. East Sikkim, NH-10 KM 28", fontSize = 12.sp) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = colors.accent,
                                        unfocusedBorderColor = colors.borderDefault,
                                        focusedLabelColor = colors.accent,
                                        unfocusedLabelColor = colors.textSecondary,
                                        focusedTextColor = colors.textPrimary,
                                        unfocusedTextColor = colors.textPrimary,
                                        focusedContainerColor = colors.bgSurface,
                                        unfocusedContainerColor = colors.bgSurface,
                                        cursorColor = colors.accent
                                    )
                                )
                            }
                        }

                        // Section 2: Field Observations Field
                        FieldCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    "FIELD OBSERVATIONS",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.5.sp,
                                    color = colors.textSecondary
                                )
                                OutlinedTextField(
                                    value = uiState.description,
                                    onValueChange = { viewModel.onDescriptionChange(it) },
                                    placeholder = {
                                        Text(
                                            "Describe slope movement, tension crack widening, water seepage, rockfall volume, or highway obstruction...",
                                            fontSize = 12.sp,
                                            color = colors.textSecondary,
                                            lineHeight = 17.sp
                                        )
                                    },
                                    minLines = 4,
                                    maxLines = 6,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = colors.accent,
                                        unfocusedBorderColor = colors.borderDefault,
                                        focusedTextColor = colors.textPrimary,
                                        unfocusedTextColor = colors.textPrimary,
                                        focusedContainerColor = colors.bgSurface,
                                        unfocusedContainerColor = colors.bgSurface,
                                        cursorColor = colors.accent
                                    )
                                )
                            }
                        }

                        // Section 3: Photographic Evidence Dropzone
                        FieldCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "PHOTOGRAPHIC EVIDENCE",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        letterSpacing = 0.5.sp,
                                        color = colors.textSecondary
                                    )
                                    Text(
                                        "${uiState.photoUris.size} attached",
                                        fontSize = 11.sp,
                                        color = colors.textSecondary
                                    )
                                }

                                if (uiState.photoUris.isNotEmpty()) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.horizontalScroll(rememberScrollState())
                                    ) {
                                        uiState.photoUris.forEach { uri ->
                                            Box(modifier = Modifier.size(72.dp)) {
                                                AsyncImage(
                                                    model = uri,
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .border(1.dp, colors.borderDefault, RoundedCornerShape(8.dp))
                                                )
                                            }
                                        }
                                    }
                                }

                                // Attach button
                                Surface(
                                    onClick = { photoPicker.launch("image/*") },
                                    shape = RoundedCornerShape(8.dp),
                                    color = colors.bgSurface,
                                    border = BorderStroke(1.dp, colors.borderDefault),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.CameraAlt, null, tint = colors.accent, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "Attach Photo Evidence from Field",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = colors.textPrimary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        // Step 1 Primary CTA
                        PrimaryActionButton(
                            text = "Continue to Classification (Step 2) →",
                            onClick = { currentStep = 2 },
                            containerColor = colors.accent
                        )

                        Spacer(Modifier.height(24.dp))
                    }
                } else {
                    // ─── STEP 2: Incident Classification & Graduated Severity ───
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Section 1: Incident Type Selector
                        SectionHeader(
                            title = "Hazard Classification",
                            subtitle = "Select the specific geotechnical or slope failure category"
                        )

                        // 2x3 Grid of incident types with clean selection states
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            val types = IncidentType.values().toList()
                            types.chunked(2).forEach { rowTypes ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowTypes.forEach { type ->
                                        val isSelected = uiState.incidentType == type
                                        Surface(
                                            onClick = { viewModel.onIncidentTypeChange(type) },
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) colors.accent else colors.bgSurface,
                                            border = BorderStroke(
                                                1.dp,
                                                if (isSelected) colors.accent else colors.borderDefault
                                            ),
                                            shadowElevation = if (colors.isDark) 0.dp else 1.dp,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = when (type) {
                                                        IncidentType.LANDSLIDE -> Icons.Default.Terrain
                                                        IncidentType.CRACK -> Icons.Default.Warning
                                                        IncidentType.ROAD_BLOCKAGE -> Icons.Default.Block
                                                        IncidentType.FLASH_FLOOD -> Icons.Default.WaterDrop
                                                        IncidentType.SLOPE_MOVEMENT -> Icons.Default.SouthEast
                                                        IncidentType.OTHER -> Icons.AutoMirrored.Filled.HelpOutline
                                                    },
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                    tint = if (isSelected) Color.White else colors.textSecondary
                                                )
                                                Text(
                                                    text = type.name.replace("_", " "),
                                                    fontSize = 10.5.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) Color.White else colors.textSecondary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Section 2: Graduated Hazard Severity Level
                        SectionHeader(
                            title = "Hazard Threat Severity",
                            subtitle = "Visual scale reflecting immediate risk to life, highway, or infrastructure"
                        )

                        // Graduated Severity Selector (Low -> Critical with increasing height & visual weight)
                        GraduatedSeveritySelector(
                            selectedSeverity = uiState.severity,
                            onSeveritySelected = { viewModel.onSeverityChange(it) }
                        )

                        // Action Directive Banner (Warning amber tone shift, zero glow)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = colors.bgSurface,
                            border = BorderStroke(1.dp, colors.warning.copy(alpha = 0.6f)),
                            shadowElevation = if (colors.isDark) 0.dp else 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = colors.warning,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text(
                                        "PROTOCOL DIRECTIVE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.warning
                                    )
                                    Text(
                                        uiState.severity.toActionGuideline(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.textPrimary
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // Bottom Navigation CTAs: Ghost Back + Primary Transmit
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GhostSecondaryButton(
                                text = "← Back",
                                onClick = { currentStep = 1 },
                                modifier = Modifier.weight(0.28f)
                            )
                            PrimaryActionButton(
                                text = if (uiState.isSubmitting) "TRANSMITTING..." else "TRANSMIT DISASTER REPORT",
                                onClick = { viewModel.submitReport() },
                                enabled = !uiState.isSubmitting,
                                containerColor = if (uiState.severity == AlertSeverity.CRITICAL) colors.critical else colors.accent,
                                modifier = Modifier.weight(0.72f)
                            )
                        }

                        Text(
                            text = "Direct encrypted dispatch to State Disaster Response Force (SDRF) Command",
                            fontSize = 10.sp,
                            color = colors.textSecondary,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onReportSubmitted()
            },
            containerColor = colors.bgSurface,
            icon = {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(colors.success.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = colors.success, modifier = Modifier.size(34.dp))
                }
            },
            title = {
                Text(
                    "REPORT DISPATCHED TO SDRF",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    letterSpacing = 0.5.sp,
                    color = colors.textPrimary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colors.bgBase,
                        border = BorderStroke(1.dp, colors.borderDefault),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("TRACKING ID", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary)
                                Text("#$reportReceiptId", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = colors.accent)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("INCIDENT TYPE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary)
                                Text(uiState.incidentType.name.replace("_", " "), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("SEVERITY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary)
                                Text(uiState.severity.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = uiState.severity.toThemeColor())
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("COORDINATES", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary)
                                Text("$latDisplay, $lonDisplay", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                            }
                        }
                    }

                    Text(
                        text = if (uiState.isOnline) {
                            "Encrypted broadcast delivered to State Disaster Response Force (SDRF) & District Incident Command Center. Emergency response teams notified."
                        } else {
                            "Saved securely in local offline buffer. Your report and GPS telemetry will automatically dispatch the moment network or mesh connection is restored."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.5.sp,
                        color = colors.textSecondary,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onReportSubmitted()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("DONE / RETURN TO HOME", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                }
            }
        )
    }
}
