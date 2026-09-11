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
import com.ner.landslide.presentation.ui.components.LoadingContent
import com.ner.landslide.presentation.ui.components.PulsingStatusDot
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

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onLoginSuccess()
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

        if (uiState.isLoading) {
            LoadingContent()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
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

                Spacer(Modifier.height(32.dp))

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
                            onValueChange = { email = it },
                            label = { Text("Government / Citizen Email") },
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
                                unfocusedTextColor = OnBackgroundDark
                            )
                        )

                        // Password
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
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

                        // Sign In Button
                        Button(
                            onClick = { viewModel.signInWithEmail(email.trim(), password) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary80),
                            enabled = email.isNotBlank() && password.isNotBlank()
                        ) {
                            Text(
                                text = "AUTHENTICATE ACCESS",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                letterSpacing = 0.8.sp,
                                color = ObsidianBase
                            )
                        }

                        // Instant Guest Demo Button
                        OutlinedButton(
                            onClick = { viewModel.loginAsDemo() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.2.dp, Primary80.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.Bolt, null, tint = Primary80, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Instant Demo Access (One-Tap Guest)",
                                color = Primary80,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Register Link
                TextButton(onClick = onNavigateToRegister) {
                    Text(
                        text = "New Responder or Citizen? Register Credentials",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

