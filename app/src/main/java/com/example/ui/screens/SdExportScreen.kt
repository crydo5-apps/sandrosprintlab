package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.model.Project
import com.example.model.ProjectStatus
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
import java.io.OutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SdExportScreen(
    project: Project,
    onNavigateBack: () -> Unit,
    onExportCompleted: (Project, String) -> Unit,
    onFinishReturnHome: () -> Unit
) {
    BackHandler { onNavigateBack() }
    val context = LocalContext.current

    val rawTitle = project.title.replace(" ", "_").replace(Regex("[^a-zA-Z0-9_-]"), "")
    val defaultFilename = "CR10S_${rawTitle}_${project.slicingConfig.filamentType.name}.gcode"

    var exportLocationUri by remember { mutableStateOf<Uri?>(null) }
    var writtenBytesCount by remember { mutableLongStateOf(0L) }
    var isVerifiedReadable by remember { mutableStateOf(false) }
    var exportStatusText by remember { mutableStateOf<String?>(null) }
    var isWriting by remember { mutableStateOf(false) }

    // SAF CreateDocument Launcher
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/x-gcode")
    ) { uri ->
        if (uri != null) {
            isWriting = true
            try {
                val gcodeContent = project.gcodeResult?.fullGcodeContent ?: "; Creality CR-10S Pro G-Code\n"
                val outputStream: OutputStream? = context.contentResolver.openOutputStream(uri)
                outputStream?.use { stream ->
                    val bytes = gcodeContent.toByteArray(Charsets.UTF_8)
                    stream.write(bytes)
                    stream.flush()
                    writtenBytesCount = bytes.size.toLong()
                }

                // Verify file readability
                val verifyInputStream = context.contentResolver.openInputStream(uri)
                val testBytes = ByteArray(100)
                val readCount = verifyInputStream?.use { it.read(testBytes) } ?: 0
                isVerifiedReadable = readCount > 0

                exportLocationUri = uri
                exportStatusText = "Erfolgreich auf SD-Karte / Speicher geschrieben"
                isWriting = false

                val updatedProj = project.copy(
                    status = ProjectStatus.EXPORTED_SD,
                    exportedSdPath = uri.toString()
                )
                onExportCompleted(updatedProj, uri.toString())

            } catch (e: Exception) {
                isWriting = false
                exportStatusText = "Schreibfehler: ${e.localizedMessage}"
                Toast.makeText(context, "Fehler beim Schreiben: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "SD-Karten-Export",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            "Creality CR-10S Pro Bereitstellung",
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
            WorkflowStepIndicator(currentStep = 4)
            // File & SAF Trigger Card
            CyberCard(glowEffect = exportLocationUri != null) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.SdCard, contentDescription = null, tint = TechLime, modifier = Modifier.size(22.dp))
                            Text(
                                "Dateiname & Zielspeicher",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        StatusPill(
                            text = if (exportLocationUri != null) "Gespeichert" else "Bereit zum Export",
                            statusType = if (exportLocationUri != null) StatusType.SUCCESS else StatusType.INFO
                        )
                    }

                    // Formatted G-Code Filename Box
                    Surface(
                        color = CyberSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Vorgeschlagener Dateiname:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Text(
                                    defaultFilename,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan
                                )
                            }
                            Text(".GCODE", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                    }

                    // Transparent SAF / OTG storage notice
                    Text(
                        text = "Android schreibt über das Storage Access Framework direkt auf die eingelegte MicroSD-Karte oder ein per USB-C OTG angeschlossenes Lesegerät.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    // Export Primary Button
                    CyberPrimaryButton(
                        text = if (exportLocationUri != null) "Erneut auf SD-Karte speichern" else "G-Code auf SD-Karte / OTG speichern",
                        onClick = { createDocumentLauncher.launch(defaultFilename) },
                        icon = Icons.Default.Save,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "save_to_sd_button"
                    )
                }
            }

            // Post-Export Status & Verification Box
            if (exportLocationUri != null) {
                CyberCard(borderColor = TechLime) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = TechLime, modifier = Modifier.size(20.dp))
                            Text("Export verifiziert", style = MaterialTheme.typography.titleMedium, color = TechLime)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetricBadge("Geschrieben", "%.2f MB".format(writtenBytesCount / (1024f * 1024f)), Modifier.weight(1f), TechLime)
                            MetricBadge("Leseprüfung", if (isVerifiedReadable) "Gültig (OK)" else "Prüfung...", Modifier.weight(1f), NeonCyan)
                            MetricBadge("Format", "Marlin G-Code", Modifier.weight(1f), ElectricBlue)
                        }

                        Text(
                            text = "Speicherort: ${exportLocationUri?.lastPathSegment ?: exportLocationUri.toString()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Step-by-Step CR-10S Pro Hardware Instructions
            CyberCard(borderColor = CyberBorderGlow) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Druckanleitung für Creality CR-10S Pro",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    HardwareStepItem(
                        stepNumber = 1,
                        icon = Icons.Default.SdCard,
                        title = "SD-Karte sicher auswerfen",
                        description = "Entferne die MicroSD-Karte oder das USB-OTG Lesegerät sicher vom Android-Gerät."
                    )

                    HardwareStepItem(
                        stepNumber = 2,
                        icon = Icons.Default.Usb,
                        title = "In CR-10S Pro einsetzen",
                        description = "Schiebe die MicroSD-Karte in den Kartenslot des Druckers (rechts am Basisgehäuse, Kontakte nach oben)."
                    )

                    HardwareStepItem(
                        stepNumber = 3,
                        icon = Icons.Default.PowerSettingsNew,
                        title = "Drucker einschalten",
                        description = "Kippschalter auf der Rückseite des Druckers auf 'ON' stellen."
                    )

                    HardwareStepItem(
                        stepNumber = 4,
                        icon = Icons.Default.TouchApp,
                        title = "Druck am Touchscreen starten",
                        description = "Tippe auf dem Farb-Touchscreen auf 'Print', wähle '$defaultFilename' und bestätige."
                    )

                    HardwareStepItem(
                        stepNumber = 5,
                        icon = Icons.Default.Print,
                        title = "Erste Schicht kontrollieren",
                        description = "Beobachte das Aufheizen des Betts (${project.slicingConfig.bedTempC}°C) und die automatische BLTouch-Nivellierung."
                    )
                }
            }

            // Explicit Reality Disclaimer Callout
            Surface(
                color = CyberSurfaceVariant,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = TextCyan, modifier = Modifier.size(18.dp))
                    Text(
                        text = "Hinweis: Der Druck läuft nicht automatisch los. Der Creality CR-10S Pro benötigt das manuelle Starten über das Touchscreen-Menü am Drucker.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            // Finish and Return to Dashboard
            CyberSecondaryButton(
                text = "Fertigstellen & zum Dashboard",
                onClick = onFinishReturnHome,
                icon = Icons.Default.DoneAll,
                modifier = Modifier.fillMaxWidth(),
                testTag = "finish_return_home_button"
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HardwareStepItem(
    stepNumber: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(CyberSurfaceVariant)
                .border(1.dp, NeonCyan, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$stepNumber",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = NeonCyan
            )
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelLarge, color = TextPrimary)
            Text(text = description, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, fontSize = 11.sp)
        }
    }
}
