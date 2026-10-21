package com.example.model

/**
 * 3D Mesh representation with triangular faces and analytical quality metrics.
 */
data class Mesh3D(
    val name: String = "Modell",
    val vertices: List<Float> = emptyList(), // Triplet of (x, y, z)
    val indices: List<Int> = emptyList(),    // Triplet of vertex indices per triangle
    val minX: Float = -25f,
    val maxX: Float = 25f,
    val minY: Float = -25f,
    val maxY: Float = 25f,
    val minZ: Float = 0f,
    val maxZ: Float = 30f,
    // Transformations
    val scale: Float = 1.0f,
    val rotationX: Float = 0f,
    val rotationY: Float = 0f,
    val rotationZ: Float = 0f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val offsetZ: Float = 0f,
    // Mesh Quality Diagnostics
    val isWatertight: Boolean = true,
    val nonManifoldEdges: Int = 0,
    val estimatedMinWallThicknessMm: Float = 1.2f,
    val overhangAngleMaxDeg: Float = 42f,
    val overhangPercentage: Float = 8.5f,
    val isEstimatedFromSinglePhoto: Boolean = false,
    val confidenceScore: Float = 0.94f
) {
    val vertexCount: Int get() = vertices.size / 3
    val triangleCount: Int get() = indices.size / 3

    val sizeX: Float get() = (maxX - minX) * scale
    val sizeY: Float get() = (maxY - minY) * scale
    val sizeZ: Float get() = (maxZ - minZ) * scale

    val volumeCm3: Float get() = ((sizeX * sizeY * sizeZ) * 0.45f) / 1000f

    /**
     * Checks if the model fits within Creality CR-10S Pro build envelope (300 x 300 x 400 mm).
     */
    fun fitsInCr10SPro(): Boolean {
        return sizeX <= 300f && sizeY <= 300f && sizeZ <= 400f
    }
}
