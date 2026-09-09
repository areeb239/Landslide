package com.ner.landslide.presentation.ui.prediction

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ner.landslide.domain.model.PredictionResult
import com.ner.landslide.presentation.ui.components.*
import com.ner.landslide.presentation.ui.theme.*
import com.ner.landslide.presentation.viewmodel.PredictionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionScreen(
    onNavigateBack: () -> Unit,
    viewModel: PredictionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Risk Prediction", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info Banner
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Primary80.copy(0.12f))
            ) {
                Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Info, null, tint = Primary80, modifier = Modifier.size(20.dp))
                    Text(
                        "Enter current slope conditions. The AI model will compute landslide risk probability.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.8f)
                    )
                }
            }

            // Input Fields
            PredictionInputField(
                label = "Rainfall (mm) — last 24 hours",
                icon = Icons.Default.WaterDrop,
                value = uiState.rainfallMm,
                onValueChange = viewModel::onRainfallChange,
                placeholder = "e.g. 120"
            )
            PredictionInputField(
                label = "Slope Angle (°)",
                icon = Icons.Default.Terrain,
                value = uiState.slopeDeg,
                onValueChange = viewModel::onSlopeChange,
                placeholder = "e.g. 35"
            )
            PredictionInputField(
                label = "Soil Moisture (%)",
                icon = Icons.Default.Grass,
                value = uiState.soilMoisturePct,
                onValueChange = viewModel::onSoilMoistureChange,
                placeholder = "e.g. 78"
            )
            PredictionInputField(
                label = "Antecedent Rainfall (mm) — last 3 days",
                icon = Icons.Default.WaterDrop,
                value = uiState.antecedentRain3d,
                onValueChange = viewModel::onAntecedentRainChange,
                placeholder = "e.g. 250"
            )

            Button(
                onClick = { viewModel.predict() },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Psychology, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Predict Risk", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            // Result
            uiState.result?.let { result ->
                PredictionResultCard(result = result)
            }

            // Error
            uiState.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PredictionInputField(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, tint = Primary80) },
        placeholder = { Text(placeholder) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
}

@Composable
private fun PredictionResultCard(result: PredictionResult) {
    val riskColor = result.riskLevel.toAlertSeverityColor()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = riskColor.copy(0.1f)),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, riskColor.copy(0.4f))
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Risk Assessment", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
                if (result.isMock) {
                    Surface(shape = RoundedCornerShape(8.dp), color = SeverityModerate.copy(0.2f)) {
                        Text("Simulated", modifier = Modifier.padding(6.dp, 3.dp),
                            style = MaterialTheme.typography.labelSmall, color = SeverityModerate)
                    }
                }
            }

            // Risk Level
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(result.riskLevel, color = riskColor, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                Column {
                    Text("Probability: ${"%.0f".format(result.probability * 100)}%",
                        style = MaterialTheme.typography.bodyMedium)
                    Text("Confidence: ${"%.0f".format(result.confidence * 100)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                }
            }

            // Factor bars (XAI)
            if (result.factors.isNotEmpty()) {
                Divider(color = riskColor.copy(0.2f))
                Text("Contributing Factors", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold)
                result.factors.forEach { (factor, value) ->
                    Column {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(factor.replace("_", " ").replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall)
                            Text("${"%.0f".format(value * 100)}%",
                                style = MaterialTheme.typography.labelSmall, color = riskColor)
                        }
                        LinearProgressIndicator(
                            progress = { value.toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = riskColor,
                            trackColor = riskColor.copy(0.15f)
                        )
                    }
                }
            }

            // Recommendation
            if (result.recommendation.isNotBlank()) {
                Divider(color = riskColor.copy(0.2f))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Lightbulb, null, tint = riskColor, modifier = Modifier.size(18.dp))
                    Text(result.recommendation, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.85f))
                }
            }
        }
    }
}
