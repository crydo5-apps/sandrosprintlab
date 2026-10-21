package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.foundation.clickable
import com.example.R
import com.example.ui.theme.ColdGray
import com.example.ui.theme.DividerColor
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.IceWhite
import com.example.ui.theme.LabBlack
import com.example.ui.theme.LabCard
import com.example.ui.theme.LabRaised
import com.example.ui.theme.LaserCyan
import com.example.ui.theme.NeonMint
import com.example.ui.theme.SteelGray
import kotlinx.coroutines.delay
import java.io.File

enum class SplashPhase {
    LOGO,           // App logo appears briefly
    SANDRO_IMAGE,   // Uploaded Grok Image displayed for 3 seconds
    DONE
}

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    var phase by remember { mutableStateOf(SplashPhase.LOGO) }
    val imageProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // 1. Show Logo briefly (~1.2 seconds)
        delay(1200)
        phase = SplashPhase.SANDRO_IMAGE

        // 2. Show Sandro Grok Splash Image for 3 seconds (3000 ms)
        imageProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 3000, easing = LinearEasing)
        )
        phase = SplashPhase.DONE
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LabBlack)
    ) {
        // Phase 1: App Logo & Laboratory Insignia
        AnimatedVisibility(
            visible = phase == SplashPhase.LOGO,
            enter = fadeIn(tween(400)),
            exit = fadeOut(tween(400))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.radialGradient(listOf(LabCard, LabBlack))),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Holographic 3D Cube Badge
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(Brush.linearGradient(listOf(LaserCyan, ElectricBlue)))
                            .border(2.dp, IceWhite.copy(alpha = 0.4f), RoundedCornerShape(26.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Print,
                            contentDescription = "Sandros Print Labor",
                            tint = LabBlack,
                            modifier = Modifier.size(56.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "SANDROS PRINT LABOR",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        color = IceWhite,
                        fontSize = 22.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "IDEEN. LAYER FÜR LAYER. REALITÄT.",
                        style = MaterialTheme.typography.labelSmall,
                        color = LaserCyan,
                        letterSpacing = 3.sp,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(36.dp))

                    // Pulse indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(NeonMint)
                        )
                        Text(
                            text = "SYSTEM INITIALISIERUNG...",
                            style = MaterialTheme.typography.labelSmall,
                            color = SteelGray,
                            letterSpacing = 1.sp,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Phase 2: Full Screen Sandro Grok Image for 3 seconds
        AnimatedVisibility(
            visible = phase == SplashPhase.SANDRO_IMAGE,
            enter = fadeIn(tween(500)),
            exit = fadeOut(tween(400))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Try drawable resource first, fallback to asset file path
                val context = LocalContext.current
                val localImageFile = File("/public/assets/aistudio/grok-image-4ccd5073-2f5e-4e58-bafc-c1638c117670_8ac79b6e.jpg")

                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(
                            if (localImageFile.exists()) localImageFile
                            else R.drawable.splash_sandro_labor
                        )
                        .crossfade(true)
                        .build(),
                    contentDescription = "Sandro's Print Lab",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Top & Bottom subtle cyber gradients for UI readability
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(LabBlack.copy(alpha = 0.85f), Color.Transparent)
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, LabBlack.copy(alpha = 0.95f))
                            )
                        )
                )

                // Skip Button in top right
                Surface(
                    color = LabBlack.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 42.dp, end = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .clickable {
                                phase = SplashPhase.DONE
                                onSplashFinished()
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Überspringen",
                            style = MaterialTheme.typography.labelSmall,
                            color = ColdGray,
                            fontSize = 11.sp
                        )
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = LaserCyan,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Bottom Status & 3-Second Progress Bar
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SANDRO'S PRINT LAB",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = IceWhite,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "STARTET IN 3 SEKUNDEN...",
                            style = MaterialTheme.typography.labelSmall,
                            color = LaserCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }

                    // Smooth 3-second progress indicator
                    LinearProgressIndicator(
                        progress = { imageProgress.value },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = LaserCyan,
                        trackColor = LabRaised
                    )

                    Text(
                        text = "CR-10S Pro Werkbank & Google Drive Cloud bereit",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ColdGray,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
