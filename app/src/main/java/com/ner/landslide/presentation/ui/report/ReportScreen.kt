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
import androidx.compose.ui.text.style.TextAlign
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
    val strings = LocalAppStrings.current
    val context = androidx.compose.ui.platform.LocalContext.current
    var currentStep by remember { mutableIntStateOf(1) } // Step 1: Location & Details, Step 2: Danger Level

    // Step 1 Validation state
    val hasValidLocation = (uiState.latitude != 0.0 && uiState.longitude != 0.0) || uiState.district.trim().isNotBlank()
    val hasValidDescription = uiState.description.trim().length >= 5
    val isStep1Valid = hasValidLocation && hasValidDescription
    var showStep1Errors by remember { mutableStateOf(false) }

    var showPermissionRationale by remember { mutableStateOf(false) }
    var showGpsDialog by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            if (!viewModel.checkGpsEnabled()) {
                showGpsDialog = true
            } else {
                viewModel.autoDetectLocation()
            }
        }
    }

    val hasLocationPermission = androidx.core.content.ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
    androidx.core.content.ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

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

    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            containerColor = colors.bgSurface,
            icon = { Icon(Icons.Default.LocationSearching, null, tint = colors.accent, modifier = Modifier.size(28.dp)) },
            title = { Text(strings.permDialogTitle, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = colors.textPrimary) },
            text = {
                Text(
                    text = strings.permDialogDesc,
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    lineHeight = 17.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionRationale = false
                        locationPermissionLauncher.launch(
                            arrayOf(
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                ) {
                    Text(strings.grantPermission, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionRationale = false }) {
                    Text(strings.cancel, color = colors.textSecondary, fontSize = 12.sp)
                }
            }
        )
    }

    if (showGpsDialog) {
        AlertDialog(
            onDismissRequest = { showGpsDialog = false },
            containerColor = colors.bgSurface,
            icon = { Icon(Icons.Default.GpsOff, null, tint = colors.warning, modifier = Modifier.size(28.dp)) },
            title = { Text(strings.gpsDisabledTitle, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = colors.textPrimary) },
            text = {
                Text(
                    text = strings.gpsDisabledDesc,
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    lineHeight = 17.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showGpsDialog = false
                        val intent = android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.warning)
                ) {
                    Text(strings.openSettings, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGpsDialog = false }) {
                    Text(strings.cancel, color = colors.textSecondary, fontSize = 12.sp)
                }
            }
        )
    }

    if (showErrorDialog && uiState.error != null) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            containerColor = colors.bgSurface,
            icon = { Icon(Icons.Default.ErrorOutline, null, tint = colors.critical, modifier = Modifier.size(28.dp)) },
            title = { Text("Report Submission Issue", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = colors.textPrimary) },
            text = {
                Text(
                    text = "${uiState.error}\n\nYour entered details and photos are safely preserved. Please tap retry to send again.",
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
                    Text("Try Again", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("Cancel", color = colors.textSecondary, fontSize = 12.sp)
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
                            text = "Report Landslide or Hazard",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            letterSpacing = 0.5.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = if (currentStep == 1) "Step 1 of 2: Location & What Happened" else "Step 2 of 2: Danger Type & Urgency",
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
                            Text("Edit Details", color = colors.accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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

            // Stepper Progress Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Step 1 Pill
                val isStep1Active = currentStep == 1
                val isStep1Completed = currentStep > 1 || isStep1Valid
                Surface(
                    onClick = { currentStep = 1 },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isStep1Active) colors.accent else colors.bgSurface,
                    border = BorderStroke(1.dp, if (isStep1Active) colors.accent else colors.borderDefault),
                    shadowElevation = if (colors.isDark) 0.dp else 1.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        currentStep > 1 -> colors.success
                                        isStep1Active -> Color.White.copy(alpha = 0.25f)
                                        else -> colors.borderDefault
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
                            text = "1. Location & Details",
                            fontSize = 11.5.sp,
                            fontWeight = if (isStep1Active) FontWeight.Bold else FontWeight.Medium,
                            color = if (isStep1Active) Color.White else colors.textSecondary
                        )
                    }
                }

                // Step 2 Pill (With Strict Validation Guard)
                val isStep2Active = currentStep == 2
                Surface(
                    onClick = {
                        if (isStep1Valid) {
                            currentStep = 2
                            showStep1Errors = false
                        } else {
                            showStep1Errors = true
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isStep2Active) colors.accent else colors.bgSurface,
                    border = BorderStroke(1.dp, if (isStep2Active) colors.accent else colors.borderDefault),
                    shadowElevation = if (colors.isDark) 0.dp else 1.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
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
                            text = "2. Hazard & Urgency",
                            fontSize = 11.5.sp,
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
                    // ─── STEP 1: Location & What Happened ───
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Section 1: Location Card
                        FieldCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "1. WHERE IS THE DANGER?",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        letterSpacing = 0.5.sp,
                                        color = colors.textPrimary
                                    )
                                    if (hasValidLocation) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = colors.success.copy(alpha = 0.15f),
                                            border = BorderStroke(1.dp, colors.success.copy(alpha = 0.3f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                PulsingStatusDot(color = colors.success, size = 5.dp)
                                                Text(
                                                    "GPS ACTIVE",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = colors.success
                                                )
                                            }
                                        }
                                    }
                                }

                                val locationHeadline = if (uiState.resolvedLocationName.isNotBlank()) {
                                    uiState.resolvedLocationName
                                } else if (uiState.district.isNotBlank()) {
                                    uiState.district
                                } else {
                                    "Tap 'Use My Current GPS Location' or type landmark below"
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(colors.accent.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.LocationOn, null, tint = colors.accent, modifier = Modifier.size(20.dp))
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = locationHeadline,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = colors.textPrimary,
                                            lineHeight = 18.sp
                                        )
                                        Text(
                                            text = "Coordinates saved using your phone's GPS",
                                            fontSize = 11.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                }

                                // Auto-Detect My Location Button
                                OutlinedButton(
                                    onClick = {
                                        if (hasLocationPermission) {
                                            if (!viewModel.checkGpsEnabled()) {
                                                showGpsDialog = true
                                            } else {
                                                viewModel.autoDetectLocation()
                                            }
                                        } else {
                                            showPermissionRationale = true
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(44.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, colors.accent.copy(alpha = 0.6f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.accent)
                                ) {
                                    if (uiState.isFetchingLocation) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = colors.accent)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Finding GPS Location...", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                                    } else {
                                        Icon(Icons.Default.MyLocation, null, modifier = Modifier.size(16.dp), tint = colors.accent)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Use My Current GPS Location", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                HorizontalDivider(color = colors.borderDefault, thickness = 0.8.dp)

                                // Town / Landmark manual input
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedTextField(
                                        value = uiState.district,
                                        onValueChange = { viewModel.onDistrictChange(it) },
                                        label = { Text("Town, Village, or Road Landmark *", fontSize = 12.sp) },
                                        placeholder = { Text("e.g. Near Singtam Bridge, NH-10, Upper Dzongu", fontSize = 12.sp) },
                                        singleLine = true,
                                        isError = showStep1Errors && !hasValidLocation,
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
                                    if (showStep1Errors && !hasValidLocation) {
                                        Text(
                                            text = "⚠️ Location is required. Please tap 'Use My Current GPS Location' or enter your town / landmark.",
                                            fontSize = 11.sp,
                                            color = colors.critical
                                        )
                                    }
                                }
                            }
                        }

                        // Section 2: What Happened Field
                        FieldCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    "2. WHAT DID YOU SEE? *",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.5.sp,
                                    color = colors.textPrimary
                                )
                                OutlinedTextField(
                                    value = uiState.description,
                                    onValueChange = { viewModel.onDescriptionChange(it) },
                                    placeholder = {
                                        Text(
                                            "Describe what happened (e.g., mud and rocks sliding onto highway, cracks forming on hillside, road blocked, water rising)...",
                                            fontSize = 12.sp,
                                            color = colors.textSecondary,
                                            lineHeight = 17.sp
                                        )
                                    },
                                    minLines = 4,
                                    maxLines = 6,
                                    isError = showStep1Errors && !hasValidDescription,
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
                                if (showStep1Errors && !hasValidDescription) {
                                    Text(
                                        text = "⚠️ Please write a brief description (at least 5 characters) of the hazard.",
                                        fontSize = 11.sp,
                                        color = colors.critical
                                    )
                                }
                            }
                        }

                        // Section 3: Photos (Optional)
                        FieldCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "3. ADD PHOTOS (OPTIONAL)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        letterSpacing = 0.5.sp,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        if (uiState.photoUris.isEmpty()) "Optional" else "${uiState.photoUris.size} photo(s) selected",
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
                                            "Take Photo or Pick from Gallery",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.5.sp,
                                            color = colors.textPrimary
                                        )
                                    }
                                }
                            }
                        }

                        // Validation Alert Banner
                        if (showStep1Errors && !isStep1Valid) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = colors.critical.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, colors.critical.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Warning, null, tint = colors.critical, modifier = Modifier.size(20.dp))
                                    Text(
                                        text = "Please fill in your location and description before continuing.",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.critical
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        // Step 1 Continue CTA
                        Button(
                            onClick = {
                                if (isStep1Valid) {
                                    currentStep = 2
                                    showStep1Errors = false
                                } else {
                                    showStep1Errors = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isStep1Valid) colors.accent else colors.accent.copy(alpha = 0.75f)
                            )
                        ) {
                            Text(
                                text = "Next: Choose Hazard Type & Danger Level →",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = Color.White
                            )
                        }

                        Spacer(Modifier.height(24.dp))
                    }
                } else {
                    // ─── STEP 2: Hazard Type & Urgency Level ───
                    val step2ScrollState = rememberScrollState()
                    LaunchedEffect(step) {
                        step2ScrollState.scrollTo(0)
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(step2ScrollState)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Section 1: Incident Type Selector
                        SectionHeader(
                            title = "1. WHAT KIND OF DANGER IS IT?",
                            subtitle = "Tap the option that best describes what you observed"
                        )

                        // 2x3 Grid of incident types with simplified citizen-friendly labels
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            val types = IncidentType.values().toList()
                            types.chunked(2).forEach { rowTypes ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowTypes.forEach { type ->
                                        val isSelected = uiState.incidentType == type
                                        val labelText = when (type) {
                                            IncidentType.LANDSLIDE -> "Landslide / Mudslide"
                                            IncidentType.CRACK -> "Ground / Hill Crack"
                                            IncidentType.ROAD_BLOCKAGE -> "Road Blocked by Debris"
                                            IncidentType.FLASH_FLOOD -> "Flash Flood / Overflow"
                                            IncidentType.SLOPE_MOVEMENT -> "Sinking Ground / Slope"
                                            IncidentType.OTHER -> "Other Danger"
                                        }

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
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
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
                                                    modifier = Modifier.size(18.dp),
                                                    tint = if (isSelected) Color.White else colors.textSecondary
                                                )
                                                Text(
                                                    text = labelText,
                                                    fontSize = 11.5.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) Color.White else colors.textPrimary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Section 2: Urgency Level
                        SectionHeader(
                            title = "2. HOW DANGEROUS IS IT RIGHT NOW?",
                            subtitle = "Choose the urgency level to help rescue teams prioritize"
                        )

                        GraduatedSeveritySelector(
                            selectedSeverity = uiState.severity,
                            onSeveritySelected = { viewModel.onSeverityChange(it) }
                        )

                        // Safety Advice Banner
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
                                        "SAFETY ADVICE",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.warning
                                    )
                                    Text(
                                        uiState.severity.toLocalizedDirective(strings),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.textPrimary
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        // Bottom Navigation CTAs: Back + Submit
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GhostSecondaryButton(
                                text = "← Back to Details",
                                onClick = { currentStep = 1 },
                                modifier = Modifier.weight(0.35f)
                            )
                            PrimaryActionButton(
                                text = if (uiState.isSubmitting) "SUBMITTING..." else "SUBMIT REPORT",
                                onClick = { viewModel.submitReport() },
                                enabled = !uiState.isSubmitting,
                                containerColor = if (uiState.severity == AlertSeverity.CRITICAL) colors.critical else colors.accent,
                                modifier = Modifier.weight(0.65f)
                            )
                        }

                        Text(
                            text = "Your report is sent directly to emergency response teams and local authorities.",
                            fontSize = 11.sp,
                            color = colors.textSecondary,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
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
                viewModel.resetState()
                currentStep = 1
                showStep1Errors = false
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
                    "Report Sent Successfully!",
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    letterSpacing = 0.5.sp,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
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
                                Text("Receipt Number", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary)
                                Text("#$reportReceiptId", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = colors.accent)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Hazard Type", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary)
                                Text(uiState.incidentType.name.replace("_", " "), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Danger Level", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary)
                                Text(uiState.severity.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = uiState.severity.toThemeColor())
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Location", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary)
                                Text(
                                    uiState.resolvedLocationName.ifBlank { uiState.district.ifBlank { "Reported Sector" } },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textPrimary
                                )
                            }
                        }
                    }

                    Text(
                        text = if (uiState.isOnline) {
                            "Emergency responders and district authorities have been alerted. Please stay in a safe place away from active landslide slopes."
                        } else {
                            "Saved securely offline on your device. Your report and GPS coordinates will automatically be sent as soon as mobile network or internet reconnects."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        lineHeight = 17.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        viewModel.resetState()
                        currentStep = 1
                        showStep1Errors = false
                        onReportSubmitted()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("DONE / RETURN TO HOME", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = Color.White)
                }
            }
        )
    }
}
