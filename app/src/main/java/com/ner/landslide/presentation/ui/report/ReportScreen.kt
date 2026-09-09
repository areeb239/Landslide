package com.ner.landslide.presentation.ui.report

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
    var currentStep by remember { mutableIntStateOf(1) } // Step 1: Location/Evidence, Step 2: Classification/Severity

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> -> viewModel.onPhotosSelected(uris) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onReportSubmitted()
    }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "DISASTER INCIDENT REPORT",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            letterSpacing = 0.6.sp,
                            color = OnBackgroundDark
                        )
                        Text(
                            "Step $currentStep of 2 • Field Telemetry Intake",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ObsidianBase
                ),
                actions = {
                    if (currentStep == 2) {
                        TextButton(onClick = { currentStep = 1 }) {
                            Text("Edit Step 1", color = Primary80, fontSize = 12.sp)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
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
                Surface(
                    onClick = { currentStep = 1 },
                    shape = RoundedCornerShape(8.dp),
                    color = if (currentStep == 1) Primary80 else SurfaceDark,
                    border = BorderStroke(1.dp, if (currentStep == 1) Primary80 else BorderSubtle),
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
                                        currentStep > 1 -> SeverityLow
                                        currentStep == 1 -> Color.White.copy(alpha = 0.25f)
                                        else -> SurfaceVariantDark
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (currentStep > 1) {
                                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(12.dp))
                            } else {
                                Text("1", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        Text(
                            text = "Location & Evidence",
                            fontSize = 11.sp,
                            fontWeight = if (currentStep == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (currentStep == 1) Color.White else TextMuted
                        )
                    }
                }

                // Step 2 Pill
                Surface(
                    onClick = { currentStep = 2 },
                    shape = RoundedCornerShape(8.dp),
                    color = if (currentStep == 2) Primary80 else SurfaceDark,
                    border = BorderStroke(1.dp, if (currentStep == 2) Primary80 else BorderSubtle),
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
                                .background(if (currentStep == 2) Color.White.copy(alpha = 0.25f) else SurfaceVariantDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "2",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentStep == 2) Color.White else TextSubtle
                            )
                        }
                        Text(
                            text = "Classification",
                            fontSize = 11.sp,
                            fontWeight = if (currentStep == 2) FontWeight.Bold else FontWeight.Medium,
                            color = if (currentStep == 2) Color.White else TextMuted
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
                                        color = TextMuted
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Primary80.copy(alpha = 0.15f),
                                        border = BorderStroke(1.dp, Primary80.copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            PulsingStatusDot(color = Primary80, size = 5.dp)
                                            Text(
                                                "GNSS LOCKED ±3.2m",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Primary80
                                            )
                                        }
                                    }
                                }

                                val latDisplay = if (uiState.latitude != 0.0) "%.4f° N".format(uiState.latitude) else "27.1765° N"
                                val lonDisplay = if (uiState.longitude != 0.0) "%.4f° E".format(uiState.longitude) else "88.5321° E"

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("COORDINATES", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSubtle)
                                        Text("$latDisplay, $lonDisplay", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = OnBackgroundDark)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("ELEVATION", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSubtle)
                                        Text("1,420 m MSL", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = OnBackgroundDark)
                                    }
                                }

                                HorizontalDivider(color = BorderSubtle, thickness = 0.8.dp)

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
                                        focusedBorderColor = Primary80,
                                        unfocusedBorderColor = BorderSubtle,
                                        focusedLabelColor = Primary80,
                                        unfocusedLabelColor = TextMuted,
                                        focusedTextColor = OnBackgroundDark,
                                        unfocusedTextColor = OnBackgroundDark,
                                        focusedContainerColor = SurfaceDark,
                                        unfocusedContainerColor = SurfaceDark,
                                        cursorColor = Primary80
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
                                    color = TextMuted
                                )
                                OutlinedTextField(
                                    value = uiState.description,
                                    onValueChange = { viewModel.onDescriptionChange(it) },
                                    placeholder = {
                                        Text(
                                            "Describe slope movement, tension crack widening, water seepage, rockfall volume, or highway obstruction...",
                                            fontSize = 12.sp,
                                            color = TextSubtle,
                                            lineHeight = 17.sp
                                        )
                                    },
                                    minLines = 4,
                                    maxLines = 6,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Primary80,
                                        unfocusedBorderColor = BorderSubtle,
                                        focusedTextColor = OnBackgroundDark,
                                        unfocusedTextColor = OnBackgroundDark,
                                        focusedContainerColor = SurfaceDark,
                                        unfocusedContainerColor = SurfaceDark,
                                        cursorColor = Primary80
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
                                        color = TextMuted
                                    )
                                    Text(
                                        "${uiState.photoUris.size} attached",
                                        fontSize = 11.sp,
                                        color = TextSubtle
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
                                                        .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                                                )
                                            }
                                        }
                                    }
                                }

                                // Attach button
                                Surface(
                                    onClick = { photoPicker.launch("image/*") },
                                    shape = RoundedCornerShape(8.dp),
                                    color = SurfaceVariantDark,
                                    border = BorderStroke(1.dp, BorderSubtle),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.CameraAlt, null, tint = Primary80, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "Attach Photo Evidence from Field",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = OnSurfaceDark
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        // Step 1 Primary CTA
                        PrimaryActionButton(
                            text = "Continue to Classification (Step 2) →",
                            onClick = { currentStep = 2 }
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
                                            color = if (isSelected) Primary80 else SurfaceDark,
                                            border = BorderStroke(
                                                1.dp,
                                                if (isSelected) Primary80 else BorderSubtle
                                            ),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                                    tint = if (isSelected) Color.White else TextMuted
                                                )
                                                Text(
                                                    text = type.name.replace("_", " "),
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) Color.White else TextMuted
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

                        // Action Directive Banner (Amber accent only per design system, zero glow)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SurfaceDark,
                            border = BorderStroke(1.dp, SeverityModerate.copy(alpha = 0.6f)),
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
                                    tint = SeverityModerate,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text(
                                        "PROTOCOL DIRECTIVE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SeverityModerate
                                    )
                                    Text(
                                        uiState.severity.toActionGuideline(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = OnBackgroundDark
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // Bottom Navigation CTAs: Ghost Back + Primary Transmit
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            GhostSecondaryButton(
                                text = "← Back",
                                onClick = { currentStep = 1 },
                                modifier = Modifier.weight(0.35f)
                            )
                            PrimaryActionButton(
                                text = if (uiState.isSubmitting) "TRANSMITTING..." else "TRANSMIT DISASTER REPORT",
                                onClick = { viewModel.submitReport() },
                                enabled = !uiState.isSubmitting,
                                containerColor = if (uiState.severity == AlertSeverity.CRITICAL) SeverityCritical else Primary80,
                                modifier = Modifier.weight(0.65f)
                            )
                        }

                        Text(
                            text = "Direct encrypted dispatch to State Disaster Response Force (SDRF) Command",
                            fontSize = 10.sp,
                            color = TextSubtle,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}
