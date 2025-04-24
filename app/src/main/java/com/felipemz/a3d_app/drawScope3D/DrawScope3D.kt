package com.felipemz.a3d_app.drawScope3D

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import com.felipemz.a3d_app.Scene
import com.felipemz.a3d_app.drawScope3D.ClippingIntersection.clipLine
import com.felipemz.a3d_app.drawScope3D.ClippingIntersection.clipPolygonWithTriangle
import com.felipemz.a3d_app.model.Camera
import com.felipemz.a3d_app.model.Matrix3x3
import com.felipemz.a3d_app.model.SceneObject
import com.felipemz.a3d_app.model.Triangle3D
import com.felipemz.a3d_app.model.Vertex
import com.felipemz.a3d_app.model.VertexUV
import com.felipemz.a3d_app.shapes.CubeFactory
import com.felipemz.a3d_app.utils.MathUtils.computeDirection
import com.felipemz.a3d_app.utils.MathUtils.computeNormal
import com.felipemz.a3d_app.utils.MathUtils.scaleByCentroid
import com.felipemz.a3d_app.utils.Offset3DUtils.adjustPoint
import com.felipemz.a3d_app.utils.Offset3DUtils.pathFromOffsets
import com.felipemz.a3d_app.utils.ProjectionUtils
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

interface DrawScope3D : DrawScope {

    fun drawTriangleObject(
        obj: SceneObject,
        a: Vertex,
        b: Vertex,
        c: Vertex,
        color: Color = Color.LightGray,
        image: ImageBitmap? = null,
        textureVertices: List<Offset>? = null
    )

    fun drawCubeObject(
        obj: SceneObject,
        size: Float = 0.5f,
        faceColor: (Int) -> Color = { Color.LightGray },
        faceImage: (Int) -> ImageBitmap? = { null }
    )

    fun drawGrid(
        position: Vertex = Vertex(),
        unitSize: Size = Size(10f, 10f),
        color: Color = Color.Gray,
        rows: Int = 10,
        columns: Int = 10,
        strokeWidth: Float = 2f,
    )

    fun drawLine(
        start: Vertex,
        end: Vertex,
        color: Color,
        strokeWidth: Float
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

    fun applyTextureToTriangle(
        canvas: android.graphics.Canvas,
        path: androidx.compose.ui.graphics.Path,
        bitmap: ImageBitmap,
        projectedVertices: List<Offset>,
        textureVertices: List<Offset>,
        position: Offset = Offset(0f, 0f),
        angle: Float = 0f,
        scale: Float = 1f
    ) {
        val paint = android.graphics.Paint().apply {
            shader = android.graphics.BitmapShader(
                bitmap.asAndroidBitmap(),
                android.graphics.Shader.TileMode.CLAMP,
                android.graphics.Shader.TileMode.CLAMP
            )
        }

        val matrix = android.graphics.Matrix().apply {
            // Transformación de la textura
            setPolyToPoly(
                floatArrayOf(
                    textureVertices[0].x, textureVertices[0].y,
                    textureVertices[1].x, textureVertices[1].y,
                    textureVertices[2].x, textureVertices[2].y
                ),
                0,
                floatArrayOf(
                    projectedVertices[0].x, projectedVertices[0].y,
                    projectedVertices[1].x, projectedVertices[1].y,
                    projectedVertices[2].x, projectedVertices[2].y
                ),
                0,
                3
            )
            // Aplicar posición, rotación y escala
            postTranslate(position.x, position.y)
            postRotate(angle, position.x, position.y)
            postScale(scale, scale, position.x, position.y)
        }

        paint.shader.setLocalMatrix(matrix)
        canvas.drawPath(path.asAndroidPath(), paint)
    }

    override fun drawCubeObject(
        obj: SceneObject,
        size: Float,
        faceColor: (Int) -> Color,
        faceImage: (Int) -> ImageBitmap?
    ) {
        val vertices = CubeFactory.createVertices(size)
        val triangles = CubeFactory.createTriangles(vertices)

        for (i in triangles.indices) {
            val (a, b, c) = triangles[i]
            val faceIndex = i / 2
            val image = faceImage(faceIndex)
            image?.let { bitmap ->
                val w = bitmap.width.toFloat()
                val h = bitmap.height.toFloat()
                val textureVertices = CubeFactory.createTextures(i, Size(w, h))
                drawTriangleObject(obj, a, b, c, faceColor(i), image, textureVertices)
            } ?: drawTriangleObject(obj, a, b, c, faceColor(i), null)
        }
    }

    override fun drawGrid(
        position: Vertex,
        unitSize: Size,
        color: Color,
        rows: Int,
        columns: Int,
        strokeWidth: Float,
    ) {
        val halfRows = rows / 2f
        val halfColumns = columns / 2f

        for (i in -halfRows.toInt()..halfRows.toInt()) {
            val start = Vertex(-halfColumns * unitSize.width, 0f, i * unitSize.height) + position
            val end = Vertex(halfColumns * unitSize.width,0f, i * unitSize.height) + position
            drawLine(start, end, color, strokeWidth)
        }

        for (i in -halfColumns.toInt()..halfColumns.toInt()) {
            val start = Vertex(i * unitSize.width, 0f, -halfRows * unitSize.height) + position
            val end = Vertex(i * unitSize.width, 0f, halfRows * unitSize.height) + position
            drawLine(start, end, color, strokeWidth)
        }
    }

    override fun drawLine(
        start: Vertex,
        end: Vertex,
        color: Color,
        strokeWidth: Float
    ) {

        val startZ = projector.distanceToProjectionZ(start)
        val endZ = projector.distanceToProjectionZ(end)

        if (startZ < 0f && endZ < 0f) return

        val (clippedStart, clippedEnd) = clipLine(start, end, startZ, endZ)

        val projectedStart = projector.project(clippedStart)
        val projectedEnd = projector.project(clippedEnd)

        if (projectedStart != null && projectedEnd != null) {
            drawScope.drawLine(
                color = color,
                start = projectedStart,
                end = projectedEnd,
                strokeWidth = strokeWidth
            )
        }
    }

    override fun drawTriangleObject(
        obj: SceneObject,
        a: Vertex,
        b: Vertex,
        c: Vertex,
        color: Color,
        image: ImageBitmap?,
        textureVertices: List<Offset>?
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
            zIndex = projector.computeZIndex(aWorld, bWorld, cWorld),
            color = color,
            imageBitmap = image,
            owner = obj,
            textureVertices = textureVertices
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

                val trianglesToDraw = mutableListOf<Triangle3D>()

                triangleZBuffer.forEach { triangleB ->

                    if (triangleA != triangleB) {

                        if (ClippingIntersection.intersect(
                                triangleA.a, triangleA.b, triangleA.c,
                                triangleB.a, triangleB.b, triangleB.c
                            )
                        ) {

                            val uvList = triangleA.textureVertices ?: listOf(Offset.Zero, Offset.Zero, Offset.Zero)
                            val polygonA = listOf(
                                VertexUV(triangleA.a, uvList[0]),
                                VertexUV(triangleA.b, uvList[1]),
                                VertexUV(triangleA.c, uvList[2])
                            )

                            val polygonB = listOf(triangleB.a, triangleB.b, triangleB.c)

                            val frontParts = clipPolygonWithTriangle(polygonA, polygonB)
                            val backParts = clipPolygonWithTriangle(polygonA, polygonB, invert = true)

                            for (tri in frontParts) {
                                trianglesToDraw.add(
                                    Triangle3D(
                                        tri[0].position, tri[1].position, tri[2].position,
                                        projector.computeZIndex(tri[0].position, tri[1].position, tri[2].position),
                                        triangleA.color, triangleA.imageBitmap ,triangleA.owner,
                                        listOf(tri[0].uv, tri[1].uv, tri[2].uv)
                                    )
                                )
                            }

                            for (tri in backParts) {
                                trianglesToDraw.add(
                                    Triangle3D(
                                        tri[0].position, tri[1].position, tri[2].position,
                                        projector.computeZIndex(tri[0].position, tri[1].position, tri[2].position) * 1.2f,
                                        triangleA.color, triangleA.imageBitmap, triangleA.owner,
                                        listOf(tri[0].uv, tri[1].uv, tri[2].uv)
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
                val path = pathFromOffsets(projected)
                it.imageBitmap?.let { bitmap ->
                    it.textureVertices?.let { uv ->
                        drawScope.clipPath(path) {
                            drawIntoCanvas { canvas ->
                                val paint = android.graphics.Paint().apply {
                                    shader = android.graphics.BitmapShader(
                                        bitmap.asAndroidBitmap(),
                                        android.graphics.Shader.TileMode.REPEAT,
                                        android.graphics.Shader.TileMode.REPEAT
                                    )
                                }
                                val matrix = android.graphics.Matrix().apply {
                                    setPolyToPoly(
                                        floatArrayOf(
                                            uv[0].x, uv[0].y,
                                            uv[1].x, uv[1].y,
                                            uv[2].x, uv[2].y
                                        ),
                                        0,
                                        floatArrayOf(
                                            projected[0].x, projected[0].y,
                                            projected[1].x, projected[1].y,
                                            projected[2].x, projected[2].y
                                        ),
                                        0,
                                        3
                                    )
                                }
                                paint.shader.setLocalMatrix(matrix)
                                canvas.nativeCanvas.drawPath(path.asAndroidPath(), paint)
                            }
                        }
                    }
                } ?: drawScope.drawPath(path, it.color)
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
                        size = Size(100f, 60f),
                    )
                }
            }
        }

        triangleZBuffer.clear()
    }
}
