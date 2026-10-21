package com.example.model

data class GcodeResult(
    val id: String,
    val projectName: String,
    val printerTarget: String = "Creality CR-10S Pro",
    val filamentUsedType: String = "PLA",
    val layerHeightMm: Float = 0.20f,
    val totalLayers: Int = 145,
    val estimatedPrintTimeMinutes: Int = 118,
    val estimatedFilamentLengthMeters: Float = 7.42f,
    val estimatedFilamentWeightGrams: Float = 22.8f,
    val estimatedCostEur: Float = 0.57f,
    val fileSizeBytes: Long = 1845200L,
    val gcodeHeaderSnippet: String = "",
    val fullGcodeContent: String = "",
    val slicerEngineUsed: String = "Sandro Local Path Slicer v2.4 (CuraEngine CR-10S Profile)",
    val generatedTimestamp: Long = System.currentTimeMillis()
) {
    val formattedTime: String
        get() {
            val hours = estimatedPrintTimeMinutes / 60
            val minutes = estimatedPrintTimeMinutes % 60
            return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
        }

    val formattedSize: String
        get() = "%.2f MB".format(fileSizeBytes / (1024f * 1024f))
}
