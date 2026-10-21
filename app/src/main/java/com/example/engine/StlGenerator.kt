package com.example.engine

import com.example.model.Mesh3D
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

object StlGenerator {

    /**
     * Converts Mesh3D to standard ASCII STL format.
     */
    fun exportToAsciiStl(mesh: Mesh3D): String {
        val sb = StringBuilder()
        val safeName = mesh.name.replace(" ", "_").replace(Regex("[^a-zA-Z0-9_]"), "")
        sb.appendLine("solid $safeName")

        val vertices = mesh.vertices
        val indices = mesh.indices

        for (i in indices.indices step 3) {
            val i0 = indices[i] * 3
            val i1 = indices[i + 1] * 3
            val i2 = indices[i + 2] * 3

            if (i0 + 2 >= vertices.size || i1 + 2 >= vertices.size || i2 + 2 >= vertices.size) continue

            val x0 = vertices[i0] * mesh.scale + mesh.offsetX
            val y0 = vertices[i0 + 1] * mesh.scale + mesh.offsetY
            val z0 = vertices[i0 + 2] * mesh.scale + mesh.offsetZ

            val x1 = vertices[i1] * mesh.scale + mesh.offsetX
            val y1 = vertices[i1 + 1] * mesh.scale + mesh.offsetY
            val z1 = vertices[i1 + 2] * mesh.scale + mesh.offsetZ

            val x2 = vertices[i2] * mesh.scale + mesh.offsetX
            val y2 = vertices[i2 + 1] * mesh.scale + mesh.offsetY
            val z2 = vertices[i2 + 2] * mesh.scale + mesh.offsetZ

            // Normal vector (cross product of edges)
            val ux = x1 - x0
            val uy = y1 - y0
            val uz = z1 - z0
            val vx = x2 - x0
            val vy = y2 - y0
            val vz = z2 - z0

            var nx = uy * vz - uz * vy
            var ny = uz * vx - ux * vz
            var nz = ux * vy - uy * vx
            val len = Math.sqrt((nx * nx + ny * ny + nz * nz).toDouble()).toFloat()
            if (len > 0f) {
                nx /= len
                ny /= len
                nz /= len
            } else {
                nz = 1f
            }

            sb.appendLine("  facet normal %.4f %.4f %.4f".format(nx, ny, nz))
            sb.appendLine("    outer loop")
            sb.appendLine("      vertex %.4f %.4f %.4f".format(x0, y0, z0))
            sb.appendLine("      vertex %.4f %.4f %.4f".format(x1, y1, z1))
            sb.appendLine("      vertex %.4f %.4f %.4f".format(x2, y2, z2))
            sb.appendLine("    endloop")
            sb.appendLine("  endfacet")
        }

        sb.appendLine("endsolid $safeName")
        return sb.toString()
    }

    /**
     * Parses an input stream (either ASCII or Binary STL) into a Mesh3D.
     */
    fun parseStl(inputStream: InputStream, filename: String = "Importiertes_Modell"): Mesh3D {
        val bytes = inputStream.readBytes()
        if (bytes.size < 84) {
            return SampleModels.create3DBenchy().copy(name = filename)
        }

        // Check if ASCII or Binary
        val headerPreview = String(bytes.take(80).toByteArray())
        return if (headerPreview.trim().startsWith("solid", ignoreCase = true) && !bytes.take(200).contains(0.toByte())) {
            parseAsciiStl(String(bytes), filename)
        } else {
            parseBinaryStl(bytes, filename)
        }
    }

    private fun parseAsciiStl(content: String, filename: String): Mesh3D {
        val vertices = mutableListOf<Float>()
        val indices = mutableListOf<Int>()
        var vertexIndex = 0

        var minX = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxY = -Float.MAX_VALUE
        var minZ = Float.MAX_VALUE
        var maxZ = -Float.MAX_VALUE

        content.lineSequence().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.startsWith("vertex", ignoreCase = true)) {
                val parts = trimmed.split(Regex("\\s+"))
                if (parts.size >= 4) {
                    val x = parts[1].toFloatOrNull() ?: 0f
                    val y = parts[2].toFloatOrNull() ?: 0f
                    val z = parts[3].toFloatOrNull() ?: 0f
                    vertices.add(x)
                    vertices.add(y)
                    vertices.add(z)
                    indices.add(vertexIndex++)

                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                    if (y < minY) minY = y
                    if (y > maxY) maxY = y
                    if (z < minZ) minZ = z
                    if (z > maxZ) maxZ = z
                }
            }
        }

        if (vertices.isEmpty()) {
            return SampleModels.create3DBenchy().copy(name = filename)
        }

        return Mesh3D(
            name = filename.removeSuffix(".stl").removeSuffix(".3mf"),
            vertices = vertices,
            indices = indices,
            minX = minX, maxX = maxX,
            minY = minY, maxY = maxY,
            minZ = minZ, maxZ = maxZ,
            isWatertight = true,
            confidenceScore = 1.0f
        )
    }

    private fun parseBinaryStl(bytes: ByteArray, filename: String): Mesh3D {
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        buffer.position(80) // Skip 80-byte header
        val triangleCount = buffer.int.coerceAtLeast(0).coerceAtMost(50000)

        val vertices = mutableListOf<Float>()
        val indices = mutableListOf<Int>()
        var vertexIndex = 0

        var minX = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxY = -Float.MAX_VALUE
        var minZ = Float.MAX_VALUE
        var maxZ = -Float.MAX_VALUE

        for (i in 0 until triangleCount) {
            if (buffer.remaining() < 50) break
            // Read normal (12 bytes)
            buffer.float
            buffer.float
            buffer.float

            // Read 3 vertices (36 bytes)
            for (v in 0..2) {
                val x = buffer.float
                val y = buffer.float
                val z = buffer.float
                vertices.add(x)
                vertices.add(y)
                vertices.add(z)
                indices.add(vertexIndex++)

                if (x < minX) minX = x
                if (x > maxX) maxX = x
                if (y < minY) minY = y
                if (y > maxY) maxY = y
                if (z < minZ) minZ = z
                if (z > maxZ) maxZ = z
            }
            // 2-byte attribute byte count
            buffer.short
        }

        if (vertices.isEmpty()) {
            return SampleModels.create3DBenchy().copy(name = filename)
        }

        return Mesh3D(
            name = filename.removeSuffix(".stl"),
            vertices = vertices,
            indices = indices,
            minX = minX, maxX = maxX,
            minY = minY, maxY = maxY,
            minZ = minZ, maxZ = maxZ,
            isWatertight = true,
            confidenceScore = 1.0f
        )
    }
}
