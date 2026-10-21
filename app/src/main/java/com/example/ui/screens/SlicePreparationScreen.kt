package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AdhesionType
import com.example.model.FilamentType
import com.example.model.InfillPattern
import com.example.model.Project
import com.example.model.SlicingConfig
import com.example.model.SupportType
import com.example.ui.components.WorkflowStepIndicator
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberPrimaryButton
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
fun SlicePreparationScreen(
    project: Project,
    onNavigateBack: () -> Unit,
    onStartSlicing: (Project, SlicingConfig) -> Unit
) {
    BackHandler { onNavigateBack() }

    var config by remember { mutableStateOf(project.slicingConfig) }
    var selectedFilament by remember { mutableStateOf(config.filamentType) }
    var nozzleTemp by remember { mutableIntStateOf(config.nozzleTempC) }
    var bedTemp by remember { mutableIntStateOf(config.bedTempC) }
    var layerHeight by remember { mutableFloatStateOf(config.layerHeightMm) }
    var infillPercent by remember { mutableIntStateOf(config.infillDensityPercent) }
    var infillPattern by remember { mutableStateOf(config.infillPattern) }
    var supportType by remember { mutableStateOf(config.supportType) }
    var adhesionType by remember { mutableStateOf(config.adhesionType) }
    var isConfirmed by remember { mutableStateOf(config.isUserConfirmed) }

    var expandedFilamentDropdown by remember { mutableStateOf(false) }
    var expandedInfillDropdown by remember { mutableStateOf(false) }

    val mesh = project.mesh
    val fitsInPrinter = mesh?.fitsInCr10SPro() ?: true

    // Dynamic calculations
    val estimatedFilamentWeight = if (mesh != null) {
        mesh.volumeCm3 * (infillPercent / 100f * 0.7f + 0.3f) * selectedFilament.densityGPerCm3
    } else 20f

    val estimatedHours = if (mesh != null) {
        val layers = (mesh.sizeZ / layerHeight).toInt()
        val totalMins = (layers * 0.8f + (estimatedFilamentWeight * 3f)).toInt().coerceAtLeast(15)
        val h = totalMins / 60
        val m = totalMins % 60
        if (h > 0) "${h}h ${m}m" else "${m}m"
    } else "45m"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Druckvorbereitung & Slicer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            "Creality CR-10S Pro Profil",
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
            WorkflowStepIndicator(currentStep = 3)

            // Printer Profile Header Card
            CyberCard(borderColor = CyberBorderGlow) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Profil: Creality CR-10S Pro",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        StatusPill(
                            text = if (isConfirmed) "Vom Nutzer bestätigt" else "Vorschlagswerte",
                            statusType = if (isConfirmed) StatusType.SUCCESS else StatusType.WARNING
                        )
                    }

                    Text(
                        text = "Standard-Druckbett 300 × 300 × 400 mm, Bowden Extruder, BLTouch automatisches Nivelliersystem (Matrix-Leveling).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricBadge("Druckbett", "300 x 300 mm", Modifier.weight(1f), ElectricBlue)
                        MetricBadge("Z-Höhe max", "400 mm", Modifier.weight(1f), ElectricBlue)
                        MetricBadge("Bowden Retract", "5.0 mm @ 45 mm/s", Modifier.weight(1.3f), NeonCyan)
                    }
                }
            }

            // Material & Temperature Settings
            CyberCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Material & Temperaturen", style = MaterialTheme.typography.titleMedium, color = TextPrimary)

                    // Filament Type Selector Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedFilamentDropdown,
                        onExpandedChange = { expandedFilamentDropdown = it }
                    ) {
                        OutlinedTextField(
                            value = selectedFilament.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Filamenttyp") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFilamentDropdown) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("filament_dropdown"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = CyberBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = expandedFilamentDropdown,
                            onDismissRequest = { expandedFilamentDropdown = false },
                            modifier = Modifier.background(CyberSurfaceCard)
                        ) {
                            FilamentType.entries.forEach { filament ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(filament.displayName, color = TextPrimary, fontWeight = FontWeight.Bold)
                                            Text(filament.description, color = TextSecondary, fontSize = 11.sp)
                                        }
                                    },
                                    onClick = {
                                        selectedFilament = filament
                                        nozzleTemp = filament.defaultNozzleTemp
                                        bedTemp = filament.defaultBedTemp
                                        expandedFilamentDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Temperature Sliders
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Düsentemperatur (Hotend)", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text("$nozzleTemp °C", style = MaterialTheme.typography.labelLarge, color = NeonCyan)
                        }
                        Slider(
                            value = nozzleTemp.toFloat(),
                            onValueChange = { nozzleTemp = it.toInt() },
                            valueRange = 180f..260f,
                            colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan, inactiveTrackColor = CyberBorder)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Druckbett-Temperatur", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text("$bedTemp °C", style = MaterialTheme.typography.labelLarge, color = TechLime)
                        }
                        Slider(
                            value = bedTemp.toFloat(),
                            onValueChange = { bedTemp = it.toInt() },
                            valueRange = 40f..110f,
                            colors = SliderDefaults.colors(thumbColor = TechLime, activeTrackColor = TechLime, inactiveTrackColor = CyberBorder)
                        )
                    }
                }
            }

            // Quality & Slicer Settings
            CyberCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Qualität & Schichthöhe", style = MaterialTheme.typography.titleMedium, color = TextPrimary)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(0.12f to "Fein (0.12)", 0.20f to "Standard (0.20)", 0.28f to "Entwurf (0.28)").forEach { (lh, title) ->
                            FilterChip(
                                selected = layerHeight == lh,
                                onClick = { layerHeight = lh },
                                label = { Text(title, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                                    selectedLabelColor = NeonCyan,
                                    containerColor = CyberSurfaceVariant,
                                    labelColor = TextSecondary
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (layerHeight == lh) NeonCyan else CyberBorder)
                            )
                        }
                    }

                    // Infill Density Slider
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Infill-Füllung: $infillPercent%", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text(infillPattern.displayName, style = MaterialTheme.typography.labelSmall, color = TextCyan)
                        }
                        Slider(
                            value = infillPercent.toFloat(),
                            onValueChange = { infillPercent = it.toInt() },
                            valueRange = 10f..100f,
                            colors = SliderDefaults.colors(thumbColor = ElectricBlue, activeTrackColor = ElectricBlue, inactiveTrackColor = CyberBorder)
                        )
                    }

                    // Support Mode Selector
                    Text("Stützstrukturen (Support)", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SupportType.entries.forEach { st ->
                            FilterChip(
                                selected = supportType == st,
                                onClick = { supportType = st },
                                label = { Text(st.name.replace("_", " "), fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyberMagenta.copy(alpha = 0.2f),
                                    selectedLabelColor = CyberMagenta,
                                    containerColor = CyberSurfaceVariant,
                                    labelColor = TextSecondary
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (supportType == st) CyberMagenta else CyberBorder)
                            )
                        }
                    }

                    // Bed Adhesion
                    Text("Druckbetthaftung (Adhesion)", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AdhesionType.entries.forEach { ad ->
                            FilterChip(
                                selected = adhesionType == ad,
                                onClick = { adhesionType = ad },
                                label = { Text(ad.name, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TechLime.copy(alpha = 0.2f),
                                    selectedLabelColor = TechLime,
                                    containerColor = CyberSurfaceVariant,
                                    labelColor = TextSecondary
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (adhesionType == ad) TechLime else CyberBorder)
                            )
                        }
                    }
                }
            }

            // Slicing Estimation Card
            CyberCard(borderColor = CyberBorderGlow) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Berechnete Schätzwerte", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Text("(Schätzung, keine Garantie)", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricBadge("Geschätzte Druckzeit", estimatedHours, Modifier.weight(1f), NeonCyan)
                        MetricBadge("Materialbedarf", "ca. %.1f g".format(estimatedFilamentWeight), Modifier.weight(1f), TechLime)
                        MetricBadge("Materialkosten", "ca. %.2f €".format(estimatedFilamentWeight * 0.024f), Modifier.weight(1f), ElectricBlue)
                    }
                }
            }

            // Pre-Slice Validation & Confirmation
            CyberCard(borderColor = if (isConfirmed) TechLime else AmberWarning) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Checkbox(
                        checked = isConfirmed,
                        onCheckedChange = { isConfirmed = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = TechLime,
                            checkmarkColor = CyberBackground,
                            uncheckedColor = AmberWarning
                        ),
                        modifier = Modifier.testTag("confirm_settings_checkbox")
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Drucker- und Materialeinstellungen bestätigen",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Ich habe bestätigt, dass die Temperaturen (${nozzleTemp}°C / ${bedTemp}°C) und Maße mit meinem Creality CR-10S Pro kompatibel sind.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Slicing Action Button
            CyberPrimaryButton(
                text = "Modell jetzt für CR-10S Pro slicen",
                onClick = {
                    val updatedConfig = config.copy(
                        filamentType = selectedFilament,
                        nozzleTempC = nozzleTemp,
                        bedTempC = bedTemp,
                        layerHeightMm = layerHeight,
                        infillDensityPercent = infillPercent,
                        infillPattern = infillPattern,
                        supportType = supportType,
                        adhesionType = adhesionType,
                        isUserConfirmed = isConfirmed
                    )
                    onStartSlicing(project, updatedConfig)
                },
                enabled = fitsInPrinter,
                icon = Icons.Default.PrecisionManufacturing,
                modifier = Modifier.fillMaxWidth(),
                testTag = "start_slicing_button"
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
