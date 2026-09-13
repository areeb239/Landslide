package com.ner.landslide.presentation.ui.auth

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
import com.ner.landslide.domain.model.UserRole
import com.ner.landslide.presentation.ui.components.GlassCard
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
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(UserRole.CITIZEN) }
    var underDevelopmentDialogTitle by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onRegisterSuccess()
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
                        listOf(BrandIndigo.copy(alpha = 0.15f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Brand Logo Emblem
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.drawable.bhoochetak_logo),
                    contentDescription = "Bhoochetak Logo",
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.5.dp, Primary80.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                )
            }

            Spacer(Modifier.height(14.dp))

            // Title & Mission Classification
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "BHOOCHETAK",
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
                text = "Create Account for Himalayan Landslide Early Warning Access",
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
                        text = "CREATE CITIZEN ACCOUNT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        color = Primary80
                    )

                    // Full Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (uiState.error != null) viewModel.clearError()
                        },
                        label = { Text("Full Name *") },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = Primary80, modifier = Modifier.size(20.dp)) },
                        singleLine = true,
                        enabled = !uiState.isLoading,
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
                        onValueChange = {
                            email = it
                            if (uiState.error != null) viewModel.clearError()
                        },
                        label = { Text("Email Address *") },
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
                            unfocusedTextColor = OnBackgroundDark,
                            focusedLabelColor = Primary80,
                            unfocusedLabelColor = TextMuted
                        )
                    )

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (uiState.error != null) viewModel.clearError()
                        },
                        label = { Text("Password (Min. 6 chars) *") },
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
                        enabled = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
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

                    // Confirm Password
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            if (uiState.error != null) viewModel.clearError()
                        },
                        label = { Text("Confirm Password *") },
                        leadingIcon = { Icon(Icons.Default.LockReset, null, tint = Primary80, modifier = Modifier.size(20.dp)) },
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
                            unfocusedTextColor = OnBackgroundDark,
                            focusedLabelColor = Primary80,
                            unfocusedLabelColor = TextMuted
                        )
                    )

                    // Password match indicator
                    if (confirmPassword.isNotBlank()) {
                        val matches = password == confirmPassword
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (matches) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = if (matches) SeverityLow else SeverityCritical,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (matches) "Passwords match" else "Passwords do not match",
                                fontSize = 11.sp,
                                color = if (matches) SeverityLow else SeverityCritical
                            )
                        }
                    }

                    // Regional Role Selection
                    Text(
                        text = "SELECT REGIONAL ROLE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = TextSubtle,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedRole = UserRole.CITIZEN },
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedRole == UserRole.CITIZEN) Primary80.copy(0.18f) else SurfaceElevated,
                            border = BorderStroke(1.2.dp, if (selectedRole == UserRole.CITIZEN) Primary80 else BorderSubtle)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.PersonOutline,
                                    null,
                                    tint = if (selectedRole == UserRole.CITIZEN) Primary80 else TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Citizen",
                                    fontSize = 12.sp,
                                    color = if (selectedRole == UserRole.CITIZEN) Primary80 else TextMuted,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    underDevelopmentDialogTitle = "Field Officer Portal"
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = SurfaceElevated,
                            border = BorderStroke(1.2.dp, BorderSubtle)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.Engineering,
                                    null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Field Officer",
                                    fontSize = 12.sp,
                                    color = TextMuted,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Quick Registration Presets
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "QUICK REGISTRATION PRESETS",
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
                                color = if (name == "Dhruv Soni") Primary80.copy(alpha = 0.25f) else SurfaceElevated,
                                border = BorderStroke(1.dp, if (name == "Dhruv Soni") Primary80 else BorderSubtle),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        name = "Dhruv Soni"
                                        email = "dhruvsoni@ner.gov.in"
                                        password = "Dhruv@1"
                                        confirmPassword = "Dhruv@1"
                                        passwordVisible = true
                                        confirmPasswordVisible = true
                                        selectedRole = UserRole.CITIZEN
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
                                    Text("Citizen Demo", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OnBackgroundDark)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SurfaceElevated,
                                border = BorderStroke(1.dp, BorderSubtle),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        underDevelopmentDialogTitle = "Field Officer Demo"
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Engineering, null, tint = TextMuted, modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Officer Demo", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                }
                            }
                        }

                        // Active Account Details Card
                        if (name == "Dhruv Soni" || email == "dhruvsoni@ner.gov.in") {
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
                                        Text("Citizen Account Ready", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Primary80)
                                    }
                                    Text("Name: Dhruv Soni", fontSize = 11.sp, color = OnBackgroundDark, fontWeight = FontWeight.SemiBold)
                                    Text("Email: dhruvsoni@ner.gov.in", fontSize = 11.sp, color = OnBackgroundDark)
                                    Text("Password: Dhruv@1", fontSize = 11.sp, color = Primary80, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
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
                            if (selectedRole == UserRole.FIELD_OFFICER) {
                                underDevelopmentDialogTitle = "Field Officer Registration"
                                return@Button
                            }
                            if (email.contains("officer", ignoreCase = true)) {
                                underDevelopmentDialogTitle = "Field Officer Portal"
                                return@Button
                            }
                            if (email.contains("admin", ignoreCase = true)) {
                                underDevelopmentDialogTitle = "Admin Portal"
                                return@Button
                            }
                            if (password.length < 6) {
                                viewModel.setValidationError("Password must be at least 6 characters long.")
                                return@Button
                            }
                            if (password != confirmPassword) {
                                viewModel.setValidationError("Password and Confirm Password do not match.")
                                return@Button
                            }
                            viewModel.registerWithEmail(
                                name = name,
                                email = email,
                                password = password,
                                confirmPassword = confirmPassword,
                                role = selectedRole
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary80),
                        enabled = name.isNotBlank() && email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank() && !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = ObsidianBase,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "CREATING ACCOUNT...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = ObsidianBase
                            )
                        } else {
                            Text(
                                "REGISTER & CREATE ACCOUNT",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.5.sp,
                                letterSpacing = 0.6.sp,
                                color = ObsidianBase
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            TextButton(
                onClick = onNavigateToLogin,
                enabled = !uiState.isLoading
            ) {
                Text(
                    "Already have an account? Sign In",
                    color = Primary80,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(20.dp))
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
                        text = "The $featureName module is currently under active development.\n\nOnly Citizen registration and access is enabled for this build.",
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
