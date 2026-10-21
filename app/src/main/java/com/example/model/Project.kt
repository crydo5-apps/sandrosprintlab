package com.example.model

enum class ProjectStatus(val label: String) {
    DRAFT("Entwurf / Fotos"),
    MODEL_READY("3D-Modell bereit"),
    SLICED("G-Code gesliced"),
    EXPORTED_SD("Auf SD exportiert")
}

data class Project(
    val id: String,
    val title: String,
    val notes: String = "",
    val status: ProjectStatus = ProjectStatus.DRAFT,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val photoCount: Int = 0,
    val photoUris: List<String> = emptyList(),
    val mesh: Mesh3D? = null,
    val slicingConfig: SlicingConfig = SlicingConfig(),
    val gcodeResult: GcodeResult? = null,
    val exportedSdPath: String? = null,
    val isDemoProject: Boolean = false
)
