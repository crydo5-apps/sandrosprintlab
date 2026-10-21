package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.StlGenerator
import com.example.model.Mesh3D
import com.example.model.Project
import com.example.ui.components.WorkflowStepIndicator
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberPrimaryButton
import com.example.ui.components.CyberSecondaryButton
import com.example.ui.components.MetricBadge
import com.example.ui.components.Model3DViewer
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
fun ModelEditorScreen(
    project: Project,
    onNavigateBack: () -> Unit,
    onSaveMeshUpdate: (Mesh3D) -> Unit,
    onProceedToSlice: (Project) -> Unit,
    onImportNewStl: () -> Unit
) {
    BackHandler { onNavigateBack() }
    val context = LocalContext.current

    var currentMesh by remember { mutableStateOf(project.mesh ?: Mesh3D()) }
    var scaleSlider by remember { mutableFloatStateOf(currentMesh.scale) }
    var highlightOverhangs by remember { mutableStateOf(false) }

    fun updateScale(newScale: Float) {
        scaleSlider = newScale
        currentMesh = currentMesh.copy(scale = newScale)
        onSaveMeshUpdate(currentMesh)
    }

    fun centerModel() {
        currentMesh = currentMesh.copy(offsetX = 0f, offsetY = 0f)
        onSaveMeshUpdate(currentMesh)
        Toast.makeText(context, "Modell auf CR-10S Pro Druckbett zentriert", Toast.LENGTH_SHORT).show()
    }

    fun snapToBed() {
        currentMesh = currentMesh.copy(offsetZ = 0f)
        onSaveMeshUpdate(currentMesh)
        Toast.makeText(context, "Modell bündig auf Z=0 platziert", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            project.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            "3D-Modell-Editor & Mesh-Prüfung",
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
                    IconButton(
                        onClick = {
                            val stl = StlGenerator.exportToAsciiStl(currentMesh)
                            Toast.makeText(context, "STL exportiert (${stl.length / 1024} KB)", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("export_stl_icon_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "STL exportieren", tint = NeonCyan)
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
            WorkflowStepIndicator(currentStep = 2)

            // Interactive 3D Model Viewer Canvas
            Model3DViewer(
                mesh = currentMesh,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                highlightOverhangs = highlightOverhangs,
                onCenterOnBed = { centerModel() }
            )

            // Warning if reconstructed from single photo
            if (currentMesh.isEstimatedFromSinglePhoto) {
                Surface(
                    color = AmberWarning.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberWarning)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(20.dp))
                        Text(
                            text = "Achtung (Einzelbild-Rekonstruktion): Maße und Wandstärken sind geschätzt. Bitte vor dem Druckabruf genau prüfen!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AmberWarning,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Quick Alignment and Inspection Toolbar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = highlightOverhangs,
                    onClick = { highlightOverhangs = !highlightOverhangs },
                    label = { Text("Überhänge (>45°)", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (highlightOverhangs) CyberMagenta else TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberMagenta.copy(alpha = 0.2f),
                        selectedLabelColor = CyberMagenta,
                        containerColor = CyberSurfaceVariant,
                        labelColor = TextSecondary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (highlightOverhangs) CyberMagenta else CyberBorder)
                )

                FilterChip(
                    selected = false,
                    onClick = { centerModel() },
                    label = { Text("Zentrieren", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.CenterFocusStrong, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(containerColor = CyberSurfaceVariant, labelColor = TextCyan),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
                )

                FilterChip(
                    selected = false,
                    onClick = { snapToBed() },
                    label = { Text("Auf Bett", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.VerticalAlignBottom, contentDescription = null, tint = TechLime, modifier = Modifier.size(14.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(containerColor = CyberSurfaceVariant, labelColor = TechLime),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
                )
            }

            // Transformations: Scale Slider
            CyberCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Skalierung", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Text(
                            "%.1f%% (%.2fx)".format(scaleSlider * 100f, scaleSlider),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    }

                    Slider(
                        value = scaleSlider,
                        onValueChange = { updateScale(it) },
                        valueRange = 0.2f..3.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = CyberBorder
                        ),
                        modifier = Modifier.testTag("scale_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("50%", color = TextSecondary, fontSize = 11.sp)
                        Text("100% (1:1 Original)", color = TextCyan, fontSize = 11.sp)
                        Text("200%", color = TextSecondary, fontSize = 11.sp)
                        Text("300%", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }

            // Mesh Quality & Diagnostics Card
            CyberCard(borderColor = CyberBorderGlow) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Mesh-Prüfung & Diagnose", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        StatusPill(
                            text = if (currentMesh.fitsInCr10SPro()) "Passend für CR-10S Pro" else "Zu groß!",
                            statusType = if (currentMesh.fitsInCr10SPro()) StatusType.SUCCESS else StatusType.ERROR
                        )
                    }

                    // Diagnostic Metrics Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricBadge(
                            label = "Wasserdicht (Manifold)",
                            value = if (currentMesh.isWatertight && currentMesh.nonManifoldEdges == 0) "Ja (Geschlossen)" else "${currentMesh.nonManifoldEdges} offene Kanten",
                            modifier = Modifier.weight(1f),
                            accentColor = if (currentMesh.isWatertight) TechLime else CyberError
                        )

                        MetricBadge(
                            label = "Min. Wandstärke",
                            value = "%.1f mm".format(currentMesh.estimatedMinWallThicknessMm * currentMesh.scale),
                            modifier = Modifier.weight(1f),
                            accentColor = if (currentMesh.estimatedMinWallThicknessMm * currentMesh.scale >= 0.8f) TechLime else AmberWarning
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricBadge(
                            label = "Überhänge (>45°)",
                            value = "%.1f%% der Fläche".format(currentMesh.overhangPercentage),
                            modifier = Modifier.weight(1f),
                            accentColor = if (currentMesh.overhangPercentage < 10f) TechLime else CyberMagenta
                        )

                        MetricBadge(
                            label = "Berechnetes Volumen",
                            value = "%.2f cm³".format(currentMesh.volumeCm3),
                            modifier = Modifier.weight(1f),
                            accentColor = ElectricBlue
                        )
                    }

                    // Exact Dimensions in mm
                    Surface(
                        color = CyberSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Abmessungen (X × Y × Z):", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text(
                                "%.1f × %.1f × %.1f mm".format(currentMesh.sizeX, currentMesh.sizeY, currentMesh.sizeZ),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextCyan
                            )
                        }
                    }
                }
            }

            // Export STL & Import alternative
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CyberSecondaryButton(
                    text = "STL exportieren",
                    onClick = {
                        val stl = StlGenerator.exportToAsciiStl(currentMesh)
                        Toast.makeText(context, "STL-Datei bereit (${stl.lines().size} Zeilen)", Toast.LENGTH_SHORT).show()
                    },
                    icon = Icons.Default.Download,
                    modifier = Modifier.weight(1f),
                    testTag = "export_stl_button"
                )

                CyberSecondaryButton(
                    text = "Anderes Modell laden",
                    onClick = onImportNewStl,
                    icon = Icons.Default.FolderOpen,
                    modifier = Modifier.weight(1f),
                    testTag = "import_another_stl_button"
                )
            }

            // Proceed to Slicing Button
            CyberPrimaryButton(
                text = "Weiter zur Druckvorbereitung (Slicing)",
                onClick = {
                    val updatedProject = project.copy(mesh = currentMesh)
                    onProceedToSlice(updatedProject)
                },
                icon = Icons.Default.PrecisionManufacturing,
                modifier = Modifier.fillMaxWidth(),
                testTag = "proceed_to_slice_button"
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
