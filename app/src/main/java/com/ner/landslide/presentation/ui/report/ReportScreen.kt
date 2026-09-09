package com.ner.landslide.presentation.ui.report

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReportScreen(
    onReportSubmitted: () -> Unit,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                            "Civic Hazard Report",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = OnBackgroundDark
                        )
                        Text(
                            "Direct link to District Disaster Management",
                            style = MaterialTheme.typography.labelSmall,
                            color = Primary80
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ObsidianBase
                )
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section: Incident Classification
                SectionHeader(
                    title = "Incident Classification",
                    subtitle = "Specify the type of geotechnical or slope hazard"
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IncidentType.values().forEach { type ->
                        val isSelected = uiState.incidentType == type
                        Surface(
                            onClick = { viewModel.onIncidentTypeChange(type) },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) Primary80.copy(alpha = 0.2f) else SurfaceVariantDark,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) Primary80 else Color.White.copy(alpha = 0.08f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
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
                                        IncidentType.OTHER -> Icons.Default.HelpOutline
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isSelected) Primary80 else TextMuted
                                )
                                Text(
                                    text = type.name.replace("_", " "),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Primary80 else OnBackgroundDark
                                )
                            }
                        }
                    }
                }

                // Section: Hazard Severity Level
                SectionHeader(
                    title = "Hazard Severity Level",
                    subtitle = "Assess current threat to human life or infrastructure"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AlertSeverity.values().forEach { severity ->
                        val isSelected = uiState.severity == severity
                        val severityColor = severity.toColor()
                        Surface(
                            onClick = { viewModel.onSeverityChange(severity) },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) severityColor.copy(alpha = 0.2f) else SurfaceVariantDark,
                            border = BorderStroke(
                                1.2.dp,
                                if (isSelected) severityColor else Color.White.copy(alpha = 0.08f)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) severityColor else TextSubtle)
                                )
                                Text(
                                    text = severity.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) severityColor else TextMuted
                                )
                            }
                        }
                    }
                }

                // Section: GNSS Location Card
                SectionHeader(
                    title = "Location Coordinates",
                    subtitle = "Automated high-precision GNSS triangulation"
                )

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = SurfaceDark.copy(alpha = 0.9f)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Primary80.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.isFetchingLocation) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Primary80,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.MyLocation, null, tint = Primary80, modifier = Modifier.size(22.dp))
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                PulsingStatusDot(color = Primary80, size = 6.dp)
                                Text(
                                    if (uiState.latitude != 0.0) "GNSS SIGNAL LOCKED" else "ACQUIRING GNSS...",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary80,
                                    letterSpacing = 0.8.sp
                                )
                            }
                            Text(
                                text = if (uiState.latitude != 0.0)
                                    "%.5f° N, %.5f° E".format(uiState.latitude, uiState.longitude)
                                else "Searching coordinates...",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnBackgroundDark
                            )
                        }

                        IconButton(
                            onClick = { viewModel.fetchLocation() },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SurfaceElevated)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Primary80, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Sector & Landmark Inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.district,
                        onValueChange = { viewModel.onDistrictChange(it) },
                        label = { Text("District", fontSize = 12.sp) },
                        placeholder = { Text("e.g. Gangtok", color = TextSubtle) },
                        leadingIcon = { Icon(Icons.Default.LocationCity, null, tint = Primary80, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary80,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = OnBackgroundDark,
                            unfocusedTextColor = OnBackgroundDark
                        )
                    )
                    OutlinedTextField(
                        value = uiState.village,
                        onValueChange = { viewModel.onVillageChange(it) },
                        label = { Text("Village / Milestone", fontSize = 12.sp) },
                        placeholder = { Text("e.g. Sevoke NH-10", color = TextSubtle) },
                        leadingIcon = { Icon(Icons.Default.Place, null, tint = CyberCyan, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary80,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = OnBackgroundDark,
                            unfocusedTextColor = OnBackgroundDark
                        )
                    )
                }

                // Description
                SectionHeader(
                    title = "Incident Observation",
                    subtitle = "Detail visible slope crack dimensions, debris volume, or tree tilting"
                )

                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { viewModel.onDescriptionChange(it) },
                    placeholder = {
                        Text(
                            "e.g. Tension cracks observed along hill slope above NH-10. Mud slurry beginning to wash into roadside drains...",
                            color = TextSubtle,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary80,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = OnBackgroundDark,
                        unfocusedTextColor = OnBackgroundDark
                    )
                )

                // Photo Upload Dropzone
                SectionHeader(
                    title = "Photographic Evidence",
                    subtitle = "Attach slope failure images for AI visual verification"
                )

                Surface(
                    onClick = { photoPicker.launch("image/*") },
                    shape = RoundedCornerShape(14.dp),
                    color = SurfaceDark,
                    border = BorderStroke(1.2.dp, Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Primary80.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AddAPhoto, null, tint = Primary80, modifier = Modifier.size(22.dp))
                        }
                        Text(
                            text = if (uiState.photoUris.isEmpty()) "TAP TO SELECT OR CAPTURE EVIDENCE"
                            else "${uiState.photoUris.size} PHOTO(S) ATTACHED",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = if (uiState.photoUris.isEmpty()) TextMuted else Primary80
                        )
                    }
                }

                // Photo Previews
                if (uiState.photoUris.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        uiState.photoUris.forEach { uri ->
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, Primary80.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                // Submit Button
                Button(
                    onClick = { viewModel.submitReport() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary80),
                    enabled = uiState.description.isNotBlank() && !uiState.isSubmitting
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = ObsidianBase,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Icon(Icons.Default.Send, null, tint = ObsidianBase, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (uiState.isOnline) "TRANSMIT DISASTER REPORT" else "SAVE LOCALLY (OFFLINE CACHE)",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            letterSpacing = 0.8.sp,
                            color = ObsidianBase
                        )
                    }
                }

                Spacer(Modifier.height(48.dp))
            }
        }
    }
}

