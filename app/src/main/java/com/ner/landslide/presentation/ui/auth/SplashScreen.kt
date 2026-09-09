package com.ner.landslide.presentation.ui.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.ner.landslide.presentation.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        visible = true
        delay(2000)
        if (FirebaseAuth.getInstance().currentUser != null || com.ner.landslide.data.repository.UserRepositoryImpl.isSessionActive(context)) {
            onNavigateToHome()
        } else {
            onNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BackgroundDark, Primary40.copy(alpha = 0.3f), BackgroundDark)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + scaleIn(initialScale = 0.8f)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(listOf(Primary80, Primary40))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Landscape,
                        contentDescription = null,
                        tint = OnPrimary,
                        modifier = Modifier.size(52.dp)
                    )
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "NER Landslide Watch",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnBackgroundDark
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "AI-Powered Early Warning System",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnBackgroundDark.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(48.dp))
                CircularProgressIndicator(
                    color = Primary80,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
