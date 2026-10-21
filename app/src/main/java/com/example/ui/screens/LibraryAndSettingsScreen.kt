package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
fun LibraryAndSettingsScreen(
    projects: List<Project>,
    onNavigateBack: () -> Unit,
    onSelectProject: (Project) -> Unit,
    onDeleteProject: (String) -> Unit
) {
    BackHandler { onNavigateBack() }
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) }
    var remoteCuraServerUrl by remember { mutableStateOf("https://slicer.sandros-labor.local:8080/api/slice") }
    var remotePhotogrammetryUrl by remember { mutableStateOf("https://meshroom.sandros-labor.local:8443/reconstruct") }
    var useRemoteBackend by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Labor-Bibliothek & Einstellungen",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
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
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = CyberBackground,
                contentColor = NeonCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = NeonCyan
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Projekte (${projects.size})", fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Drucker & Slicer", fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Maker-Hilfe", fontSize = 13.sp) }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // Projects tab
                        Text("Gespeicherte 3D-Projekte", style = MaterialTheme.typography.titleMedium, color = TextPrimary)

                        projects.forEach { project ->
                            CyberCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { onSelectProject(project) }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(project.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                            if (project.isDemoProject) {
                                                StatusPill("Demo", StatusType.INFO)
                                            }
                                        }
                                        Text(
                                            text = if (project.mesh != null) "Mesh: %.1f x %.1f mm".format(project.mesh.sizeX, project.mesh.sizeY) else "${project.photoCount} Fotos",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                        StatusPill(project.status.label, StatusType.SUCCESS)
                                    }

                                    IconButton(onClick = { onDeleteProject(project.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Löschen", tint = CyberError)
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // Printer & Backend Settings tab
                        CyberCard(borderColor = CyberBorderGlow) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Standard-3D-Drucker: Creality CR-10S Pro", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                Text("Konfigurierte Hardware-Parameter", style = MaterialTheme.typography.labelSmall, color = TextSecondary)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    MetricBadge("Bett X/Y", "300 x 300 mm", Modifier.weight(1f), NeonCyan)
                                    MetricBadge("Höhe Z", "400 mm", Modifier.weight(1f), ElectricBlue)
                                    MetricBadge("Leveling", "BLTouch", Modifier.weight(1f), TechLime)
                                }
                            }
                        }

                        // Google Drive Sync Settings Card
                        CyberCard(borderColor = NeonCyan) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Google Drive Auto-Sync", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Text("Automatisierter Import von 3D-Webseiten", style = MaterialTheme.typography.labelSmall, color = TechLime)
                                    }
                                    StatusPill("OAuth Aktiv", StatusType.SUCCESS)
                                }

                                Text(
                                    "Konfigurierter Zielordner für die 10 täglichen Top-Modelle von MakerWorld, Printables und Thingiverse:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )

                                Surface(
                                    color = CyberSurfaceVariant,
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("📁 Projekte / 3D-Drucker", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = NeonCyan)
                                        Text("drive.readonly", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    }
                                }

                                Text(
                                    "Der Ordner wird täglich automatisch abgefragt und neue .stl/.3mf Dateien werden im Dashboard bereitgestellt.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Slicer Backend Interface Card
                        CyberCard {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Externer Slicer-Dienst (CuraEngine)", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                        Text("Verbinde ein eigenes Backend (z.B. OctoPrint/Moonraker)", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, fontSize = 11.sp)
                                    }
                                    Switch(
                                        checked = useRemoteBackend,
                                        onCheckedChange = { useRemoteBackend = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = ElectricBlue)
                                    )
                                }

                                if (useRemoteBackend) {
                                    OutlinedTextField(
                                        value = remoteCuraServerUrl,
                                        onValueChange = { remoteCuraServerUrl = it },
                                        label = { Text("CuraEngine Server URL") },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = NeonCyan,
                                            unfocusedBorderColor = CyberBorder,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        )
                                    )
                                } else {
                                    Surface(
                                        color = CyberSurfaceVariant,
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
                                    ) {
                                        Text(
                                            "Aktuell aktiv: Sandro CR-10S Pro Native Slicer. Vollständig lokaler Betrieb ohne externe Server-Abhängigkeit.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TechLime,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Photogrammetry Backend Settings
                        CyberCard {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Photogrammetrie-Schnittstelle", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                Text(
                                    "Sandros Print Labor unterstützt die Übergabe von Fotobildserien an externe Rekonstruktions-Cluster (z.B. AliceVision/Meshroom oder COLMAP über REST-API).",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )

                                OutlinedTextField(
                                    value = remotePhotogrammetryUrl,
                                    onValueChange = { remotePhotogrammetryUrl = it },
                                    label = { Text("Photogrammetrie API-Endpoint") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NeonCyan,
                                        unfocusedBorderColor = CyberBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )

                                CyberSecondaryButton(
                                    text = "Verbindung testen",
                                    onClick = {
                                        Toast.makeText(context, "Schnittstelle syntaktisch bereit (Demo-Modus aktiv)", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    2 -> {
                        // Maker Guide & Troubleshooting for CR-10S Pro
                        Text("Maker-Hilfe für Creality CR-10S Pro", style = MaterialTheme.typography.titleMedium, color = TextPrimary)

                        HelpSection(
                            title = "1. BLTouch & Matrix Auto-Bed-Leveling",
                            content = "Der CR-10S Pro verfügt über einen induktiven/kapazitiven bzw. BLTouch Sensor. Stelle vor dem Druck sicher, dass das Bett per 4-Punkt-Schrauben manuell grob vorjustiert ist, damit das 16-Punkt-Matrix-Leveling optimal ausgleicht."
                        )

                        HelpSection(
                            title = "2. Z-Offset Feinjustierung",
                            content = "Tippe während des Drucks des ersten Layers auf 'Adjust' am Touchscreen, um den Z-Offset in 0.05mm Schritten so einzustellen, dass die Linien leicht verpresst ('squished') werden, ohne die Düse zu blockieren."
                        )

                        HelpSection(
                            title = "3. Bowden Extruder Retraction",
                            content = "Durch den langen Bowdenschlauch benötigt der CR-10S Pro 5.0 bis 6.0 mm Rückzug (Retraction) bei 45 mm/s. Zu kurze Werte führen zu Stringing (Fäden), zu lange zu Verstopfungen im Heatbreak."
                        )

                        HelpSection(
                            title = "4. SD-Karten Formatierung",
                            content = "Verwende MicroSD-Karten bis maximal 32 GB, formatiert im FAT32-Dateisystem mit 4096 Byte Sektorgröße. Halte Dateinamen unter 20 Zeichen ohne Leerzeichen oder Umlaute."
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun HelpSection(title: String, content: String) {
    CyberCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = NeonCyan)
            Text(content, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, lineHeight = 18.sp)
        }
    }
}
