package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PhotoItem
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberBorderGlow
import com.example.ui.theme.CyberError
import com.example.ui.theme.CyberMagenta
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TechLime
import com.example.ui.theme.TextCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PhotoAssistantView(
    photos: List<PhotoItem>,
    modifier: Modifier = Modifier
) {
    val photoCount = photos.size
    val anglesPresent = photos.map { it.angleDegrees }.toSet()

    CyberCard(modifier = modifier, borderColor = CyberBorderGlow) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header with assistant icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(ElectricBlue.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text("3D-Foto-Assistent", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Text("Qualitäts- & Rundum-Abdeckungsanalyse", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                }

                StatusPill(
                    text = when {
                        photoCount == 0 -> "Keine Fotos"
                        photoCount == 1 -> "1 Foto (Schätzung)"
                        photoCount < 6 -> "Basis-Abdeckung"
                        else -> "Optimale Serie"
                    },
                    statusType = when {
                        photoCount == 0 -> StatusType.ERROR
                        photoCount == 1 -> StatusType.WARNING
                        photoCount < 6 -> StatusType.INFO
                        else -> StatusType.SUCCESS
                    }
                )
            }

            // Radar Ring & Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Circular 360° Angle Radar
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(CyberBackground)
                        .border(1.dp, CyberBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(90.dp)) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = size.width / 2f - 6f

                        // Outer dashed circle
                        drawCircle(color = CyberBorder, radius = radius, style = Stroke(width = 1.5f))
                        drawCircle(color = CyberBorder.copy(alpha = 0.5f), radius = radius * 0.6f, style = Stroke(width = 1f))

                        // Draw target angle sectors (8 positions: 0, 45, 90, 135, 180, 225, 270, 315)
                        val sectorAngles = listOf(0, 45, 90, 135, 180, 225, 270, 315)
                        for (a in sectorAngles) {
                            val rad = Math.toRadians((a - 90).toDouble())
                            val px = center.x + (radius * cos(rad)).toFloat()
                            val py = center.y + (radius * sin(rad)).toFloat()

                            val hasShot = anglesPresent.any { Math.abs(it - a) < 25 } || (photoCount >= 8 && a < photoCount * 45)
                            drawCircle(
                                color = if (hasShot) NeonCyan else CyberBorder,
                                radius = if (hasShot) 4.5f else 2.5f,
                                center = Offset(px, py)
                            )
                        }

                        // Center indicator
                        drawCircle(color = if (photoCount > 0) TechLime else TextSecondary, radius = 4f, center = center)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$photoCount",
                            style = MaterialTheme.typography.titleMedium,
                            color = NeonCyan,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Fotos",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontSize = 9.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Breakdown list of quality factors
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    QualityItem(
                        icon = Icons.Default.Sync,
                        title = "Überlappung (~70%)",
                        status = if (photoCount >= 4) "Ausreichend" else "Niedrig",
                        isOk = photoCount >= 4
                    )
                    QualityItem(
                        icon = Icons.Default.LightMode,
                        title = "Gleichmäßige Ausleuchtung",
                        status = "Diffus ohne Schatten",
                        isOk = true
                    )
                    QualityItem(
                        icon = Icons.Default.Info,
                        title = "Hintergrund & Textur",
                        status = "Ruhig, nicht spiegelnd",
                        isOk = true
                    )
                }
            }

            // Honest guidance text callout
            Surface(
                color = if (photoCount == 1) AmberWarning.copy(alpha = 0.12f) else CyberSurfaceVariant,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(
                    1.dp,
                    if (photoCount == 1) AmberWarning.copy(alpha = 0.4f) else CyberBorder
                )
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        if (photoCount == 1) Icons.Default.Warning else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (photoCount == 1) AmberWarning else TextCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (photoCount == 1) {
                            "Wichtiger Hinweis: Ein einzelnes Foto erlaubt nur eine geschätzte 2.5D Rekonstruktion mit ungenauen Rückseiten. Für maßhaltigen 3D-Druck nimm bitte mindestens 8-16 Fotos rund um das Objekt auf!"
                        } else if (photoCount == 0) {
                            "Empfehlung: Umkreise das Objekt gleichmäßig in 15°-Schritten. Halte die Kamera auf gleicher Höhe mit mindestens 60% Überlappung zwischen den Bildern."
                        } else {
                            "Gute Ausgangslage: ${photoCount} Blickwinkel erfasst. Die Überlappung ermöglicht eine dichte Dreiecksnetz-Rekonstruktion."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (photoCount == 1) AmberWarning else TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun QualityItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    status: String,
    isOk: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (isOk) TechLime else AmberWarning,
            modifier = Modifier.size(14.dp)
        )
        Column {
            Text(title, style = MaterialTheme.typography.labelSmall, color = TextPrimary, fontSize = 11.sp)
            Text(status, style = MaterialTheme.typography.bodyMedium, color = if (isOk) TechLime else AmberWarning, fontSize = 10.sp)
        }
    }
}
