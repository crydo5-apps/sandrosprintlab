package com.example

import com.example.engine.SampleModels
import com.example.engine.StlGenerator
import com.example.model.FilamentType
import com.example.model.InfillPattern
import com.example.model.SlicingConfig
import com.example.service.slicer.CrealityCR10SProSlicer
import com.example.service.slicer.SlicingProgress
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

class ExampleUnitTest {

    @Test
    fun testCalibrationCubeGeometry() {
        val cube = SampleModels.createCalibrationCube()
        assertEquals(8, cube.vertexCount)
        assertEquals(12, cube.triangleCount)
        assertEquals(20f, cube.sizeX, 0.001f)
        assertEquals(20f, cube.sizeY, 0.001f)
        assertEquals(20f, cube.sizeZ, 0.001f)
        assertTrue(cube.isWatertight)
        assertTrue(cube.fitsInCr10SPro())
    }

    @Test
    fun testStlExportAndParse() {
        val cube = SampleModels.createCalibrationCube()
        val stlAscii = StlGenerator.exportToAsciiStl(cube)
        assertTrue(stlAscii.contains("solid"))
        assertTrue(stlAscii.contains("facet normal"))
        assertTrue(stlAscii.contains("endsolid"))

        val inputStream = ByteArrayInputStream(stlAscii.toByteArray())
        val parsed = StlGenerator.parseStl(inputStream, "TestCube.stl")
        assertEquals(12, parsed.triangleCount)
        assertEquals(20f, parsed.sizeX, 0.01f)
        assertEquals(20f, parsed.sizeY, 0.01f)
    }

    @Test
    fun testCrealityCR10SProSlicingProducesValidMarlinGcode() = runBlocking {
        val slicer = CrealityCR10SProSlicer()
        val cube = SampleModels.createCalibrationCube()
        val config = SlicingConfig(
            printerName = "Creality CR-10S Pro",
            filamentType = FilamentType.PLA,
            layerHeightMm = 0.20f,
            nozzleTempC = 205,
            bedTempC = 60,
            infillDensityPercent = 20,
            infillPattern = InfillPattern.GRID
        )

        val states = slicer.slice(cube, config).toList()
        assertTrue(states.isNotEmpty())

        val completed = states.filterIsInstance<SlicingProgress.Completed>().firstOrNull()
        assertNotNull(completed)

        val gcode = completed!!.result.fullGcodeContent
        // Verify key CR-10S Pro firmware commands
        assertTrue(gcode.contains("G28 ;Home all axes"))
        assertTrue(gcode.contains("G29 ;CR-10S Pro Matrix Auto Bed Leveling"))
        assertTrue(gcode.contains("M420 S1 Z2"))
        assertTrue(gcode.contains("M104 S205"))
        assertTrue(gcode.contains("M140 S60"))
        assertTrue(gcode.contains("G1 X0.1 Y200.0")) // CR-10S Pro purge line
        assertTrue(gcode.contains("G1 X0 Y300")) // Bed presentation
    }

    @Test
    fun testGoogleDriveFolderAndDailyModels() {
        val driveService = com.example.service.drive.GoogleDriveSyncService()

        assertEquals("Projekte/3D-Drucker", driveService.targetFolderPath.value)
        assertTrue(driveService.isDriveConnected.value)

        val models = driveService.driveModels.value
        assertEquals(10, models.size)

        // Verify models from all 3 websites are present
        val sources = models.map { it.sourceWebsite }.toSet()
        assertTrue(sources.contains("MakerWorld"))
        assertTrue(sources.contains("Printables"))
        assertTrue(sources.contains("Thingiverse"))

        val firstModel = models.first()
        assertTrue(firstModel.name.endsWith(".stl") || firstModel.name.endsWith(".3mf"))
    }

    @Test
    fun testFormatStringSafe() {
        val selectedLayerSlider = 0
        val config = SlicingConfig()
        val clampedLayer = selectedLayerSlider.coerceIn(0, 100)
        val zHeight = "%.2f".format(0.24f + (clampedLayer * config.layerHeightMm))
        val fanStatus = if (clampedLayer > 0) "100%" else "0% (Erste Schicht Haftung)"
        val text = "Z-Höhe: $zHeight mm | Geschwindigkeit: 50 mm/s | Lüfter: $fanStatus"
        assertTrue(text.contains("Z-Höhe:"))
        assertTrue(text.contains("100%") || text.contains("0%"))
    }

    @Test
    fun testAllTenGoogleDriveModelsHaveUniqueRealisticGeometries() = runBlocking {
        val driveService = com.example.service.drive.GoogleDriveSyncService()
        val models = driveService.driveModels.value
        assertEquals(10, models.size)

        val downloadedMeshes = mutableListOf<com.example.model.Mesh3D>()

        for (item in models) {
            val states = driveService.downloadModel(item).toList()
            val success = states.filterIsInstance<com.example.service.drive.GoogleDriveSyncService.DownloadStatus.Success>().firstOrNull()
            assertNotNull("Download should succeed for ${item.name}", success)
            val mesh = success!!.mesh
            downloadedMeshes.add(mesh)

            // Ensure no mesh is an empty or generic cube
            assertTrue("${item.name} should have triangles", mesh.triangleCount >= 12)
            assertTrue("${item.name} should fit in CR-10S Pro", mesh.fitsInCr10SPro())
            assertTrue("${item.name} should be watertight", mesh.isWatertight)
        }

        // Verify all 10 models have distinct names and distinct geometries
        val uniqueNames = downloadedMeshes.map { it.name }.toSet()
        assertEquals("Each model must have a unique distinct name", 10, uniqueNames.size)

        val uniqueTriangleCounts = downloadedMeshes.map { it.triangleCount }.toSet()
        assertTrue("Models should have diverse distinct geometry topologies", uniqueTriangleCounts.size >= 6)
    }

    @Test
    fun testIconic3DBenchyGeometry() {
        val benchy = SampleModels.create3DBenchy()
        assertTrue(benchy.triangleCount > 50)
        assertTrue(benchy.sizeZ > 30f) // Chimney / smokestack height
        assertTrue(benchy.fitsInCr10SPro())
    }
}



