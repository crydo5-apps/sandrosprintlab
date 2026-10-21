package com.example.model

data class PhotoItem(
    val id: String,
    val uriString: String,
    val angleDegrees: Int, // e.g. 0, 45, 90, 135, 180, etc.
    val isFrontAngle: Boolean = false,
    val hasSufficientOverlap: Boolean = true,
    val hasEvenLighting: Boolean = true,
    val qualityScore: Int = 88 // 0 - 100
)
