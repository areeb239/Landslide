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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ner.landslide.domain.model.UserRole
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
            .background(Brush.verticalGradient(listOf(BackgroundDark, SurfaceDark)))
    ) {
        if (uiState.isLoading) {
            LoadingContent()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp, vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(24.dp))
                Text(
                    "Create Account",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = OnBackgroundDark
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Join the NER Landslide Monitoring Network",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnBackgroundDark.copy(0.55f),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(36.dp))

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                Spacer(Modifier.height(14.dp))

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(Modifier.height(14.dp))

                // Password
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                null
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(Modifier.height(24.dp))

                // Role Selection
                Text(
                    "Select Your Role",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = OnBackgroundDark,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
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

                uiState.error?.let { error ->
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(Modifier.height(10.dp))
                            FilledTonalButton(
                                onClick = {
                                    viewModel.loginAsDemo(
                                        uiState.selectedRole,
                                        name.ifBlank { "Dhruv Soni" }
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Bolt, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Bypass with Demo Mode (${uiState.selectedRole.name})")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                Button(
                    onClick = {
                        viewModel.registerWithEmail(
                            name.trim(), email.trim(), password, uiState.selectedRole
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    enabled = name.isNotBlank() && email.isNotBlank() && password.length >= 6
                ) {
                    Text("Create Account", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }

                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        viewModel.loginAsDemo(
                            uiState.selectedRole,
                            name.ifBlank { "Dhruv Soni" }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Primary80.copy(alpha = 0.6f))
                ) {
                    Icon(Icons.Default.Bolt, null, tint = Primary80, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Instant Demo Mode (${uiState.selectedRole.name.replace("_", " ")})",
                        color = Primary80,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(16.dp))

                TextButton(onClick = onNavigateToLogin) {
                    Text("Already have an account? Sign In", color = Primary80)
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
    val borderColor = if (selected) Primary80 else OnBackgroundDark.copy(0.2f)
    val bgColor = if (selected) Primary80.copy(0.15f) else SurfaceDark

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(1.5.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = if (selected) Primary80 else OnBackgroundDark.copy(0.6f),
                modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(4.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) Primary80 else OnBackgroundDark.copy(0.6f),
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        }
    }
}
