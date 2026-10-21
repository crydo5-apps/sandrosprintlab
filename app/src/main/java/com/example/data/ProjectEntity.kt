package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.AdhesionType
import com.example.model.FilamentType
import com.example.model.GcodeResult
import com.example.model.InfillPattern
import com.example.model.Mesh3D
import com.example.model.Project
import com.example.model.ProjectStatus
import com.example.model.SlicingConfig
import com.example.model.SupportType
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val notes: String,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long,
    val photoCount: Int,
    val photoUrisJson: String,
    // Mesh JSON or serialized data
    val meshJson: String?,
    // Slicing config fields
    val printerName: String,
    val filamentType: String,
    val layerHeightMm: Float,
    val infillPercent: Int,
    val infillPattern: String,
    val nozzleTempC: Int,
    val bedTempC: Int,
    val speedMmS: Int,
    val supportType: String,
    val adhesionType: String,
    val isConfirmed: Boolean,
    // Gcode result fields
    val gcodeId: String?,
    val totalLayers: Int?,
    val printTimeMinutes: Int?,
    val filamentMeters: Float?,
    val filamentGrams: Float?,
    val gcodeHeaderSnippet: String?,
    val gcodeContent: String?,
    val exportedSdPath: String?,
    val isDemo: Boolean
) {
    fun toDomain(): Project {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val listType = Types.newParameterizedType(List::class.java, String::class.java)
        val listAdapter = moshi.adapter<List<String>>(listType)
        val photos = try {
            listAdapter.fromJson(photoUrisJson) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        val meshAdapter = moshi.adapter(Mesh3D::class.java)
        val meshObj = meshJson?.let {
            try { meshAdapter.fromJson(it) } catch (e: Exception) { null }
        }

        val config = SlicingConfig(
            printerName = printerName,
            filamentType = try { FilamentType.valueOf(filamentType) } catch (e: Exception) { FilamentType.PLA },
            layerHeightMm = layerHeightMm,
            infillDensityPercent = infillPercent,
            infillPattern = try { InfillPattern.valueOf(infillPattern) } catch (e: Exception) { InfillPattern.GYROID },
            nozzleTempC = nozzleTempC,
            bedTempC = bedTempC,
            printSpeedMmS = speedMmS,
            supportType = try { SupportType.valueOf(supportType) } catch (e: Exception) { SupportType.NONE },
            adhesionType = try { AdhesionType.valueOf(adhesionType) } catch (e: Exception) { AdhesionType.SKIRT },
            isUserConfirmed = isConfirmed
        )

        val gcode = if (gcodeId != null && totalLayers != null) {
            GcodeResult(
                id = gcodeId,
                projectName = title,
                printerTarget = printerName,
                filamentUsedType = filamentType,
                layerHeightMm = layerHeightMm,
                totalLayers = totalLayers,
                estimatedPrintTimeMinutes = printTimeMinutes ?: 60,
                estimatedFilamentLengthMeters = filamentMeters ?: 5f,
                estimatedFilamentWeightGrams = filamentGrams ?: 15f,
                estimatedCostEur = (filamentGrams ?: 15f) * 0.024f,
                fileSizeBytes = (gcodeContent?.toByteArray()?.size ?: 102400).toLong(),
                gcodeHeaderSnippet = gcodeHeaderSnippet ?: "",
                fullGcodeContent = gcodeContent ?: ""
            )
        } else null

        return Project(
            id = id,
            title = title,
            notes = notes,
            status = try { ProjectStatus.valueOf(status) } catch (e: Exception) { ProjectStatus.DRAFT },
            createdAt = createdAt,
            updatedAt = updatedAt,
            photoCount = photoCount,
            photoUris = photos,
            mesh = meshObj,
            slicingConfig = config,
            gcodeResult = gcode,
            exportedSdPath = exportedSdPath,
            isDemoProject = isDemo
        )
    }

    companion object {
        fun fromDomain(p: Project): ProjectEntity {
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val listType = Types.newParameterizedType(List::class.java, String::class.java)
            val listAdapter = moshi.adapter<List<String>>(listType)
            val photosJson = listAdapter.toJson(p.photoUris)

            val meshAdapter = moshi.adapter(Mesh3D::class.java)
            val meshJson = p.mesh?.let { meshAdapter.toJson(it) }

            return ProjectEntity(
                id = p.id,
                title = p.title,
                notes = p.notes,
                status = p.status.name,
                createdAt = p.createdAt,
                updatedAt = p.updatedAt,
                photoCount = p.photoCount,
                photoUrisJson = photosJson,
                meshJson = meshJson,
                printerName = p.slicingConfig.printerName,
                filamentType = p.slicingConfig.filamentType.name,
                layerHeightMm = p.slicingConfig.layerHeightMm,
                infillPercent = p.slicingConfig.infillDensityPercent,
                infillPattern = p.slicingConfig.infillPattern.name,
                nozzleTempC = p.slicingConfig.nozzleTempC,
                bedTempC = p.slicingConfig.bedTempC,
                speedMmS = p.slicingConfig.printSpeedMmS,
                supportType = p.slicingConfig.supportType.name,
                adhesionType = p.slicingConfig.adhesionType.name,
                isConfirmed = p.slicingConfig.isUserConfirmed,
                gcodeId = p.gcodeResult?.id,
                totalLayers = p.gcodeResult?.totalLayers,
                printTimeMinutes = p.gcodeResult?.estimatedPrintTimeMinutes,
                filamentMeters = p.gcodeResult?.estimatedFilamentLengthMeters,
                filamentGrams = p.gcodeResult?.estimatedFilamentWeightGrams,
                gcodeHeaderSnippet = p.gcodeResult?.gcodeHeaderSnippet,
                gcodeContent = p.gcodeResult?.fullGcodeContent,
                exportedSdPath = p.exportedSdPath,
                isDemo = p.isDemoProject
            )
        }
    }
}
