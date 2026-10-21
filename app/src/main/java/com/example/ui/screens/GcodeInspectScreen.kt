package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GcodeResult
import com.example.model.Project
import com.example.model.ProjectStatus
import com.example.model.SlicingConfig
import com.example.service.slicer.CrealityCR10SProSlicer
import com.example.service.slicer.SlicingProgress
import com.example.ui.components.WorkflowStepIndicator
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberPrimaryButton
import com.example.ui.components.CyberSecondaryButton
import com.example.ui.components.MetricBadge
import com.example.ui.components.StatusPill
import com.example.ui.components.StatusType
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberBorderGlow
import com.example.ui.theme.CyberError
import com.example.ui.theme.CyberMagenta
import com.example.ui.theme.CyberSurfaceCard
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TechLime
import com.example.ui.theme.TextCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GcodeInspectScreen(
    project: Project,
    config: SlicingConfig,
    onNavigateBack: () -> Unit,
    onSlicingFinished: (Project, GcodeResult) -> Unit,
    onProceedToSdExport: (Project) -> Unit
) {
    BackHandler { onNavigateBack() }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var gcodeResult by remember { mutableStateOf(project.gcodeResult) }
    var isSlicing by remember { mutableStateOf(gcodeResult == null) }
    var slicingPercent by remember { mutableIntStateOf(0) }
    var slicingPhase by remember { mutableStateOf("Slicer initialisieren...") }
    var slicingError by remember { mutableStateOf<String?>(null) }
    var selectedLayerSlider by remember { mutableIntStateOf(0) }

    val slicer = remember { CrealityCR10SProSlicer() }

    LaunchedEffect(Unit) {
        if (gcodeResult == null && project.mesh != null) {
            isSlicing = true
            slicer.slice(project.mesh, config).collect { progress ->
                when (progress) {
                    is SlicingProgress.Progress -> {
                        slicingPercent = progress.percentage
                        slicingPhase = progress.currentPhase
                    }
                    is SlicingProgress.Completed -> {
                        isSlicing = false
                        gcodeResult = progress.result
                        val updatedProject = project.copy(
                            status = ProjectStatus.SLICED,
                            slicingConfig = config,
                            gcodeResult = progress.result
                        )
                        onSlicingFinished(updatedProject, progress.result)
                    }
                    is SlicingProgress.Error -> {
                        isSlicing = false
                        slicingError = progress.message
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "G-Code Analyse & Schichten",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            "Creality CR-10S Pro G-Code",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextCyan
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück", tint = TextCyan)
                    }
                },
                actions = {
                    if (gcodeResult != null) {
                        IconButton(
                            onClick = {
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "${project.title}.gcode")
                                    putExtra(Intent.EXTRA_TEXT, gcodeResult?.fullGcodeContent ?: "")
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "G-Code teilen"))
                            }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Teilen", tint = NeonCyan)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CyberBackground)
            )
        },
        containerColor = CyberBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WorkflowStepIndicator(currentStep = 4)

            // Slicing Loading State
            if (isSlicing) {
                CyberCard(glowEffect = true) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = NeonCyan,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(52.dp)
                        )

                        Text(
                            text = "CR-10S Pro Slicing läuft...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        LinearProgressIndicator(
                            progress = { slicingPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = NeonCyan,
                            trackColor = CyberSurfaceVariant
                        )

                        Text(
                            text = "$slicingPercent% — $slicingPhase",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            } else if (slicingError != null) {
                // Slicing Error State
                CyberCard(borderColor = CyberError) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Slicing Fehlgeschlagen", style = MaterialTheme.typography.titleMedium, color = CyberError)
                        Text(slicingError ?: "", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                        CyberSecondaryButton(
                            text = "Zurück zu den Einstellungen",
                            onClick = onNavigateBack,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else if (gcodeResult != null) {
                val res = gcodeResult!!

                // Slicing Success Summary Card
                CyberCard(glowEffect = true) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Slicing erfolgreich abgeschlossen",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TechLime
                            )
                            StatusPill("${res.totalLayers} Schichten", StatusType.SUCCESS)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val filamentWeight = "%.1f".format(res.estimatedFilamentWeightGrams)
                            val filamentLength = "%.1f".format(res.estimatedFilamentLengthMeters)
                            MetricBadge("Dauer ca.", res.formattedTime, Modifier.weight(1f), NeonCyan)
                            MetricBadge("Filament", "$filamentWeight g (${filamentLength}m)", Modifier.weight(1.3f), TechLime)
                            MetricBadge("Dateigröße", res.formattedSize, Modifier.weight(1f), ElectricBlue)
                        }

                        // Machine specific warning callout
                        Surface(
                            color = AmberWarning.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AmberWarning.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "Achtung: Dieser G-Code enthält Creality CR-10S Pro Marlin-Befehle (G29 Matrix-Leveling, M420 S1, 300x300mm Grenzen). Niemals auf anderen Druckermodellen ausführen!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AmberWarning,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // Layer Preview Slider
                val maxLayers = res.totalLayers.coerceAtLeast(1)
                val clampedLayer = selectedLayerSlider.coerceIn(0, maxLayers)

                CyberCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Schichten-Inspektion", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Text(
                                "Layer $clampedLayer von $maxLayers",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextCyan
                            )
                        }

                        Slider(
                            value = clampedLayer.toFloat(),
                            onValueChange = { selectedLayerSlider = it.toInt().coerceIn(0, maxLayers) },
                            valueRange = 0f..maxLayers.toFloat(),
                            colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan, inactiveTrackColor = CyberBorder),
                            modifier = Modifier.testTag("layer_inspect_slider")
                        )

                        val zHeight = "%.2f".format(0.24f + (clampedLayer * config.layerHeightMm))
                        val fanStatus = if (clampedLayer > 0) "100%" else "0% (Erste Schicht Haftung)"
                        Text(
                            text = "Z-Höhe: $zHeight mm | Geschwindigkeit: 50 mm/s | Lüfter: $fanStatus",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                // G-Code Excerpt / Console View
                CyberCard(borderColor = CyberBorder) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Code, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                                Text("G-Code Vorschau & Header", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            }

                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(res.fullGcodeContent))
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Kopieren", tint = TextCyan, modifier = Modifier.size(16.dp))
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberBackground)
                                .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
                                .horizontalScroll(rememberScrollState())
                                .verticalScroll(rememberScrollState())
                                .padding(10.dp)
                        ) {
                            Text(
                                text = res.gcodeHeaderSnippet,
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Monospace,
                                color = TechLime,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // Primary Action: Proceed to SD Card Export
                CyberPrimaryButton(
                    text = "G-Code auf SD-Karte / OTG exportieren",
                    onClick = {
                        val updatedProj = project.copy(
                            status = ProjectStatus.SLICED,
                            gcodeResult = res
                        )
                        onProceedToSdExport(updatedProj)
                    },
                    icon = Icons.Default.SdCard,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "proceed_to_sd_export_button"
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
