package com.ner.landslide.presentation.ui.auth

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ner.landslide.domain.model.UserRole
import com.ner.landslide.presentation.ui.components.GlassCard
import com.ner.landslide.presentation.ui.components.LoadingContent
import com.ner.landslide.presentation.ui.theme.*
import com.ner.landslide.presentation.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onRegisterSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBase)
    ) {
        // Decorative background glow radial
        Box(
            modifier = Modifier
                .size(340.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-80).dp)
                .background(
                    Brush.radialGradient(
                        listOf(BrandIndigo.copy(alpha = 0.15f), Color.Transparent)
                    )
                )
        )

        if (uiState.isLoading) {
            LoadingContent()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Shield Emblem
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(BrandIndigo.copy(alpha = 0.3f), Primary80.copy(alpha = 0.2f))
                                )
                            )
                            .border(1.5.dp, Primary80.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AppRegistration,
                            contentDescription = null,
                            tint = Primary80,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Title & Mission Classification
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "BHURAKSHAK",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.4.sp,
                        color = OnBackgroundDark
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Primary80.copy(alpha = 0.2f),
                        border = BorderStroke(0.8.dp, Primary80.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = "REGISTRATION",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Primary80,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Personnel Onboarding & Regional Enlistment",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(24.dp))

                // Registration Glass Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = SurfaceDark.copy(alpha = 0.95f),
                    borderColor = Color.White.copy(alpha = 0.1f)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "NEW OPERATOR PROFILE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            color = Primary80
                        )

                        // Full Name
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, null, tint = Primary80, modifier = Modifier.size(20.dp)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary80,
                                unfocusedBorderColor = BorderSubtle,
                                focusedTextColor = OnBackgroundDark,
                                unfocusedTextColor = OnBackgroundDark,
                                focusedLabelColor = Primary80,
                                unfocusedLabelColor = TextMuted
                            )
                        )

                        // Email
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, null, tint = Primary80, modifier = Modifier.size(20.dp)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary80,
                                unfocusedBorderColor = BorderSubtle,
                                focusedTextColor = OnBackgroundDark,
                                unfocusedTextColor = OnBackgroundDark,
                                focusedLabelColor = Primary80,
                                unfocusedLabelColor = TextMuted
                            )
                        )

                        // Password
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Access Security Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, null, tint = Primary80, modifier = Modifier.size(20.dp)) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary80,
                                unfocusedBorderColor = BorderSubtle,
                                focusedTextColor = OnBackgroundDark,
                                unfocusedTextColor = OnBackgroundDark,
                                focusedLabelColor = Primary80,
                                unfocusedLabelColor = TextMuted
                            )
                        )

                        // Role Selection Label
                        Text(
                            text = "ASSIGNED REGIONAL ROLE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = TextSubtle,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        // Role Selector Pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RoleChip(
                                label = "Citizen",
                                icon = Icons.Default.PersonOutline,
                                selected = uiState.selectedRole == UserRole.CITIZEN,
                                modifier = Modifier.weight(1f)
                            ) { viewModel.onRoleSelected(UserRole.CITIZEN) }

                            RoleChip(
                                label = "Field Officer",
                                icon = Icons.Default.Badge,
                                selected = uiState.selectedRole == UserRole.FIELD_OFFICER,
                                modifier = Modifier.weight(1f)
                            ) { viewModel.onRoleSelected(UserRole.FIELD_OFFICER) }

                            RoleChip(
                                label = "Admin",
                                icon = Icons.Default.AdminPanelSettings,
                                selected = uiState.selectedRole == UserRole.ADMIN,
                                modifier = Modifier.weight(1f)
                            ) { viewModel.onRoleSelected(UserRole.ADMIN) }
                        }

                        // Error Banner
                        uiState.error?.let { error ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SeverityCritical.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, SeverityCritical.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.ErrorOutline, null, tint = SeverityCritical, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = error,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SeverityCritical
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                viewModel.registerWithEmail(
                                    name.trim(), email.trim(), password, uiState.selectedRole
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary80),
                            enabled = name.isNotBlank() && email.isNotBlank() && password.length >= 6
                        ) {
                            Text(
                                "ENLIST IN BHURAKSHAK NETWORK",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                letterSpacing = 0.5.sp,
                                color = ObsidianBase
                            )
                        }

                        // Instant Demo Button
                        OutlinedButton(
                            onClick = {
                                viewModel.loginAsDemo(
                                    uiState.selectedRole,
                                    name.ifBlank { "Dhruv Soni" }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Primary80.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = SurfaceElevated.copy(alpha = 0.5f)
                            )
                        ) {
                            Icon(Icons.Default.Bolt, null, tint = Primary80, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Instant Demo Access (${uiState.selectedRole.name.replace("_", " ")})",
                                color = Primary80,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        "Already have credentials? Sign In",
                        color = Primary80,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun RoleChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val borderColor = if (selected) Primary80 else BorderSubtle
    val bgColor = if (selected) Primary80.copy(0.15f) else SurfaceElevated

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        border = BorderStroke(1.2.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) Primary80 else TextMuted,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = if (selected) Primary80 else TextMuted,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}
