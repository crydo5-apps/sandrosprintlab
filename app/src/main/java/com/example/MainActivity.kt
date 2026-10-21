package com.example

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.ProjectRepository
import com.example.engine.StlGenerator
import com.example.model.GcodeResult
import com.example.model.Mesh3D
import com.example.model.Project
import com.example.model.ProjectStatus
import com.example.model.SlicingConfig
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GcodeInspectScreen
import com.example.ui.screens.LibraryAndSettingsScreen
import com.example.ui.screens.ModelEditorScreen
import com.example.ui.screens.NewProjectScreen
import com.example.ui.screens.SdExportScreen
import com.example.ui.screens.SlicePreparationScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import java.util.UUID

sealed class Screen {
    object Dashboard : Screen()
    object NewProject : Screen()
    data class ModelEditor(val project: Project) : Screen()
    data class SlicePrep(val project: Project) : Screen()
    data class GcodeInspect(val project: Project, val config: SlicingConfig) : Screen()
    data class SdExport(val project: Project) : Screen()
    object LibraryAndSettings : Screen()
}

class MainActivity : ComponentActivity() {

    private lateinit var repository: ProjectRepository
    private var onStlImportedCallback: ((Mesh3D) -> Unit)? = null

    // STL File Picker Launcher
    private val importStlLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                contentResolver.openInputStream(uri)?.use { stream ->
                    val filename = uri.lastPathSegment ?: "Importiertes_Modell.stl"
                    val parsedMesh = StlGenerator.parseStl(stream, filename)
                    onStlImportedCallback?.invoke(parsedMesh)
                    Toast.makeText(this, "Modell geladen: ${parsedMesh.triangleCount} Dreiecke", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Fehler beim Laden: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        repository = ProjectRepository(this)
        val driveService = com.example.service.drive.GoogleDriveSyncService(this)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PrintLaborApp(
                        repository = repository,
                        driveService = driveService,
                        onTriggerImportStl = { callback ->
                            onStlImportedCallback = callback
                            importStlLauncher.launch("*/*")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PrintLaborApp(
    repository: ProjectRepository,
    driveService: com.example.service.drive.GoogleDriveSyncService,
    onTriggerImportStl: ((Mesh3D) -> Unit) -> Unit
) {
    // 2-Phase Splash Screen: Brief Logo, then 3 seconds Sandro Grok image
    var isSplashVisible by remember { mutableStateOf(true) }

    if (isSplashVisible) {
        SplashScreen(
            onSplashFinished = { isSplashVisible = false }
        )
        return
    }

    val projects by repository.allProjects.collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }

    BackHandler(enabled = currentScreen !is Screen.Dashboard) {
        currentScreen = when (val s = currentScreen) {
            is Screen.NewProject -> Screen.Dashboard
            is Screen.ModelEditor -> Screen.Dashboard
            is Screen.SlicePrep -> Screen.ModelEditor(s.project)
            is Screen.GcodeInspect -> Screen.SlicePrep(s.project)
            is Screen.SdExport -> Screen.Dashboard
            is Screen.LibraryAndSettings -> Screen.Dashboard
            is Screen.Dashboard -> Screen.Dashboard
        }
    }

    when (val screen = currentScreen) {
        is Screen.Dashboard -> {
            DashboardScreen(
                projects = projects,
                driveService = driveService,
                onNewProject = { currentScreen = Screen.NewProject },
                onImportStl = {
                    onTriggerImportStl { mesh ->
                        val importedProject = Project(
                            id = "proj_" + UUID.randomUUID().toString().take(8),
                            title = mesh.name,
                            status = ProjectStatus.MODEL_READY,
                            mesh = mesh
                        )
                        coroutineScope.launch { repository.saveProject(importedProject) }
                        currentScreen = Screen.ModelEditor(importedProject)
                    }
                },
                onImportDriveModel = { driveItem, mesh ->
                    val driveProject = Project(
                        id = "proj_drive_" + UUID.randomUUID().toString().take(6),
                        title = driveItem.name.removeSuffix(".stl").removeSuffix(".3mf"),
                        notes = "Importiert aus Google Drive (${driveItem.sourceWebsite} / ${driveItem.category})",
                        status = ProjectStatus.MODEL_READY,
                        mesh = mesh
                    )
                    coroutineScope.launch { repository.saveProject(driveProject) }
                    currentScreen = Screen.ModelEditor(driveProject)
                },
                onSelectProject = { project ->
                    currentScreen = when (project.status) {
                        ProjectStatus.DRAFT, ProjectStatus.MODEL_READY -> Screen.ModelEditor(project)
                        ProjectStatus.SLICED -> Screen.GcodeInspect(project, project.slicingConfig)
                        ProjectStatus.EXPORTED_SD -> Screen.SdExport(project)
                    }
                },
                onOpenSettings = { currentScreen = Screen.LibraryAndSettings }
            )
        }

        is Screen.NewProject -> {
            NewProjectScreen(
                onNavigateBack = { currentScreen = Screen.Dashboard },
                onModelCreated = { newProject ->
                    coroutineScope.launch { repository.saveProject(newProject) }
                    currentScreen = Screen.ModelEditor(newProject)
                },
                onImportStl = {
                    onTriggerImportStl { mesh ->
                        val importedProject = Project(
                            id = "proj_" + UUID.randomUUID().toString().take(8),
                            title = mesh.name,
                            status = ProjectStatus.MODEL_READY,
                            mesh = mesh
                        )
                        coroutineScope.launch { repository.saveProject(importedProject) }
                        currentScreen = Screen.ModelEditor(importedProject)
                    }
                }
            )
        }

        is Screen.ModelEditor -> {
            ModelEditorScreen(
                project = screen.project,
                onNavigateBack = { currentScreen = Screen.Dashboard },
                onSaveMeshUpdate = { updatedMesh ->
                    val updated = screen.project.copy(mesh = updatedMesh)
                    coroutineScope.launch { repository.saveProject(updated) }
                },
                onProceedToSlice = { projectWithMesh ->
                    coroutineScope.launch { repository.saveProject(projectWithMesh) }
                    currentScreen = Screen.SlicePrep(projectWithMesh)
                },
                onImportNewStl = {
                    onTriggerImportStl { mesh ->
                        val updated = screen.project.copy(mesh = mesh, status = ProjectStatus.MODEL_READY)
                        coroutineScope.launch { repository.saveProject(updated) }
                        currentScreen = Screen.ModelEditor(updated)
                    }
                }
            )
        }

        is Screen.SlicePrep -> {
            SlicePreparationScreen(
                project = screen.project,
                onNavigateBack = { currentScreen = Screen.ModelEditor(screen.project) },
                onStartSlicing = { project, config ->
                    currentScreen = Screen.GcodeInspect(project, config)
                }
            )
        }

        is Screen.GcodeInspect -> {
            GcodeInspectScreen(
                project = screen.project,
                config = screen.config,
                onNavigateBack = { currentScreen = Screen.SlicePrep(screen.project) },
                onSlicingFinished = { updatedProject, gcodeResult ->
                    coroutineScope.launch { repository.saveProject(updatedProject) }
                },
                onProceedToSdExport = { slicedProject ->
                    coroutineScope.launch { repository.saveProject(slicedProject) }
                    currentScreen = Screen.SdExport(slicedProject)
                }
            )
        }

        is Screen.SdExport -> {
            SdExportScreen(
                project = screen.project,
                onNavigateBack = { currentScreen = Screen.GcodeInspect(screen.project, screen.project.slicingConfig) },
                onExportCompleted = { exportedProject, sdPath ->
                    coroutineScope.launch { repository.saveProject(exportedProject) }
                },
                onFinishReturnHome = { currentScreen = Screen.Dashboard }
            )
        }

        is Screen.LibraryAndSettings -> {
            LibraryAndSettingsScreen(
                projects = projects,
                onNavigateBack = { currentScreen = Screen.Dashboard },
                onSelectProject = { proj ->
                    currentScreen = Screen.ModelEditor(proj)
                },
                onDeleteProject = { id ->
                    coroutineScope.launch { repository.deleteProject(id) }
                }
            )
        }
    }
}
