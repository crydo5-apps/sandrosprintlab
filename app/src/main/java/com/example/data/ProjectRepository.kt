package com.example.data

import android.content.Context
import com.example.engine.SampleModels
import com.example.model.FilamentType
import com.example.model.GcodeResult
import com.example.model.InfillPattern
import com.example.model.Project
import com.example.model.ProjectStatus
import com.example.model.SlicingConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ProjectRepository(context: Context) {
    private val db = PrintLaborDatabase.getDatabase(context)
    private val dao = db.projectDao()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val existing = dao.getAllProjects().first()
                if (existing.isEmpty()) {
                    seedDemoProjects()
                }
            } catch (e: Exception) {
                // Ignore initialization errors
            }
        }
    }

    val allProjects: Flow<List<Project>> = dao.getAllProjects().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun getProject(id: String): Project? {
        return dao.getProjectById(id)?.toDomain()
    }

    suspend fun saveProject(project: Project) {
        dao.insertProject(ProjectEntity.fromDomain(project))
    }

    suspend fun deleteProject(id: String) {
        dao.deleteProjectById(id)
    }

    private suspend fun seedDemoProjects() {
        // Demo Project 1: CR-10S Pro Calibration Cube (Ready to slice)
        val cubeMesh = SampleModels.createCalibrationCube()
        val p1 = Project(
            id = "demo_proj_1",
            title = "CR-10S Pro Kalibrierwürfel 20mm",
            notes = "Testdruck zur Überprüfung von Maßhaltigkeit, Extrusion und Z-Achsen-Kalibrierung.",
            status = ProjectStatus.MODEL_READY,
            photoCount = 8,
            mesh = cubeMesh,
            slicingConfig = SlicingConfig(
                printerName = "Creality CR-10S Pro",
                filamentType = FilamentType.PLA,
                layerHeightMm = 0.20f,
                infillDensityPercent = 20,
                infillPattern = InfillPattern.GRID,
                nozzleTempC = 205,
                bedTempC = 60,
                isUserConfirmed = true
            ),
            isDemoProject = true
        )

        // Demo Project 2: Cyber Smartphone Stand (Already sliced with real G-code, ready to export)
        val standMesh = SampleModels.createCyberPhoneStand()
        val standGcode = GcodeResult(
            id = "GCODE_DEMO_99",
            projectName = "Cyber Smartphone-Halterung",
            printerTarget = "Creality CR-10S Pro",
            filamentUsedType = "PLA",
            layerHeightMm = 0.20f,
            totalLayers = 250,
            estimatedPrintTimeMinutes = 135,
            estimatedFilamentLengthMeters = 8.6f,
            estimatedFilamentWeightGrams = 26.3f,
            estimatedCostEur = 0.65f,
            fileSizeBytes = 2450000L,
            gcodeHeaderSnippet = "; Generated for Creality CR-10S Pro\n; Object: Cyber Smartphone-Halterung\n; Infill: 20% Gyroid\n; Bed: 60C, Nozzle: 205C",
            fullGcodeContent = "; Creality CR-10S Pro G-Code\nG28\nG29\nM420 S1\nG1 Z0.28 F3000\n; [Demo Gcode Content]\n"
        )
        val p2 = Project(
            id = "demo_proj_2",
            title = "Cyber Smartphone-Halterung",
            notes = "Ergonomischer Ständer mit 65° Neigungswinkel und Kabelführung.",
            status = ProjectStatus.SLICED,
            photoCount = 18,
            mesh = standMesh,
            slicingConfig = SlicingConfig(
                printerName = "Creality CR-10S Pro",
                filamentType = FilamentType.PLA,
                layerHeightMm = 0.20f,
                infillDensityPercent = 20,
                infillPattern = InfillPattern.GYROID,
                nozzleTempC = 205,
                bedTempC = 60,
                isUserConfirmed = true
            ),
            gcodeResult = standGcode,
            isDemoProject = true
        )

        // Demo Project 3: CR-10S Pro Düsen-Organizer
        val nozzleMesh = SampleModels.createNozzleHolder()
        val p3 = Project(
            id = "demo_proj_3",
            title = "CR-10S Pro Düsen-Organizer",
            notes = "Halter für 0.4mm, 0.6mm und 0.8mm MK8 Messingdüsen.",
            status = ProjectStatus.MODEL_READY,
            photoCount = 12,
            mesh = nozzleMesh,
            slicingConfig = SlicingConfig(
                printerName = "Creality CR-10S Pro",
                filamentType = FilamentType.PETG,
                layerHeightMm = 0.16f,
                infillDensityPercent = 25,
                infillPattern = InfillPattern.HONEYCOMB,
                nozzleTempC = 235,
                bedTempC = 75,
                isUserConfirmed = false
            ),
            isDemoProject = true
        )

        dao.insertProject(ProjectEntity.fromDomain(p1))
        dao.insertProject(ProjectEntity.fromDomain(p2))
        dao.insertProject(ProjectEntity.fromDomain(p3))
    }
}
