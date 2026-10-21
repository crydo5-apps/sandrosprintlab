package com.example.model

enum class FilamentType(
    val displayName: String,
    val defaultNozzleTemp: Int,
    val defaultBedTemp: Int,
    val description: String,
    val densityGPerCm3: Float
) {
    PLA(
        displayName = "PLA (Polymilchsäure)",
        defaultNozzleTemp = 205,
        defaultBedTemp = 60,
        description = "Ideal für Einsteiger, formstabil, minimales Warping.",
        densityGPerCm3 = 1.24f
    ),
    PETG(
        displayName = "PETG (Polyethylenterephthalat)",
        defaultNozzleTemp = 235,
        defaultBedTemp = 75,
        description = "Witterungsbeständig, schlagzäh, flexibler als PLA.",
        densityGPerCm3 = 1.27f
    ),
    TPU(
        displayName = "TPU (Flexibel / 95A)",
        defaultNozzleTemp = 220,
        defaultBedTemp = 50,
        description = "Elastisches Filament. Benötigt langsame Druckgeschwindigkeiten auf CR-10S Pro.",
        densityGPerCm3 = 1.21f
    ),
    ABS(
        displayName = "ABS (Acrylnitril-Butadien-Styrol)",
        defaultNozzleTemp = 245,
        defaultBedTemp = 100,
        description = "Hohe Hitzebeständigkeit. Erfordert idealerweise Einhausung gegen Zugluft.",
        densityGPerCm3 = 1.04f
    )
}

enum class InfillPattern(val displayName: String) {
    GYROID("Gyroid (Gleichmäßige Festigkeit)"),
    GRID("Gitter (Klassisch & schnell)"),
    HONEYCOMB("Wabenstruktur (Sehr stabil)"),
    TRIANGLES("Dreiecke (Hohe Seitensteifigkeit)")
}

enum class SupportType(val displayName: String) {
    NONE("Keine Stützen"),
    BUILDPLATE_ONLY("Nur auf Druckbett berührend"),
    EVERYWHERE("Überall generieren")
}

enum class AdhesionType(val displayName: String) {
    SKIRT("Skirt (Düsenreinigungslinie)"),
    BRIM("Brim (Randverbreiterung gegen Warping)"),
    RAFT("Raft (Druckfloß)")
}

data class SlicingConfig(
    val printerName: String = "Creality CR-10S Pro",
    val bedWidthMm: Float = 300f,
    val bedDepthMm: Float = 300f,
    val maxPrintHeightMm: Float = 400f,
    val nozzleDiameterMm: Float = 0.4f,
    val filamentType: FilamentType = FilamentType.PLA,
    val filamentDiameterMm: Float = 1.75f,
    val layerHeightMm: Float = 0.20f,
    val initialLayerHeightMm: Float = 0.24f,
    val infillDensityPercent: Int = 20,
    val infillPattern: InfillPattern = InfillPattern.GYROID,
    val printSpeedMmS: Int = 50,
    val outerWallSpeedMmS: Int = 25,
    val travelSpeedMmS: Int = 120,
    val nozzleTempC: Int = 205,
    val bedTempC: Int = 60,
    val supportType: SupportType = SupportType.NONE,
    val supportOverhangAngleDeg: Int = 50,
    val adhesionType: AdhesionType = AdhesionType.SKIRT,
    val retractionDistanceMm: Float = 5.0f,
    val retractionSpeedMmS: Float = 45.0f,
    val fanSpeedPercent: Int = 100,
    val isUserConfirmed: Boolean = false // Must be confirmed before slicing
)
