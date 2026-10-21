package com.example.model

data class DriveModelItem(
    val id: String,
    val name: String,
    val sourceWebsite: String, // "MakerWorld", "Printables", "Thingiverse"
    val category: String, // "Top 10 Beliebt", "Neueste Uploads", "Funktional / Maker"
    val fileSizeBytes: Long,
    val extension: String = "stl",
    val syncTimestampText: String = "Heute, 06:00 Uhr",
    val description: String = "",
    val downloadUrl: String? = null,
    val isDownloaded: Boolean = false,
    val author: String = "Community Maker"
) {
    val formattedSize: String
        get() = "%.1f MB".format(fileSizeBytes / (1024f * 1024f))
}
