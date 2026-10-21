package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Mesh3D
import com.example.ui.theme.BedPlateGrid
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberBorderGlow
import com.example.ui.theme.CyberMagenta
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TechLime
import com.example.ui.theme.TextCyan
import com.example.ui.theme.TextSecondary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun Model3DViewer(
    mesh: Mesh3D?,
    modifier: Modifier = Modifier,
    showBedPlate: Boolean = true,
    highlightOverhangs: Boolean = false,
    onCenterOnBed: () -> Unit = {}
) {
    var pitch by remember { mutableFloatStateOf(25f) } // Elevation angle
    var yaw by remember { mutableFloatStateOf(45f) }   // Azimuth angle
    var zoom by remember { mutableFloatStateOf(3.2f) }
    var panX by remember { mutableFloatStateOf(0f) }
    var panY by remember { mutableFloatStateOf(0f) }
    var renderModeWireframe by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CyberBackground)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, gestureZoom, _ ->
                    zoom = (zoom * gestureZoom).coerceIn(1.2f, 9.5f)
                    panX += pan.x
                    panY += pan.y
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    yaw = (yaw + dragAmount.x * 0.5f) % 360f
                    pitch = (pitch - dragAmount.y * 0.5f).coerceIn(-85f, 85f)
                }
            }
            .testTag("model_3d_viewer")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f + panX
            val cy = size.height / 2f + panY

            val radYaw = Math.toRadians(yaw.toDouble())
            val radPitch = Math.toRadians(pitch.toDouble())
            val cosY = cos(radYaw).toFloat()
            val sinY = sin(radYaw).toFloat()
            val cosP = cos(radPitch).toFloat()
            val sinP = sin(radPitch).toFloat()

            // 3D Projection function (World -> Screen)
            fun project(x: Float, y: Float, z: Float): Offset {
                // 1. Yaw rotation (around Z-axis)
                val x1 = x * cosY - y * sinY
                val y1 = x * sinY + y * cosY
                val z1 = z

                // 2. Pitch rotation (around X-axis)
                val y2 = y1 * cosP - z1 * sinP
                val z2 = y1 * sinP + z1 * cosP

                // 3. Isometric/Perspective scale
                val fov = 600f
                val distance = fov + y2 * 1.5f
                val perspective = if (distance > 50f) fov / distance else 1.0f

                val sx = cx + x1 * zoom * perspective
                val sy = cy - z2 * zoom * perspective
                return Offset(sx, sy)
            }

            // Draw CR-10S Pro Bed Plate (300 x 300 mm grid)
            if (showBedPlate) {
                val bedHalf = 50f // Scaled representation of 300mm bed
                val gridSteps = 6
                val step = (bedHalf * 2) / gridSteps

                // Grid lines
                for (i in 0..gridSteps) {
                    val pos = -bedHalf + i * step
                    // Parallel to X
                    val p1 = project(-bedHalf, pos, 0f)
                    val p2 = project(bedHalf, pos, 0f)
                    drawLine(
                        color = BedPlateGrid,
                        start = p1,
                        end = p2,
                        strokeWidth = 1.2f
                    )
                    // Parallel to Y
                    val q1 = project(pos, -bedHalf, 0f)
                    val q2 = project(pos, bedHalf, 0f)
                    drawLine(
                        color = BedPlateGrid,
                        start = q1,
                        end = q2,
                        strokeWidth = 1.2f
                    )
                }

                // Outer border of Creality CR-10S Pro bed
                val b1 = project(-bedHalf, -bedHalf, 0f)
                val b2 = project(bedHalf, -bedHalf, 0f)
                val b3 = project(bedHalf, bedHalf, 0f)
                val b4 = project(-bedHalf, bedHalf, 0f)
                val bedPath = Path().apply {
                    moveTo(b1.x, b1.y)
                    lineTo(b2.x, b2.y)
                    lineTo(b3.x, b3.y)
                    lineTo(b4.x, b4.y)
                    close()
                }
                drawPath(bedPath, color = CyberBorderGlow, style = Stroke(width = 2f))

                // Bed origin coordinate markers (X=Red/Cyan, Y=Green/Blue)
                val origin = project(-bedHalf, -bedHalf, 0f)
                val xArrow = project(-bedHalf + 18f, -bedHalf, 0f)
                val yArrow = project(-bedHalf, -bedHalf + 18f, 0f)
                val zArrow = project(-bedHalf, -bedHalf, 18f)

                drawLine(NeonCyan, origin, xArrow, strokeWidth = 3f)
                drawLine(TechLime, origin, yArrow, strokeWidth = 3f)
                drawLine(CyberMagenta, origin, zArrow, strokeWidth = 3f)
            }

            // Render Mesh Triangles
            if (mesh != null && mesh.vertices.isNotEmpty() && mesh.indices.isNotEmpty()) {
                val verts = mesh.vertices
                val inds = mesh.indices
                val scale = mesh.scale
                val ox = mesh.offsetX
                val oy = mesh.offsetY
                val oz = mesh.offsetZ

                // Light source direction
                val lx = 0.5f
                val ly = -0.5f
                val lz = 0.8f
                val lightNorm = kotlin.math.sqrt(lx * lx + ly * ly + lz * lz)
                val nlx = lx / lightNorm
                val nly = ly / lightNorm
                val nlz = lz / lightNorm

                val triangleCount = inds.size / 3
                for (t in 0 until triangleCount) {
                    val i0 = inds[t * 3] * 3
                    val i1 = inds[t * 3 + 1] * 3
                    val i2 = inds[t * 3 + 2] * 3

                    if (i0 + 2 >= verts.size || i1 + 2 >= verts.size || i2 + 2 >= verts.size) continue

                    val vx0 = verts[i0] * scale + ox
                    val vy0 = verts[i0 + 1] * scale + oy
                    val vz0 = verts[i0 + 2] * scale + oz

                    val vx1 = verts[i1] * scale + ox
                    val vy1 = verts[i1 + 1] * scale + oy
                    val vz1 = verts[i1 + 2] * scale + oz

                    val vx2 = verts[i2] * scale + ox
                    val vy2 = verts[i2 + 1] * scale + oy
                    val vz2 = verts[i2 + 2] * scale + oz

                    val p0 = project(vx0, vy0, vz0)
                    val p1 = project(vx1, vy1, vz1)
                    val p2 = project(vx2, vy2, vz2)

                    // Calculate face normal
                    val ux = vx1 - vx0
                    val uy = vy1 - vy0
                    val uz = vz1 - vz0
                    val wx = vx2 - vx0
                    val wy = vy2 - vy0
                    val wz = vz2 - vz0
                    var nx = uy * wz - uz * wy
                    var ny = uz * wx - ux * wz
                    var nz = ux * wy - uy * wx
                    val len = kotlin.math.sqrt(nx * nx + ny * ny + nz * nz)
                    if (len > 0f) {
                        nx /= len
                        ny /= len
                        nz /= len
                    } else {
                        nz = 1f
                    }

                    // Overhang check: Normal pointing downward (Z < 0) steeper than ~45°
                    val isOverhang = highlightOverhangs && nz < -0.3f

                    val path = Path().apply {
                        moveTo(p0.x, p0.y)
                        lineTo(p1.x, p1.y)
                        lineTo(p2.x, p2.y)
                        close()
                    }

                    if (!renderModeWireframe) {
                        // Lambertian shading
                        val diffuse = (nx * nlx + ny * nly + nz * nlz).coerceIn(0.15f, 1.0f)
                        val faceColor = when {
                            isOverhang -> CyberMagenta.copy(alpha = 0.85f)
                            mesh.isEstimatedFromSinglePhoto -> Color(
                                red = (0.2f * diffuse).coerceIn(0f, 1f),
                                green = (0.55f * diffuse).coerceIn(0f, 1f),
                                blue = (0.75f * diffuse).coerceIn(0f, 1f),
                                alpha = 0.9f
                            )
                            else -> Color(
                                red = (0.05f * diffuse).coerceIn(0f, 1f),
                                green = (0.65f * diffuse).coerceIn(0f, 1f),
                                blue = (0.85f * diffuse).coerceIn(0f, 1f),
                                alpha = 0.92f
                            )
                        }

                        drawPath(path, color = faceColor)
                        drawPath(
                            path,
                            color = if (isOverhang) CyberMagenta else NeonCyan.copy(alpha = 0.35f),
                            style = Stroke(width = 1f)
                        )
                    } else {
                        // Cyber Wireframe mode
                        drawPath(
                            path,
                            color = if (isOverhang) CyberMagenta else NeonCyan,
                            style = Stroke(width = 1.4f)
                        )
                    }
                }
            }
        }

        // Top Overlay: Status and stats
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = CyberBackground.copy(alpha = 0.85f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(if (mesh != null) TechLime else CyberMagenta, RoundedCornerShape(4.dp))
                    )
                    Text(
                        text = if (mesh != null) "${mesh.triangleCount} Dreiecke | %.1f x %.1f x %.1f mm".format(mesh.sizeX, mesh.sizeY, mesh.sizeZ) else "Kein Modell",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextCyan
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AssistChip(
                    onClick = { renderModeWireframe = !renderModeWireframe },
                    label = { Text(if (renderModeWireframe) "Wireframe" else "Shaded", fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.GridOn,
                            contentDescription = "Drahtgittermodus",
                            tint = if (renderModeWireframe) NeonCyan else TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = CyberBackground.copy(alpha = 0.85f),
                        labelColor = TextCyan
                    ),
                    border = AssistChipDefaults.assistChipBorder(true, borderColor = CyberBorder)
                )
            }
        }

        // Bottom Controls: Quick Angle Resets & Center
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilledTonalIconButton(
                    onClick = {
                        pitch = 25f
                        yaw = 45f
                        panX = 0f
                        panY = 0f
                        zoom = 3.2f
                    },
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = CyberBackground.copy(alpha = 0.85f),
                        contentColor = NeonCyan
                    )
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Ansicht zurücksetzen", modifier = Modifier.size(18.dp))
                }

                FilledTonalIconButton(
                    onClick = {
                        pitch = 85f // Top view
                        yaw = 0f
                        panX = 0f
                        panY = 0f
                    },
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = CyberBackground.copy(alpha = 0.85f),
                        contentColor = ElectricBlue
                    )
                ) {
                    Icon(Icons.Default.ViewInAr, contentDescription = "Draufsicht (Top View)", modifier = Modifier.size(18.dp))
                }

                FilledTonalIconButton(
                    onClick = {
                        panX = 0f
                        panY = 0f
                        onCenterOnBed()
                    },
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = CyberBackground.copy(alpha = 0.85f),
                        contentColor = TechLime
                    )
                ) {
                    Icon(Icons.Default.CenterFocusStrong, contentDescription = "Auf Bett zentrieren", modifier = Modifier.size(18.dp))
                }
            }

            Surface(
                color = CyberBackground.copy(alpha = 0.85f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
            ) {
                Text(
                    text = "Bett: CR-10S Pro 300x300mm",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }
        }
    }
}
