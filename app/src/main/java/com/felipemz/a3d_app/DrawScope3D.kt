package com.felipemz.a3d_app

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import com.felipemz.a3d_app.model.Camera
import com.felipemz.a3d_app.model.Matrix3x3
import com.felipemz.a3d_app.model.SceneObject
import com.felipemz.a3d_app.model.Triangle3D
import com.felipemz.a3d_app.model.Vertex
import kotlin.math.floor

interface DrawScope3D : DrawScope {

    fun drawRelativeTriangleZBuffered(
        obj: SceneObject,
        a: Vertex,
        b: Vertex,
        c: Vertex,
        color: Color
    )

    fun drawCubeObject(
        obj: SceneObject,
        size: Float = 0.5f
    )

    fun drawScene(scene: Scene)
}

class DrawScope3DImpl(
    private val textMeasurer: TextMeasurer,
    private val camera: Camera,
    private val drawScope: DrawScope,
    private val showOutline: Boolean,
) : DrawScope by drawScope, DrawScope3D {

    private val triangleZBuffer = mutableListOf<Triangle3D>()

    override fun drawCubeObject(
        obj: SceneObject,
        size: Float,
    ) {
        val v = listOf(
            Vertex(-size, -size, -size),
            Vertex(size, -size, -size),
            Vertex(size, size, -size),
            Vertex(-size, size, -size),
            Vertex(-size, -size, size),
            Vertex(size, -size, size),
            Vertex(size, size, size),
            Vertex(-size, size, size)
        )

        val triangles = listOf(
            Triple(v[0], v[1], v[2]), Triple(v[0], v[2], v[3]), // Cara trasera
            Triple(v[4], v[6], v[5]), Triple(v[4], v[7], v[6]), // Cara frontal
            Triple(v[0], v[3], v[7]), Triple(v[0], v[7], v[4]), // Izquierda
            Triple(v[1], v[5], v[6]), Triple(v[1], v[6], v[2]), // Derecha
            Triple(v[3], v[2], v[6]), Triple(v[3], v[6], v[7]), // Arriba
            Triple(v[0], v[4], v[5]), Triple(v[0], v[5], v[1]) // Abajo
        )

        val colors = listOf(
            Color.Gray, Color.Gray,
            Color.Blue, Color.Blue,
            Color.Red, Color.Red,
            Color.Green, Color.Green,
            Color.Yellow, Color.Yellow,
            Color.Magenta, Color.Magenta
        )

        for (i in triangles.indices) {
            val (a, b, c) = triangles[i]
            drawRelativeTriangleZBuffered(obj, a, b, c, colors[i])
        }
    }

    private fun scaleByCentroid(
        a: Vertex,
        b: Vertex,
        c: Vertex,
        scaleFactor: Float
    ): Triple<Vertex, Vertex, Vertex> {
        val centroid = (a + b + c) / 3f
        return Triple(
            centroid + (a - centroid) * scaleFactor,
            centroid + (b - centroid) * scaleFactor,
            centroid + (c - centroid) * scaleFactor
        )
    }

    override fun drawRelativeTriangleZBuffered(
        obj: SceneObject,
        a: Vertex,
        b: Vertex,
        c: Vertex,
        color: Color
    ) {
        val modelMatrix = Matrix3x3.rotation(obj.rotation)

        val (sa, sb, sc) = scaleByCentroid(a, b, c, 1f)

        val aWorld = modelMatrix * (sa * obj.scale) + obj.position
        val bWorld = modelMatrix * (sb * obj.scale) + obj.position
        val cWorld = modelMatrix * (sc * obj.scale) + obj.position

        val normal = computeNormal(aWorld, bWorld, cWorld)
        val cameraDir = computeDirection(aWorld, bWorld, cWorld)

        if (normal dot cameraDir < 0f) return

        val triangle = Triangle3D(aWorld, bWorld, cWorld, color = color, owner = obj)

        triangleZBuffer.add(triangle)
    }

    private fun project(vertex: Vertex): Offset? {
        val x = vertex.x - camera.position.x
        val y = vertex.y - camera.position.y
        val z = vertex.z - camera.position.z

        val cosX = kotlin.math.cos(camera.angle.x)
        val sinX = kotlin.math.sin(camera.angle.x)
        val cosY = kotlin.math.cos(camera.angle.y)
        val sinY = kotlin.math.sin(camera.angle.y)
        val cosZ = kotlin.math.cos(camera.angle.z)
        val sinZ = kotlin.math.sin(camera.angle.z)

        val pointerX = cosY * (sinZ * y + cosZ * x) - sinY * z
        val pointerY = sinX * (cosY * z + sinY * (sinZ * y + cosZ * x)) + cosX * (cosZ * y - sinZ * x)
        val pointerZ = cosX * (cosY * z + sinY * (sinZ * y + cosZ * x)) - sinX * (cosZ * y - sinZ * x)

        return Offset(
            x = (pointerX * camera.focalLength / pointerZ) + camera.screenSize.width / 2,
            y = (pointerY * camera.focalLength / pointerZ) + camera.screenSize.height / 2
        ).takeIf { pointerZ > 0 }
    }

    override fun drawScene(scene: Scene) {
        triangleZBuffer.clear()
        scene.objects.forEach { it.draw(this, it) }

        val clipped = triangleZBuffer.toList() //.sortedByDescending { it.zIndex }.toMutableList()

        for (i in clipped.indices) {
            val triangleA = clipped[i]
            val projected1 = listOfNotNull(project(triangleA.a), project(triangleA.b), project(triangleA.c))
            if (projected1.size != 3) continue

            //var pathA = pathFromOffsets(projected1)
            var color = triangleA.color

            triangleZBuffer.remove(clipped[i])
            val trianglesToDraw = mutableListOf<Triangle3D>()

            for (j in clipped.indices) {
                val triangleB = clipped[j]

                if (j == i) continue

                if (TriangleIntersect.intersect(
                        triangleA.a, triangleA.b, triangleA.c,
                        triangleB.a, triangleB.b, triangleB.c
                    )
                ) {

                    color = triangleA.color.copy(alpha = 0.5f)

                    val polygonA = listOf(triangleA.a, triangleA.b, triangleA.c)
                    val polygonB = listOf(triangleB.a, triangleB.b, triangleB.c)

                    val frontParts = clipPolygonWithTriangle(polygonA, polygonB)
                    val backParts = clipPolygonWithTriangle(polygonA, polygonB, invert = true)

                    for (tri in frontParts) {
                        trianglesToDraw.add(
                            Triangle3D(tri[0], tri[1], tri[2], computeZIndex(tri), triangleA.color, triangleA.owner)
                        )
                    }

                    for (tri in backParts) {
                        trianglesToDraw.add(
                            Triangle3D(tri[0], tri[1], tri[2], computeZIndex(tri) * 1.2f, color, triangleA.owner)
                        )
                    }
                }
            }
            if (trianglesToDraw.isEmpty()) trianglesToDraw.add(
                triangleA.copy(
                    zIndex = computeZIndex(listOf(triangleA.a, triangleA.b, triangleA.c))
                )
            )
            triangleZBuffer.addAll(trianglesToDraw)
            trianglesToDraw.clear()
        }

        triangleZBuffer.sortedByDescending { it.zIndex }.forEach {
            val projected = listOfNotNull(project(it.a), project(it.b), project(it.c))
            if (projected.size >= 3) {
                drawScope.drawPath(pathFromOffsets(projected), it.color)
                if (showOutline) {
                    drawScope.drawLine(Color.Black, projected[0], projected[1])
                    drawScope.drawLine(Color.Black, projected[1], projected[2])
                    drawScope.drawLine(Color.Black, projected[2], projected[0])
                    val projectCenterOffset = Offset(
                        (projected[0].x + projected[1].x + projected[2].x) / 3,
                        (projected[0].y + projected[1].y + projected[2].y) / 3
                    )
                    drawText(
                        textMeasurer,
                        text = "${floor(it.zIndex)}",
                        topLeft = projectCenterOffset,
                        size = Size(100f, 100f),
                    )
                }
            }
        }

        triangleZBuffer.clear()
    }

    private fun computeDirection(
        a: Vertex,
        b: Vertex,
        c: Vertex
    ) = (camera.position - ((a + b + c) / 3f)).normalize()

    private fun computeNormal(
        p1: Vertex,
        p2: Vertex,
        p3: Vertex
    ): Vertex {
        val u = p2 - p1
        val v = p3 - p1
        return v.cross(u).normalize()
    }

    private fun distanceToCamera(v: Vertex): Float {
        val dx = v.x - camera.position.x
        val dy = v.y - camera.position.y
        val dz = v.z - camera.position.z
        return dx * dx + dy * dy + dz * dz
    }

    private fun pathFromOffsets(offsets: List<Offset>): Path {
        return Path().apply {
            moveTo(offsets[0].x, offsets[0].y)
            lineTo(offsets[1].x, offsets[1].y)
            lineTo(offsets[2].x, offsets[2].y)
            close()
        }
    }

    fun clipPolygonWithTriangle(
        clippee: List<Vertex>,
        clipper: List<Vertex>,
        invert: Boolean = false
    ): List<List<Vertex>> {
        var result = clippee

        for (i in clipper.indices) {
            val a = clipper[i]
            val b = clipper[(i + 1) % clipper.size]
            val edge = b - a
            val rawNormal = (clipper[(i + 2) % clipper.size] - b).cross(edge).normalize() // plano perpendicular

            val normal = if (invert) -rawNormal else rawNormal

            result = clipPolygonByPlane(result, a, normal)
            if (result.isEmpty()) break
        }

        return triangulate(result)
    }

    fun triangulate(poly: List<Vertex>): List<List<Vertex>> {
        if (poly.size < 3) return emptyList()
        val triangles = mutableListOf<List<Vertex>>()
        for (i in 1 until poly.size - 1) {
            triangles.add(listOf(poly[0], poly[i], poly[i + 1]))
        }
        return triangles
    }

    fun clipPolygonByPlane(
        polygon: List<Vertex>,
        planePoint: Vertex,
        planeNormal: Vertex
    ): List<Vertex> {
        if (polygon.size < 3) return emptyList()

        val output = mutableListOf<Vertex>()

        fun distance(v: Vertex): Float = (v - planePoint).dot(planeNormal)

        for (i in polygon.indices) {
            val current = polygon[i]
            val next = polygon[(i + 1) % polygon.size]
            val dCurrent = distance(current)
            val dNext = distance(next)

            if (dCurrent >= 0f) {
                if (dNext >= 0f) {
                    output.add(next)
                } else {
                    val t = dCurrent / (dCurrent - dNext)
                    val intersect = current + (next - current) * t
                    output.add(intersect)
                }
            } else {
                if (dNext >= 0f) {
                    val t = dCurrent / (dCurrent - dNext)
                    val intersect = current + (next - current) * t
                    output.add(intersect)
                    output.add(next)
                }
            }
        }

        return output
    }

    fun computeZIndex(vertices: List<Vertex>): Float {
        if (vertices.isEmpty()) return Float.MAX_VALUE
        return vertices.sumOf { distanceToCameraZ(it).toDouble() }.toFloat() / vertices.size
    }

    private fun distanceToCameraZ(v: Vertex): Float {
        val dx = v.x - camera.position.x
        val dy = v.y - camera.position.y
        val dz = v.z - camera.position.z

        val cosX = kotlin.math.cos(camera.angle.x)
        val sinX = kotlin.math.sin(camera.angle.x)
        val cosY = kotlin.math.cos(camera.angle.y)
        val sinY = kotlin.math.sin(camera.angle.y)
        val cosZ = kotlin.math.cos(camera.angle.z)
        val sinZ = kotlin.math.sin(camera.angle.z)

        return cosX * (cosY * dz + sinY * (sinZ * dy + cosZ * dx)) - sinX * (cosZ * dy - sinZ * dx)
    }
}

object TriangleIntersect {

    const val EPSILON = 1e-5f

    fun intersect(
        a1: Vertex,
        b1: Vertex,
        c1: Vertex,
        a2: Vertex,
        b2: Vertex,
        c2: Vertex
    ): Boolean {
        val n1 = (b1 - a1).cross(c1 - a1)
        val d1 = -(n1 dot a1)

        val distA2 = (n1 dot a2) + d1
        val distB2 = (n1 dot b2) + d1
        val distC2 = (n1 dot c2) + d1

        val side1 = (distA2 > EPSILON) || (distB2 > EPSILON) || (distC2 > EPSILON)
        val side2 = (distA2 < -EPSILON) || (distB2 < -EPSILON) || (distC2 < -EPSILON)


        if (!side1 || !side2) return false

        val n2 = (b2 - a2).cross(c2 - a2)
        val d2 = -(n2 dot a2)

        val distA1 = (n2 dot a1) + d2
        val distB1 = (n2 dot b1) + d2
        val distC1 = (n2 dot c1) + d2

        val side3 = (distA1 > 0f) || (distB1 > 0f) || (distC1 > 0f)
        val side4 = (distA1 < 0f) || (distB1 < 0f) || (distC1 < 0f)

        if (!side3 || !side4) return false

        return true
    }
}
