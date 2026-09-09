package com.ner.landslide.presentation.ui.report

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    Column(modifier = Modifier.fillMaxSize()) {
        // Offline banner
        if (!uiState.isOnline) {
            OfflineBanner(pendingCount = 1)
        }

        // Form content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = "Report an Incident",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            if (!uiState.isOnline) {
                Text(
                    text = "You're offline. Report will be saved locally and auto-synced when connected.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SeverityHigh
                )
            }

            // Incident Type
            SectionHeader(title = "Incident Type")
            val incidentTypes = IncidentType.values()
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                incidentTypes.forEach { type ->
                    FilterChip(
                        selected = uiState.incidentType == type,
                        onClick = { viewModel.onIncidentTypeChange(type) },
                        label = { Text(type.name.replace("_", " ")) }
                    )
                }
            }

            // Severity
            SectionHeader(title = "Severity Level")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AlertSeverity.values().forEach { severity ->
                    FilterChip(
                        selected = uiState.severity == severity,
                        onClick = { viewModel.onSeverityChange(severity) },
                        label = {
                            Text(
                                severity.name,
                                color = if (uiState.severity == severity) severity.toColor() else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    )
                }
            }

            // Location
            SectionHeader(title = "Location")
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (uiState.isFetchingLocation) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.MyLocation, null, tint = Primary80)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (uiState.latitude != 0.0)
                                "%.5f, %.5f".format(uiState.latitude, uiState.longitude)
                            else "Location not fetched",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    TextButton(onClick = { viewModel.fetchLocation() }) {
                        Text("Refresh", color = Primary80, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // District & Village
            OutlinedTextField(
                value = uiState.district,
                onValueChange = { viewModel.onDistrictChange(it) },
                label = { Text("District") },
                leadingIcon = { Icon(Icons.Default.LocationCity, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.village,
                onValueChange = { viewModel.onVillageChange(it) },
                label = { Text("Village / Area") },
                leadingIcon = { Icon(Icons.Default.Place, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Description
            SectionHeader(title = "Description")
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                label = { Text("Describe what you observed...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp),
                maxLines = 5
            )

            // Photo Upload
            SectionHeader(title = "Photos / Videos")
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { photoPicker.launch("image/*") },
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.AddAPhoto, null, tint = Primary80)
                    Text(
                        text = if (uiState.photoUris.isEmpty()) "Tap to select photos"
                        else "${uiState.photoUris.size} photo(s) selected",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Photo Previews
            if (uiState.photoUris.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    uiState.photoUris.forEach { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            // Error
            uiState.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
            }

            // Submit
            Button(
                onClick = { viewModel.submitReport() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = uiState.description.isNotBlank() && !uiState.isSubmitting
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Send, null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (uiState.isOnline) "Submit Report" else "Save Offline",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
