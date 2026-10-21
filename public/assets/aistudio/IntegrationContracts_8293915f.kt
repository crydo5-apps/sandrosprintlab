package de.sandrosprintlabor

import android.net.Uri

/**
 * Schnittstelle für einen echten Foto-zu-3D-Rekonstruktionsdienst.
 * Eine Implementierung muss Fehler und Upload-Zustimmung explizit behandeln.
 */
interface ReconstructionRepository {
    suspend fun createModel(photoUris: List<Uri>, consentToUpload: Boolean): Result<Uri>
}

/**
 * Schnittstelle für einen echten Slicer wie CuraEngine oder einen kompatiblen Backend-Dienst.
 * Erst eine erfolgreiche Implementierung darf erzeugten G-Code als druckbereit markieren.
 */
interface SlicerRepository {
    suspend fun slice(
        modelUri: Uri,
        printerProfileId: String,
        materialProfileId: String,
        settings: SliceSettings
    ): Result<SlicedGCode>
}

data class SliceSettings(
    val layerHeightMillimeters: Float,
    val infillPercent: Int,
    val supportsEnabled: Boolean
)

data class SlicedGCode(
    val contentUri: Uri,
    val printerProfileId: String,
    val materialProfileId: String,
    val estimatedPrintMinutes: Int?,
    val estimatedMaterialGrams: Float?
)
