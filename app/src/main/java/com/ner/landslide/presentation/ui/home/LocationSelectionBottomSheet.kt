package com.ner.landslide.presentation.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ner.landslide.presentation.ui.components.PulsingStatusDot
import com.ner.landslide.presentation.ui.theme.BhurakshakTheme
import com.ner.landslide.util.AppLocationManager
import com.ner.landslide.util.LocationSearchResult
import com.ner.landslide.util.SelectedLocation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSelectionBottomSheet(
    currentLocation: SelectedLocation,
    onDismissRequest: () -> Unit,
    onSelectGpsLocation: () -> Unit,
    onSelectCustomLocation: (LocationSearchResult) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    searchResults: List<LocationSearchResult>,
    isSearching: Boolean
) {
    val colors = BhurakshakTheme.colors
    var searchQuery by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = colors.bgSurface,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = colors.borderDefault)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Title & Subtitle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SELECT OPERATIONAL SECTOR",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.accent,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "Set location for Weather, Radar & Risk Prediction",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }

                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // GPS Quick Selector Action Card
            Surface(
                onClick = {
                    onSelectGpsLocation()
                    onDismissRequest()
                },
                shape = RoundedCornerShape(12.dp),
                color = if (currentLocation.isGpsLocation) colors.accent.copy(alpha = 0.12f) else colors.bgBase,
                border = BorderStroke(
                    1.2.dp,
                    if (currentLocation.isGpsLocation) colors.accent else colors.borderDefault
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (currentLocation.isGpsLocation) colors.accent else colors.bgSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null,
                            tint = if (currentLocation.isGpsLocation) Color.White else colors.accent,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Use Current GPS Location",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = colors.textPrimary
                            )
                            if (currentLocation.isGpsLocation) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = colors.accent.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "ACTIVE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = colors.accent,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (currentLocation.isGpsLocation)
                                currentLocation.name
                            else
                                "Auto-detect sector using phone GNSS sensors",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.5.sp,
                            color = colors.textSecondary,
                            maxLines = 1
                        )
                    }

                    if (currentLocation.isGpsLocation) {
                        PulsingStatusDot(color = colors.accent, size = 8.dp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.ArrowForwardIos,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            // Search Bar Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    onSearchQueryChange(it)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Search city, hill station, or district...",
                        fontSize = 13.sp,
                        color = colors.textSecondary
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            onSearchQueryChange("")
                        }) {
                            Icon(Icons.Default.Clear, null, tint = colors.textSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.accent,
                    unfocusedBorderColor = colors.borderDefault,
                    focusedContainerColor = colors.bgBase,
                    unfocusedContainerColor = colors.bgBase,
                    cursorColor = colors.accent,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary
                )
            )

            // Horizontal Sector Hotspot Chips
            Text(
                text = "POPULAR MONITORING SECTORS",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textSecondary,
                letterSpacing = 0.5.sp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AppLocationManager.POPULAR_SECTOR_HOTSPOTS.take(8).forEach { sector ->
                    val isSelected = !currentLocation.isGpsLocation && currentLocation.name.contains(sector.name, ignoreCase = true)
                    Surface(
                        onClick = {
                            onSelectCustomLocation(sector)
                            onDismissRequest()
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) colors.accent else colors.bgBase,
                        border = BorderStroke(1.dp, if (isSelected) colors.accent else colors.borderDefault)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = sector.name,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else colors.textPrimary
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = colors.borderDefault.copy(alpha = 0.5f), thickness = 0.5.dp)

            // Search Results List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isSearching) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = colors.accent, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                        }
                    }
                } else if (searchResults.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No matching sectors found. Try another city or district name.",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.5.sp,
                                color = colors.textSecondary
                            )
                        }
                    }
                } else {
                    items(searchResults, key = { "${it.name}_${it.state}" }) { result ->
                        val isSelected = !currentLocation.isGpsLocation &&
                                currentLocation.name.contains(result.name, ignoreCase = true)

                        Surface(
                            onClick = {
                                onSelectCustomLocation(result)
                                onDismissRequest()
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) colors.accent.copy(alpha = 0.1f) else Color.Transparent,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = null,
                                        tint = if (isSelected) colors.accent else colors.textSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Column {
                                        Text(
                                            text = result.name,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = if (isSelected) colors.accent else colors.textPrimary
                                        )
                                        Text(
                                            text = "${result.state} • ${result.category}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 11.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = colors.accent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}
