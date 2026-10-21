package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DriveModelItem
import com.example.model.Mesh3D
import com.example.service.drive.GoogleDriveSyncService
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberBorderGlow
import com.example.ui.theme.CyberMagenta
import com.example.ui.theme.CyberSurfaceCard
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TechLime
import com.example.ui.theme.TextCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleDriveExplorerSheet(
    driveService: GoogleDriveSyncService,
    onDismiss: () -> Unit,
    onModelSelectedForLabor: (DriveModelItem, Mesh3D) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val driveModels by driveService.driveModels.collectAsState()
    val targetFolder by driveService.targetFolderPath.collectAsState()
    val lastSync by driveService.lastSyncDate.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedPlatformFilter by remember { mutableStateOf("Alle") }
    var downloadingItemId by remember { mutableStateOf<String?>(null) }
    var downloadProgressText by remember { mutableStateOf("") }
    var downloadProgressPercent by remember { mutableStateOf(0) }

    val filteredModels = if (selectedPlatformFilter == "Alle") {
        driveModels
    } else {
        driveModels.filter { it.sourceWebsite.equals(selectedPlatformFilter, ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CyberSurfaceCard,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
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
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ElectricBlue.copy(alpha = 0.2f))
                            .border(1.dp, NeonCyan, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(22.dp))
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Google Drive Synchronisation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                            StatusPill("Verbunden", StatusType.SUCCESS)
                        }
                        Text("Ordner: $targetFolder", style = MaterialTheme.typography.labelSmall, color = TextCyan)
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Schließen", tint = TextSecondary)
                }
            }

            // Sync Information Card
            Surface(
                color = CyberSurfaceVariant,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Täglicher Download-Job aktiv",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "10 neueste & beliebteste Modelle aus 3 Plattformen automatisch im Ordner '$targetFolder' abgelegt ($lastSync).",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = {
                            driveService.loadDailySyncedModels()
                            Toast.makeText(context, "Google Drive Ordner aktualisiert (10 Modelle synchronisiert)", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Aktualisieren", tint = NeonCyan)
                    }
                }
            }

            // Platform Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Alle", "MakerWorld", "Printables", "Thingiverse").forEach { platform ->
                    val isSelected = selectedPlatformFilter == platform
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedPlatformFilter = platform },
                        label = { Text(platform, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                            selectedLabelColor = NeonCyan,
                            containerColor = CyberBackground,
                            labelColor = TextSecondary
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) NeonCyan else CyberBorder
                        )
                    )
                }
            }

            // Downloading Progress Banner
            if (downloadingItemId != null) {
                Surface(
                    color = CyberBackground,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Download aus Google Drive...", style = MaterialTheme.typography.labelLarge, color = NeonCyan)
                            Text("$downloadProgressPercent%", style = MaterialTheme.typography.labelSmall, color = TextCyan)
                        }
                        LinearProgressIndicator(
                            progress = { downloadProgressPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = NeonCyan,
                            trackColor = CyberSurfaceVariant
                        )
                        Text(downloadProgressText, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }

            // Models List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredModels) { modelItem ->
                    DriveModelCard(
                        modelItem = modelItem,
                        isCurrentlyDownloading = downloadingItemId == modelItem.id,
                        onImportToLabor = {
                            downloadingItemId = modelItem.id
                            coroutineScope.launch {
                                driveService.downloadModel(modelItem).collect { status ->
                                    when (status) {
                                        is GoogleDriveSyncService.DownloadStatus.Progress -> {
                                            downloadProgressPercent = status.percent
                                            downloadProgressText = status.detail
                                        }
                                        is GoogleDriveSyncService.DownloadStatus.Success -> {
                                            downloadingItemId = null
                                            Toast.makeText(context, "${modelItem.name} geladen & bereit!", Toast.LENGTH_SHORT).show()
                                            onModelSelectedForLabor(modelItem, status.mesh)
                                            onDismiss()
                                        }
                                        is GoogleDriveSyncService.DownloadStatus.Error -> {
                                            downloadingItemId = null
                                            Toast.makeText(context, "Fehler: ${status.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DriveModelCard(
    modelItem: DriveModelItem,
    isCurrentlyDownloading: Boolean,
    onImportToLabor: () -> Unit
) {
    Surface(
        color = CyberBackground,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Platform & Category Badge
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = when (modelItem.sourceWebsite) {
                            "MakerWorld" -> TechLime.copy(alpha = 0.15f)
                            "Printables" -> AmberWarning.copy(alpha = 0.15f)
                            else -> ElectricBlue.copy(alpha = 0.15f)
                        },
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            0.8.dp,
                            when (modelItem.sourceWebsite) {
                                "MakerWorld" -> TechLime
                                "Printables" -> AmberWarning
                                else -> ElectricBlue
                            }
                        )
                    ) {
                        Text(
                            text = modelItem.sourceWebsite,
                            style = MaterialTheme.typography.labelSmall,
                            color = when (modelItem.sourceWebsite) {
                                "MakerWorld" -> TechLime
                                "Printables" -> AmberWarning
                                else -> ElectricBlue
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        color = CyberMagenta.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(0.8.dp, CyberMagenta)
                    ) {
                        Text(
                            text = modelItem.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberMagenta,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp
                        )
                    }
                }

                Text(
                    text = "${modelItem.extension.uppercase()} • ${modelItem.formattedSize}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            // Model Title & Description
            Text(
                text = modelItem.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = modelItem.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )

            // Bottom Actions & Download Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Von ${modelItem.author} • ${modelItem.syncTimestampText}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextCyan,
                    fontSize = 10.sp
                )

                CyberPrimaryButton(
                    text = if (isCurrentlyDownloading) "Wird geladen..." else "In Labor importieren",
                    onClick = onImportToLabor,
                    enabled = !isCurrentlyDownloading,
                    icon = if (isCurrentlyDownloading) null else Icons.Default.PrecisionManufacturing,
                    modifier = Modifier.height(38.dp),
                    testTag = "import_drive_model_${modelItem.id}"
                )
            }
        }
    }
}
