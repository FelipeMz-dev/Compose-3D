package com.felipemz.a3d_app.drawScope3D

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import com.felipemz.a3d_app.model.Scene
import com.felipemz.a3d_app.drawScope3D.BSP.drawBSP
import com.felipemz.a3d_app.drawScope3D.BSP.insertTriangle
import com.felipemz.a3d_app.drawScope3D.Clipping.clipLine
import com.felipemz.a3d_app.model.Camera
import com.felipemz.a3d_app.model.Matrix3x3
import com.felipemz.a3d_app.model.Polygon
import com.felipemz.a3d_app.model.SceneObject
import com.felipemz.a3d_app.model.Triangle3D
import com.felipemz.a3d_app.model.Vertex
import com.felipemz.a3d_app.shapes.CubeFactory
import com.felipemz.a3d_app.shapes.SphereFactory
import com.felipemz.a3d_app.utils.MathUtils.computeDirection
import com.felipemz.a3d_app.utils.MathUtils.computeNormal
import com.felipemz.a3d_app.utils.MathUtils.scaleByCentroid
import com.felipemz.a3d_app.utils.Offset3DUtils.isPathOutOfBounds
import com.felipemz.a3d_app.utils.Offset3DUtils.pathFromOffsets
import com.felipemz.a3d_app.utils.ProjectionUtils

interface DrawScope3D : DrawScope {

    fun drawTriangleFromObject(
        obj: SceneObject,
        a: Vertex,
        b: Vertex,
        c: Vertex,
        color: Color = Color.LightGray,
        image: ImageBitmap? = null,
        textureVertices: List<Offset>? = null,
        scale: Float = 1f
    )

    fun drawCubeObject(
        obj: SceneObject,
        size: Float = 0.5f,
        faceColor: (Int) -> Color = { Color.LightGray },
        faceImage: (Int) -> ImageBitmap? = { null }
    )

    fun drawSphereObject(
        obj: SceneObject,
        radius: Float = 0.5f,
        latitudeBands: Int = 16,
        longitudeBands: Int = 16,
        imageSize: Size? = null,
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

    private var triangleBSPTree: BSPNode? = null

    private val projector = ProjectionUtils(camera)

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
                drawTriangleFromObject(obj, a, b, c, faceColor(i), image, textureVertices)
            } ?: drawTriangleFromObject(obj, a, b, c, faceColor(i), null)
        }
    }

    override fun drawSphereObject(
        obj: SceneObject,
        radius: Float,
        latitudeBands: Int,
        longitudeBands: Int,
        imageSize: Size?,
        faceColor: (Int) -> Color,
        faceImage: (Int) -> ImageBitmap?
    ) {
        val triangles = SphereFactory.createSphere(
            radius = radius,
            latitudeBands = latitudeBands,
            longitudeBands = longitudeBands,
            center = obj.position,
            imageSize = imageSize
        )

        for (i in triangles.indices) {
            val triangle = triangles[i]
            val faceIndex = i / 2
            val image = faceImage(faceIndex)
            image?.let { bitmap ->
                drawTriangleFromObject(
                    obj, triangle.polygon.first, triangle.polygon.second, triangle.polygon.third, faceColor(i), bitmap, triangle.textureVertices
                )
            } ?: drawTriangleFromObject(obj, triangle.polygon.first, triangle.polygon.second, triangle.polygon.third, faceColor(i), null)
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
            val end = Vertex(halfColumns * unitSize.width, 0f, i * unitSize.height) + position
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

    override fun drawTriangleFromObject(
        obj: SceneObject,
        a: Vertex,
        b: Vertex,
        c: Vertex,
        color: Color,
        image: ImageBitmap?,
        textureVertices: List<Offset>?,
        scale: Float,
    ) {
        val modelMatrix = Matrix3x3.rotation(obj.rotation)

        val (sa, sb, sc) = scaleByCentroid(a, b, c, scale)

        val aWorld = modelMatrix * (sa * obj.scale) + obj.position
        val bWorld = modelMatrix * (sb * obj.scale) + obj.position
        val cWorld = modelMatrix * (sc * obj.scale) + obj.position

        val normal = computeNormal(aWorld, bWorld, cWorld)
        val cameraDir = computeDirection(camera.position, aWorld, bWorld, cWorld)

        if (normal dot cameraDir < 0f) return

        val projected = listOfNotNull(
            projector.project(aWorld),
            projector.project(bWorld),
            projector.project(cWorld)
        )
        val path = pathFromOffsets(projected)
        if (isPathOutOfBounds(size, path)) return

        val triangle = Triangle3D(
            polygon = Polygon(aWorld, bWorld, cWorld),
            zIndex = projector.computeZIndex(aWorld, bWorld, cWorld),
            color = color,
            imageBitmap = image,
            owner = obj,
            textureVertices = textureVertices
        )

        triangleBSPTree = insertTriangle(triangleBSPTree, triangle)
    }

    override fun drawScene(scene: Scene) {
        var index = 0
        scene.objects.forEach { it.draw(this@DrawScope3DImpl, it) }

        drawBSP(triangleBSPTree, camera.position) { triangle ->
            drawTriangleProjected(triangle, scene.idIndex)
            index++
        }
        scene.objects.firstOrNull { it.id == scene.idIndex }?.apply { drawAxes(this) }
        triangleBSPTree = null
    }

    private fun drawTriangleProjected(
        triangle: Triangle3D,
        index: Int
    ) {
        val projected = listOfNotNull(
            projector.project(triangle.polygon.first),
            projector.project(triangle.polygon.second),
            projector.project(triangle.polygon.third)
        )
        if (projected.size == 3) {
            val path = pathFromOffsets(projected)
            when {
                path.isEmpty -> return
                isPathOutOfBounds(size, path) -> return
                else -> {
                    triangle.imageBitmap?.let { bitmap ->
                        triangle.textureVertices?.let { uv ->
                            bitmap.applyTexture(uv, path, projected)
                        }
                    } ?: drawScope.drawPath(path, triangle.color.copy(alpha = 0.8f))
                    if (showOutline && triangle.owner?.id == index) {
                        drawScope.drawLine(Color.Black, projected[0], projected[1])
                        drawScope.drawLine(Color.Black, projected[1], projected[2])
                        drawScope.drawLine(Color.Black, projected[2], projected[0])
                    }
                }
            }
        }
    }

    private fun drawText(
        text: String,
        topLeft: Offset,
        size: Size
    ) {
        drawScope.drawText(
            textMeasurer,
            text = text,
            topLeft = topLeft,
            size = size,
        )
    }

    private fun ImageBitmap.applyTexture(
        uv: List<Offset>,
        path: Path,
        projected: List<Offset>,
    ) {
        drawScope.clipPath(path) {
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    shader = android.graphics.BitmapShader(
                        this@applyTexture.asAndroidBitmap(),
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

    private fun drawAxes(obj: SceneObject, length: Float = 10f) {
        val modelMatrix = Matrix3x3.rotation(obj.rotation)
        val center = obj.position

        val xAxis = modelMatrix * Vertex(length, 0f, 0f) + center
        val yAxis = modelMatrix * Vertex(0f, length, 0f) + center
        val zAxis = modelMatrix * Vertex(0f, 0f, length) + center

        drawLine(center, xAxis, Color.Red, strokeWidth = 4f)
        drawLine(center, yAxis, Color.Green, strokeWidth = 4f)
        drawLine(center, zAxis, Color.Blue, strokeWidth = 4f)
    }
}