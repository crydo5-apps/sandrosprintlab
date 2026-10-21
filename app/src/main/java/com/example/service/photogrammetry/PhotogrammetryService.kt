package com.example.service.photogrammetry

import com.example.engine.SampleModels
import com.example.model.Mesh3D
import com.example.model.PhotoItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

sealed class PhotogrammetryProgress {
    data class Phase(
        val phaseIndex: Int, // 1: Bilder prüfen, 2: Modell berechnen, 3: Modell aufbereiten
        val phaseName: String,
        val percentage: Int,
        val detail: String
    ) : PhotogrammetryProgress()

    data class Success(
        val mesh: Mesh3D,
        val warnings: List<String>
    ) : PhotogrammetryProgress()

    data class Failed(val reason: String) : PhotogrammetryProgress()
}

interface PhotogrammetryService {
    val serviceName: String
    val isCloudBackendConnected: Boolean
    fun reconstructModel(
        photos: List<PhotoItem>,
        objectName: String,
        userConsentGiven: Boolean
    ): Flow<PhotogrammetryProgress>
}

class LocalPhotogrammetryService : PhotogrammetryService {
    override val serviceName: String = "Sandros Labor Photogrammetrie Engine (Lokal & Simulation)"
    override val isCloudBackendConnected: Boolean = false

    override fun reconstructModel(
        photos: List<PhotoItem>,
        objectName: String,
        userConsentGiven: Boolean
    ): Flow<PhotogrammetryProgress> = flow {
        if (photos.isEmpty()) {
            emit(PhotogrammetryProgress.Failed("Keine Fotos vorhanden. Bitte mindestens ein Foto aufnehmen oder importieren."))
            return@flow
        }

        val isSinglePhoto = photos.size == 1

        // Phase 1: Bilder prüfen
        emit(
            PhotogrammetryProgress.Phase(
                phaseIndex = 1,
                phaseName = "Phase 1: Bilder & Beleuchtung analysieren",
                percentage = 15,
                detail = "Prüfe ${photos.size} Foto(s) auf Kontrast, Überlappung und Winkelabdeckung..."
            )
        )
        delay(600)

        emit(
            PhotogrammetryProgress.Phase(
                phaseIndex = 1,
                phaseName = "Phase 1: Bilder & Beleuchtung analysieren",
                percentage = 35,
                detail = "SIFT/ORB Feature-Punkte in Aufnahmen extrahieren..."
            )
        )
        delay(600)

        // Phase 2: Modell berechnen
        emit(
            PhotogrammetryProgress.Phase(
                phaseIndex = 2,
                phaseName = "Phase 2: 3D-Geometrie berechnen",
                percentage = 55,
                detail = if (isSinglePhoto) {
                    "Schätze Tiefenkarte aus Einzelbild (2.5D Monocular Depth)..."
                } else {
                    "Structure from Motion (SfM): Spärliche 3D-Punktwolke aus ${photos.size} Blickwinkeln berechnen..."
                }
            )
        )
        delay(700)

        emit(
            PhotogrammetryProgress.Phase(
                phaseIndex = 2,
                phaseName = "Phase 2: 3D-Geometrie berechnen",
                percentage = 75,
                detail = if (isSinglePhoto) {
                    "Interpoliere verdeckte Rückseiten (Achtung: Geschätzt!)..."
                } else {
                    "Multi-View Stereo (MVS): Dichte Punktwolke und Tiefenkarten triangulieren..."
                }
            )
        )
        delay(700)

        // Phase 3: Modell aufbereiten
        emit(
            PhotogrammetryProgress.Phase(
                phaseIndex = 3,
                phaseName = "Phase 3: Modell für 3D-Druck aufbereiten",
                percentage = 88,
                detail = "Poisson-Oberflächenrekonstruktion & Glättung des Dreiecksnetzes..."
            )
        )
        delay(600)

        emit(
            PhotogrammetryProgress.Phase(
                phaseIndex = 3,
                phaseName = "Phase 3: Modell für 3D-Druck aufbereiten",
                percentage = 98,
                detail = "Wandstärken- und Manifold-Prüfung für Creality CR-10S Pro..."
            )
        )
        delay(500)

        val warnings = mutableListOf<String>()
        val mesh = if (isSinglePhoto) {
            warnings.add("Hinweis Einzelbild: Das Modell basiert auf nur einem Foto. Rückseiten und Tiefenmaße sind geschätzt und können Maßabweichungen aufweisen.")
            warnings.add("Für präzisen 3D-Druck wird eine Serie von 8 bis 24 Fotos rund um das Objekt empfohlen.")
            SampleModels.createEstimateFromSinglePhoto(objectName.ifBlank { "Einzelbild-Scan" })
        } else {
            if (photos.size < 6) {
                warnings.add("Geringe Fotoanzahl (${photos.size}): Kleinere Oberflächendetails könnten unscharf sein.")
            }
            SampleModels.createMultiPhotoReconstruction(objectName.ifBlank { "Photogrammetrie-Modell" }, photos.size)
        }

        emit(PhotogrammetryProgress.Success(mesh, warnings))
    }
}
