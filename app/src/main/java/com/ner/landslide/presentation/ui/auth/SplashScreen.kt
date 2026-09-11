package com.ner.landslide.presentation.ui.auth

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.ner.landslide.R
import com.ner.landslide.presentation.ui.components.PulsingStatusDot
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
        delay(1800)
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
                    colors = listOf(ObsidianBase, BackgroundDark, Color(0xFF0F172A))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + scaleIn(initialScale = 0.85f)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.bhoochetak_logo),
                        contentDescription = "Bhoochetak Logo",
                        modifier = Modifier
                            .size(108.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .border(1.5.dp, Primary80.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    )
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "BHOOCHETAK",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.8.sp,
                    color = OnBackgroundDark
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "Himalayan Landslide Early Warning System",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 13.sp,
                    color = TextMuted
                )

                Spacer(Modifier.height(48.dp))

                PulsingStatusDot(color = Primary80, size = 10.dp)
            }
        }
    }
}

