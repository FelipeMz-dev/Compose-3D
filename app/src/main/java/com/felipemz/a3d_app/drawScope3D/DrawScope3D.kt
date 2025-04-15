package com.felipemz.a3d_app.drawScope3D

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import com.felipemz.a3d_app.Scene
import com.felipemz.a3d_app.model.Camera
import com.felipemz.a3d_app.model.Matrix3x3
import com.felipemz.a3d_app.model.SceneObject
import com.felipemz.a3d_app.model.Triangle3D
import com.felipemz.a3d_app.model.Vertex
import com.felipemz.a3d_app.shapes.CubeFactory
import com.felipemz.a3d_app.utils.MathUtils.computeDirection
import com.felipemz.a3d_app.utils.MathUtils.computeNormal
import com.felipemz.a3d_app.utils.MathUtils.scaleByCentroid
import com.felipemz.a3d_app.utils.PathUtils.pathFromOffsets
import com.felipemz.a3d_app.utils.ProjectionUtils
import kotlin.math.floor

interface DrawScope3D : DrawScope {

    fun drawTriangleObject(
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

    private val projector = ProjectionUtils(camera)
    private val triangleZBuffer = mutableListOf<Triangle3D>()

    override fun drawCubeObject(
        obj: SceneObject,
        size: Float,
    ) {
        val vertices = CubeFactory.createVertices(size)
        val triangles = CubeFactory.createTriangles(vertices)
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
            drawTriangleObject(obj, a, b, c, colors[i])
        }
    }

    override fun drawTriangleObject(
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
        val cameraDir = computeDirection(camera.position, aWorld, bWorld, cWorld)

        if (normal dot cameraDir < 0f) return

        val triangle = Triangle3D(
            aWorld, bWorld, cWorld,
            zIndex = computeZIndex(aWorld, bWorld, cWorld),
            color = color,
            owner = obj
        )

        triangleZBuffer.add(triangle)
    }

    override fun drawScene(scene: Scene) {
        triangleZBuffer.clear()
        scene.objects.forEach { it.draw(this, it) }

        val clipped = mutableListOf<Triangle3D>()

        triangleZBuffer.forEach { triangleA ->
            val projected1 = listOfNotNull(
                projector.project(triangleA.a),
                projector.project(triangleA.b),
                projector.project(triangleA.c)
            )
            if (projected1.size == 3) {

                //var pathA = pathFromOffsets(projected1)
                var color = triangleA.color

                val trianglesToDraw = mutableListOf<Triangle3D>()

                triangleZBuffer.forEach { triangleB ->

                    if (triangleA != triangleB) {

                        if (TriangleIntersect.intersect(
                                triangleA.a, triangleA.b, triangleA.c,
                                triangleB.a, triangleB.b, triangleB.c
                            )
                        ) {

                            // test intersection
                            color = triangleA.color.copy(alpha = 0.5f)

                            val polygonA = listOf(triangleA.a, triangleA.b, triangleA.c)
                            val polygonB = listOf(triangleB.a, triangleB.b, triangleB.c)

                            val frontParts = clipPolygonWithTriangle(polygonA, polygonB)
                            val backParts = clipPolygonWithTriangle(polygonA, polygonB, invert = true)

                            for (tri in frontParts) {
                                trianglesToDraw.add(
                                    Triangle3D(
                                        tri[0], tri[1], tri[2],
                                        computeZIndex(tri[0], tri[1], tri[2]),
                                        triangleA.color, triangleA.owner
                                    )
                                )
                            }

                            for (tri in backParts) {
                                trianglesToDraw.add(
                                    Triangle3D(
                                        tri[0], tri[1], tri[2],
                                        computeZIndex(tri[0], tri[1], tri[2]) * 1.2f,
                                        color, triangleA.owner
                                    )
                                )
                            }
                        }
                    }
                }
                if (trianglesToDraw.isEmpty()) clipped.add(triangleA)
                else {
                    clipped.addAll(trianglesToDraw)
                    trianglesToDraw.clear()
                }
            }
        }

        clipped.sortedByDescending { it.zIndex }.forEach {
            val projected = listOfNotNull(
                projector.project(it.a),
                projector.project(it.b),
                projector.project(it.c)
            )
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

    fun computeZIndex(
        a: Vertex,
        b: Vertex,
        c: Vertex
    ): Float {
        return (projector.distanceToProjectionZ(a) +
                projector.distanceToProjectionZ(b) +
                projector.distanceToProjectionZ(c)) / 3f
    }
}
