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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ner.landslide.R
import com.ner.landslide.presentation.ui.components.GlassCard
import com.ner.landslide.presentation.ui.theme.*
import com.ner.landslide.presentation.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var underDevelopmentDialogTitle by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBase)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        listOf(BrandIndigo.copy(alpha = 0.2f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            // Brand Emblem
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.drawable.bhoochetak_logo),
                    contentDescription = "Bhoochetak Logo",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.5.dp, Primary80.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                )
            }

            Spacer(Modifier.height(18.dp))

            // Title & Mission Classification
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "BHOOCHETAK",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp,
                    color = OnBackgroundDark
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Primary80.copy(alpha = 0.2f),
                    border = BorderStroke(0.8.dp, Primary80.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = "NER",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Primary80,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Himalayan Landslide Early Warning & Telemetry System",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.sp,
                color = TextMuted,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))

            // Authentication Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceDark.copy(alpha = 0.95f),
                borderColor = Color.White.copy(alpha = 0.1f)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "OPERATOR LOGIN",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        color = Primary80
                    )

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            if (uiState.error != null) viewModel.clearError()
                        },
                        label = { Text("Government / Citizen Email") },
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = Primary80, modifier = Modifier.size(20.dp)) },
                        singleLine = true,
                        enabled = !uiState.isLoading,
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
                            unfocusedTextColor = OnBackgroundDark
                        )
                    )

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (uiState.error != null) viewModel.clearError()
                        },
                        label = { Text("Security Key / Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = Primary80, modifier = Modifier.size(20.dp)) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = TextMuted
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        singleLine = true,
                        enabled = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary80,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = OnBackgroundDark,
                            unfocusedTextColor = OnBackgroundDark
                        )
                    )

                    // Error Banner
                    uiState.error?.let { error ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SeverityCritical.copy(alpha = 0.15f),
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
                                    fontSize = 12.sp,
                                    color = SeverityCritical
                                )
                            }
                        }
                    }

                    // Quick Operator Access Chips
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "QUICK OPERATOR PRESETS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = TextSubtle,
                            fontSize = 11.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SurfaceElevated,
                                border = BorderStroke(1.dp, BorderSubtle),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        underDevelopmentDialogTitle = "Admin Portal"
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.AdminPanelSettings, null, tint = TextMuted, modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (email == "dhruvsoni@ner.gov.in" || email == "citizen@ner.gov.in") Primary80.copy(alpha = 0.25f) else SurfaceElevated,
                                border = BorderStroke(1.dp, if (email == "dhruvsoni@ner.gov.in" || email == "citizen@ner.gov.in") Primary80 else BorderSubtle),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        email = "dhruvsoni@ner.gov.in"
                                        password = "Dhruv@1"
                                        passwordVisible = true
                                        if (uiState.error != null) viewModel.clearError()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Person, null, tint = Primary80, modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Citizen", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OnBackgroundDark)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SurfaceElevated,
                                border = BorderStroke(1.dp, BorderSubtle),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        underDevelopmentDialogTitle = "Field Officer Portal"
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Engineering, null, tint = TextMuted, modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Officer", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                }
                            }
                        }

                        // Active Account Info Card
                        if (email == "dhruvsoni@ner.gov.in" || email == "citizen@ner.gov.in") {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Primary80.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, Primary80.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.CheckCircle, null, tint = Primary80, modifier = Modifier.size(14.dp))
                                        Text("Citizen Account Active", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Primary80)
                                    }
                                    Text("Account: Dhruv Soni", fontSize = 11.sp, color = OnBackgroundDark, fontWeight = FontWeight.SemiBold)
                                    Text("Email: dhruvsoni@ner.gov.in", fontSize = 11.sp, color = OnBackgroundDark)
                                    Text("Password: Dhruv@1", fontSize = 11.sp, color = Primary80, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Sign In Button
                    Button(
                        onClick = {
                            if (email.contains("admin", ignoreCase = true)) {
                                underDevelopmentDialogTitle = "Admin Portal"
                                return@Button
                            }
                            if (email.contains("officer", ignoreCase = true)) {
                                underDevelopmentDialogTitle = "Field Officer Portal"
                                return@Button
                            }
                            viewModel.signInWithEmail(email, password)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary80),
                        enabled = email.isNotBlank() && password.isNotBlank() && !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = ObsidianBase,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "VERIFYING CREDENTIALS...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = ObsidianBase
                            )
                        } else {
                            Text(
                                text = "AUTHENTICATE ACCESS",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                letterSpacing = 0.8.sp,
                                color = ObsidianBase
                            )
                        }
                    }

                    // Divider with text
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = BorderSubtle)
                        Text(
                            text = "NEW TO BHOOCHETAK?",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = BorderSubtle)
                    }

                    // Sign Up / Create Account Button
                    OutlinedButton(
                        onClick = onNavigateToRegister,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.2.dp, Primary80.copy(alpha = 0.6f)),
                        enabled = !uiState.isLoading
                    ) {
                        Icon(Icons.Default.PersonAdd, null, tint = Primary80, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "CREATE ACCOUNT / SIGN UP",
                            color = Primary80,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        underDevelopmentDialogTitle?.let { featureName ->
            AlertDialog(
                onDismissRequest = { underDevelopmentDialogTitle = null },
                containerColor = SurfaceDark,
                title = {
                    Text(
                        text = "🚧 $featureName is under development",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = OnBackgroundDark
                    )
                },
                text = {
                    Text(
                        text = "The $featureName module is currently under active development.\n\nOnly Citizen login is active for Himalayan landslide alerts and emergency telemetry.",
                        color = TextMuted,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { underDevelopmentDialogTitle = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary80),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Understood", color = ObsidianBase, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}
