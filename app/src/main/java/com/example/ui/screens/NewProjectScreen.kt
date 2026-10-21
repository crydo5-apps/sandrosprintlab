package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Mesh3D
import com.example.model.PhotoItem
import com.example.model.Project
import com.example.model.ProjectStatus
import com.example.service.photogrammetry.LocalPhotogrammetryService
import com.example.service.photogrammetry.PhotogrammetryProgress
import com.example.ui.components.WorkflowStepIndicator
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberPrimaryButton
import com.example.ui.components.CyberSecondaryButton
import com.example.ui.components.PhotoAssistantView
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
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewProjectScreen(
    onNavigateBack: () -> Unit,
    onModelCreated: (Project) -> Unit,
    onImportStl: () -> Unit
) {
    BackHandler { onNavigateBack() }

    var objectName by remember { mutableStateOf("Neues Druckteil") }
    var makerNotes by remember { mutableStateOf("") }
    val photoList = remember { mutableStateListOf<PhotoItem>() }

    var showPrivacyDialog by remember { mutableStateOf(false) }
    var privacyConsentGiven by remember { mutableStateOf(false) }

    var isReconstructing by remember { mutableStateOf(false) }
    var currentPhaseName by remember { mutableStateOf("") }
    var currentPhaseDetail by remember { mutableStateOf("") }
    var currentPhaseIndex by remember { mutableIntStateOf(1) }
    var reconstructionProgress by remember { mutableIntStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val photogrammetryService = remember { LocalPhotogrammetryService() }

    // System Photo Picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 30)
    ) { uris ->
        uris.forEachIndexed { index, uri ->
            val angle = ((photoList.size + index) * 45) % 360
            photoList.add(
                PhotoItem(
                    id = UUID.randomUUID().toString(),
                    uriString = uri.toString(),
                    angleDegrees = angle,
                    qualityScore = 90
                )
            )
        }
    }

    // Function to add demo photo series for testing photogrammetry workflow
    fun addSamplePhotos(count: Int) {
        val angles = listOf(0, 45, 90, 135, 180, 225, 270, 315)
        for (i in 0 until count) {
            val angle = angles[i % angles.size]
            photoList.add(
                PhotoItem(
                    id = UUID.randomUUID().toString(),
                    uriString = "android.resource://com.aistudio.sandrosprintlabor.crxpro/drawable/ic_launcher_cyber_nozzle",
                    angleDegrees = angle,
                    qualityScore = 92
                )
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Neues 3D-Projekt",
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WorkflowStepIndicator(currentStep = 1)

            // Project Metadata Inputs
            CyberCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Objektdaten", style = MaterialTheme.typography.titleMedium, color = TextPrimary)

                    OutlinedTextField(
                        value = objectName,
                        onValueChange = { objectName = it },
                        label = { Text("Objektname / Bauteil") },
                        placeholder = { Text("z.B. Getrieberad, Halterung") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("object_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CyberBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = NeonCyan
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = makerNotes,
                        onValueChange = { makerNotes = it },
                        label = { Text("Notizen / Funktion (optional)") },
                        placeholder = { Text("z.B. Für CR-10S Pro Werkzeughalter, PLA") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CyberBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        maxLines = 2
                    )
                }
            }

            // Photo Assistant with Radar & Feedback
            PhotoAssistantView(photos = photoList)

            // Photo Gallery & Capture Bar
            CyberCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Aufnahmen (${photoList.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )

                        if (photoList.isNotEmpty()) {
                            TextButton(onClick = { photoList.clear() }) {
                                Text("Alle löschen", color = CyberError, fontSize = 12.sp)
                            }
                        }
                    }

                    // Add Photo Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CyberSecondaryButton(
                            text = "Galerie wählen",
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            icon = Icons.Default.AddPhotoAlternate,
                            modifier = Modifier.weight(1f),
                            testTag = "pick_gallery_button"
                        )

                        CyberSecondaryButton(
                            text = "+8 Demo-Fotos",
                            onClick = { addSamplePhotos(8) },
                            icon = Icons.Default.AutoAwesome,
                            modifier = Modifier.weight(1f),
                            testTag = "add_sample_photos_button"
                        )
                    }

                    // Horizontal Photo Strip
                    if (photoList.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            itemsIndexed(photoList) { index, item ->
                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(CyberSurfaceVariant)
                                        .border(1.dp, CyberBorder, RoundedCornerShape(10.dp))
                                ) {
                                    AsyncImage(
                                        model = item.uriString,
                                        contentDescription = "Foto ${index + 1}",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )

                                    // Angle badge
                                    Surface(
                                        color = CyberBackground.copy(alpha = 0.8f),
                                        shape = RoundedCornerShape(bottomEnd = 6.dp),
                                        modifier = Modifier.align(Alignment.TopStart)
                                    ) {
                                        Text(
                                            text = "${item.angleDegrees}°",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextCyan,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                            fontSize = 9.sp
                                        )
                                    }

                                    // Remove button
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(CyberBackground.copy(alpha = 0.85f))
                                            .clickable { photoList.removeAt(index) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Löschen", tint = CyberError, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Privacy & Consent Card
            CyberCard(borderColor = if (privacyConsentGiven) TechLime else CyberBorder) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Checkbox(
                        checked = privacyConsentGiven,
                        onCheckedChange = { privacyConsentGiven = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = NeonCyan,
                            checkmarkColor = CyberBackground,
                            uncheckedColor = CyberBorder
                        ),
                        modifier = Modifier.testTag("privacy_checkbox")
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Datenschutz & Verarbeitungs-Zustimmung",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary
                        )
                        Text(
                            text = "Fotos werden datenschutzkonform verarbeitet. Bei Cloud-Diensten erfolgt die Übertragung verschlüsselt. Details ansehen.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.clickable { showPrivacyDialog = true }
                        )
                    }
                }
            }

            // Active Reconstruction Progress Banner
            if (isReconstructing) {
                CyberCard(glowEffect = true) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(currentPhaseName, style = MaterialTheme.typography.titleMedium, color = NeonCyan)
                            Text("$reconstructionProgress%", style = MaterialTheme.typography.titleMedium, color = TextCyan)
                        }

                        LinearProgressIndicator(
                            progress = { reconstructionProgress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = NeonCyan,
                            trackColor = CyberSurfaceVariant
                        )

                        Text(currentPhaseDetail, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                }
            }

            // Error display if failed
            if (errorMessage != null) {
                Surface(
                    color = CyberError.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberError)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CyberError,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Primary Start Action
            CyberPrimaryButton(
                text = if (photoList.size == 1) "Geschätztes 3D-Modell berechnen (1 Foto)" else "3D-Modell berechnen (${photoList.size} Fotos)",
                onClick = {
                    if (!privacyConsentGiven) {
                        showPrivacyDialog = true
                        return@CyberPrimaryButton
                    }

                    if (photoList.isEmpty()) {
                        errorMessage = "Bitte nimm mindestens ein Foto auf oder füge Demo-Bilder hinzu."
                        return@CyberPrimaryButton
                    }

                    isReconstructing = true
                    errorMessage = null

                    coroutineScope.launch {
                        photogrammetryService.reconstructModel(
                            photos = photoList,
                            objectName = objectName,
                            userConsentGiven = privacyConsentGiven
                        ).collect { state ->
                            when (state) {
                                is PhotogrammetryProgress.Phase -> {
                                    currentPhaseIndex = state.phaseIndex
                                    currentPhaseName = state.phaseName
                                    reconstructionProgress = state.percentage
                                    currentPhaseDetail = state.detail
                                }
                                is PhotogrammetryProgress.Success -> {
                                    isReconstructing = false
                                    val newProj = Project(
                                        id = "proj_" + UUID.randomUUID().toString().take(8),
                                        title = objectName.ifBlank { "3D-Scan" },
                                        notes = makerNotes,
                                        status = ProjectStatus.MODEL_READY,
                                        photoCount = photoList.size,
                                        photoUris = photoList.map { it.uriString },
                                        mesh = state.mesh
                                    )
                                    onModelCreated(newProj)
                                }
                                is PhotogrammetryProgress.Failed -> {
                                    isReconstructing = false
                                    errorMessage = state.reason
                                }
                            }
                        }
                    }
                },
                enabled = !isReconstructing,
                icon = Icons.Default.AutoAwesome,
                modifier = Modifier.fillMaxWidth(),
                testTag = "start_reconstruction_button"
            )

            // Direct Alternative: Import STL
            CyberSecondaryButton(
                text = "Stattdessen fertiges 3D-Modell (STL/3MF) importieren",
                onClick = onImportStl,
                icon = Icons.Default.FolderOpen,
                modifier = Modifier.fillMaxWidth(),
                testTag = "alternative_import_stl"
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Privacy & Data Upload Consent Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = NeonCyan)
                    Text("Datenschutz & Upload-Hinweis", color = TextPrimary)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Sandros Print Labor verarbeitet deine Bilddaten transparent und sicher:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Text(
                        "• Bei lokaler Verarbeitung verbleiben alle Bilder ausschließlich auf diesem Android-Gerät.\n" +
                        "• Falls ein externer Photogrammetrie-Server (z. B. Meshroom / COLMAP Cluster) in den Einstellungen konfiguriert ist, werden nur die ausgewählten Fotos über eine verschlüsselte HTTPS-Verbindung übertragen.\n" +
                        "• Nach der Triangulierung der Dreiecksflächen werden temporäre Punktwolken auf dem Server automatisch gelöscht.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        privacyConsentGiven = true
                        showPrivacyDialog = false
                    }
                ) {
                    Text("Zustimmen & Verstanden", color = NeonCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Abbrechen", color = TextSecondary)
                }
            },
            containerColor = CyberSurfaceCard
        )
    }
}
