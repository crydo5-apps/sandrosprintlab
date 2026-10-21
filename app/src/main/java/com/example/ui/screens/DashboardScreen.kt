package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DriveModelItem
import com.example.model.Mesh3D
import com.example.model.Project
import com.example.model.ProjectStatus
import com.example.service.drive.GoogleDriveSyncService
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberSecondaryButton
import com.example.ui.components.GoogleDriveExplorerSheet
import com.example.ui.components.MetricBadge
import com.example.ui.components.StatusPill
import com.example.ui.components.StatusType
import com.example.ui.theme.Amber
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.ColdGray
import com.example.ui.theme.DividerColor
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.IceWhite
import com.example.ui.theme.LabBlack
import com.example.ui.theme.LabCard
import com.example.ui.theme.LabRaised
import com.example.ui.theme.LabSurface
import com.example.ui.theme.LaserCyan
import com.example.ui.theme.NeonMint
import com.example.ui.theme.PlasmaMagenta
import com.example.ui.theme.SignalRed
import com.example.ui.theme.SteelGray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    projects: List<Project>,
    driveService: GoogleDriveSyncService,
    onNewProject: () -> Unit,
    onImportStl: () -> Unit,
    onImportDriveModel: (DriveModelItem, Mesh3D) -> Unit,
    onSelectProject: (Project) -> Unit,
    onOpenSettings: () -> Unit
) {
    var showDriveSheet by remember { mutableStateOf(false) }
    val driveModels by driveService.driveModels.collectAsState()
    val targetFolder by driveService.targetFolderPath.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Isometric Hologram Cube Icon
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Brush.linearGradient(listOf(LaserCyan, ElectricBlue))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ViewInAr,
                                contentDescription = "Logo",
                                tint = LabBlack,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Sandros Print Labor",
                                color = IceWhite,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "IDEEN. LAYER FÜR LAYER. REALITÄT.",
                                color = SteelGray,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                },
                actions = {
                    // System Secure Badge matching mockup
                    Surface(
                        color = LabCard,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, NeonMint.copy(alpha = 0.6f)),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "System Secure",
                                tint = NeonMint,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "System Secure",
                                color = NeonMint,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LabBlack
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = LabSurface,
                contentColor = ColdGray,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Stay on Start */ },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Start") },
                    label = { Text("Start", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LaserCyan,
                        selectedTextColor = LaserCyan,
                        indicatorColor = LabRaised,
                        unselectedIconColor = SteelGray,
                        unselectedTextColor = ColdGray
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onOpenSettings,
                    icon = { Icon(Icons.Default.FolderOpen, contentDescription = "Projekte") },
                    label = { Text("Projekte", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LaserCyan,
                        selectedTextColor = LaserCyan,
                        indicatorColor = LabRaised,
                        unselectedIconColor = SteelGray,
                        unselectedTextColor = ColdGray
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onOpenSettings,
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Einstellungen") },
                    label = { Text("Einstellungen", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LaserCyan,
                        selectedTextColor = LaserCyan,
                        indicatorColor = LabRaised,
                        unselectedIconColor = SteelGray,
                        unselectedTextColor = ColdGray
                    )
                )
            }
        },
        containerColor = LabBlack
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Hero Greeting: "Willkommen, Sandro"
            item {
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Willkommen, Sandro",
                            color = IceWhite,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Schön, dass du wieder da bist.\nBereit für neue Ideen?",
                            color = ColdGray,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }

                    // Futuristic 3D Printer / Cube Art Box
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.radialGradient(listOf(ElectricBlue.copy(alpha = 0.3f), LabCard)))
                            .border(1.dp, LaserCyan.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.PrecisionManufacturing,
                                contentDescription = null,
                                tint = LaserCyan,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "DESIGN · DRUCK",
                                color = LaserCyan,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }

            // 2. Prominent Neon Cyan CTA Button matching Mockup
            item {
                Surface(
                    onClick = onNewProject,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("prominent_new_project_button"),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(LaserCyan, ElectricBlue)
                                ),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(LabBlack.copy(alpha = 0.22f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = null,
                                        tint = LabBlack,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Text(
                                    text = "Neues Modell erstellen",
                                    color = LabBlack,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = LabBlack,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // 3. "Letztes Projekt" Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Letztes Projekt",
                        color = IceWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "Alle Projekte >",
                        color = LaserCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onOpenSettings() }
                    )
                }
            }

            item {
                val latestProject = projects.firstOrNull()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, DividerColor, RoundedCornerShape(18.dp))
                        .clickable {
                            if (latestProject != null) onSelectProject(latestProject)
                            else onNewProject()
                        },
                    colors = CardDefaults.cardColors(containerColor = LabCard)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // 3D Thumbnail
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.verticalGradient(listOf(LabRaised, LabBlack)))
                                .border(1.dp, LaserCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ViewInAr,
                                contentDescription = null,
                                tint = LaserCyan,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        // Project Details
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = latestProject?.title ?: "3DBenchy",
                                color = IceWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "PLA  |  0,20 mm  |  1h 56min",
                                color = ColdGray,
                                fontSize = 12.sp
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = NeonMint,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = if (latestProject != null) latestProject.status.label else "Druck abgeschlossen",
                                    color = NeonMint,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = " · Gestern, 18:24",
                                    color = SteelGray,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Optionen",
                            tint = ColdGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // 4. "Mein 3D-Drucker" Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mein 3D-Drucker",
                        color = IceWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "Geräte verwalten >",
                        color = LaserCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onOpenSettings() }
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, DividerColor, RoundedCornerShape(18.dp)),
                    colors = CardDefaults.cardColors(containerColor = LabCard)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Printer Icon / Gantry Art
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Brush.verticalGradient(listOf(LabRaised, LabBlack)))
                                    .border(1.dp, ElectricBlue.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Print,
                                    contentDescription = "Creality CR-10S Pro",
                                    tint = LaserCyan,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = "Creality CR-10S Pro",
                                    color = IceWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Offline · Standby",
                                    color = SteelGray,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Telemetry Row matching Mockup
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PrinterTelemetryPill(
                                icon = Icons.Default.DeviceThermostat,
                                label = "Druckbett",
                                value = "-- °C",
                                modifier = Modifier.weight(1f)
                            )
                            PrinterTelemetryPill(
                                icon = Icons.Default.PrecisionManufacturing,
                                label = "Düse",
                                value = "-- °C",
                                modifier = Modifier.weight(1f)
                            )
                            PrinterTelemetryPill(
                                icon = Icons.Default.Air,
                                label = "Lüfter",
                                value = "-- %",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // SD Card Subcard matching Mockup
                        Surface(
                            color = LabRaised,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, DividerColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.SdCard,
                                    contentDescription = "SD Karte",
                                    tint = SignalRed,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = "SD-Karte nicht verbunden",
                                        color = IceWhite,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Bitte SD-Karte einlegen, um zu drucken.",
                                        color = ColdGray,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Google Drive Cloud Sync Card ("Projekte/3D-Drucker")
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, LaserCyan.copy(alpha = 0.5f), RoundedCornerShape(18.dp)),
                    colors = CardDefaults.cardColors(containerColor = LabCard)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(LaserCyan.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.CloudDone,
                                        contentDescription = null,
                                        tint = LaserCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Google Drive: $targetFolder",
                                        color = IceWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "Täglich 10 Top-Modelle synchronisiert",
                                        color = NeonMint,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            StatusPill("10 Modelle", StatusType.SUCCESS)
                        }

                        Text(
                            text = "Automatisch von MakerWorld, Printables & Thingiverse in deinen Drive-Ordner übertragen.",
                            color = ColdGray,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        // Strip of drive models
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(driveModels.take(5)) { driveItem ->
                                Surface(
                                    color = LabRaised,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, DividerColor),
                                    modifier = Modifier
                                        .width(170.dp)
                                        .clickable { showDriveSheet = true }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = driveItem.sourceWebsite,
                                            color = when (driveItem.sourceWebsite) {
                                                "MakerWorld" -> NeonMint
                                                "Printables" -> Amber
                                                else -> ElectricBlue
                                            },
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = driveItem.name,
                                            color = IceWhite,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = driveItem.formattedSize,
                                            color = SteelGray,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }

                        CyberSecondaryButton(
                            text = "Drive-Modelle ansehen & importieren",
                            onClick = { showDriveSheet = true },
                            icon = Icons.Default.CloudDownload,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "browse_drive_button"
                        )
                    }
                }
            }

            // 6. Lokale STL / 3MF Import & Quick Actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CyberSecondaryButton(
                        text = "Lokale STL laden",
                        onClick = onImportStl,
                        icon = Icons.Default.FolderOpen,
                        modifier = Modifier.weight(1f),
                        testTag = "quick_stl_import"
                    )
                    CyberSecondaryButton(
                        text = "Labor-Settings",
                        onClick = onOpenSettings,
                        icon = Icons.Default.Settings,
                        modifier = Modifier.weight(1f),
                        testTag = "quick_settings"
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Google Drive Explorer Bottom Sheet
    if (showDriveSheet) {
        GoogleDriveExplorerSheet(
            driveService = driveService,
            onDismiss = { showDriveSheet = false },
            onModelSelectedForLabor = { item, mesh ->
                onImportDriveModel(item, mesh)
            }
        )
    }
}

@Composable
private fun PrinterTelemetryPill(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = LabRaised,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, DividerColor),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = LaserCyan,
                modifier = Modifier.size(16.dp)
            )
            Column {
                Text(label, color = SteelGray, fontSize = 9.sp)
                Text(value, color = IceWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
