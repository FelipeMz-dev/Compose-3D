package com.felipemz.a3d_app

/*
* override fun drawSquareLined(
        position: Vertex,
        angle: Vertex,
        size: Size
    ) {
        val rotationMatrix = Matrix3x3.rotation(angle)

        fun rotatedVertex(x: Float, y: Float, z: Float): Vertex {
            val rotated = rotationMatrix * Vertex(x, y, z)
            return Vertex(
                rotated.x + position.x,
                rotated.y + position.y,
                rotated.z + position.z
            )
        }

        val halfW = size.width / 2f
        val halfH = size.height / 2f

        val lines = listOf(
            Triple(-halfW, -halfH, 0f) to Triple(halfW, -halfH, 0f),
            Triple(-halfW, halfH, 0f) to Triple(-halfW, -halfH, 0f),
            Triple(halfW, halfH, 0f) to Triple(-halfW, halfH, 0f),
            Triple(halfW, -halfH, 0f) to Triple(halfW, halfH, 0f)
        )

        lines.forEach { (start, end) ->
            draw3DLine(
                node1 = rotatedVertex(start.first, start.second, start.third),
                node2 = rotatedVertex(end.first, end.second, end.third),
                color = Color.DarkGray,
                strokeWidth = 5f
            )
        }
    }
    * override fun draw3DLine(
        node1: Vertex,
        node2: Vertex,
        color: Color,
        strokeWidth: Float
    ) {
        var start = project(node1)
        var end = project(node2)
        val min = Offset.Zero
        val max = Offset(size.width, size.height)

        if (start != null && end != null) {

            if ((start.x < min.x && end.x < min.x) || (start.x > max.x && end.x > max.x) ||
                (start.y < min.y && end.y < min.y) || (start.y > max.y && end.y > max.y)) return

            start = adjustPoint(start, end, min, max)
            end = adjustPoint(end, start, min, max)

            drawScope.drawLine(
                color = color,
                start = start,
                end = end,
                strokeWidth = strokeWidth
            )
        }
    }

    override fun drawCubeLined(
        position: Vertex,
        angle: Vertex,
        scale: Vertex
    ) {

        val (cosX, sinX) = kotlin.math.cos(angle.x) to kotlin.math.sin(angle.x)
        val (cosY, sinY) = kotlin.math.cos(angle.y) to kotlin.math.sin(angle.y)
        val (cosZ, sinZ) = kotlin.math.cos(angle.z) to kotlin.math.sin(angle.z)
        val size = scale / 2f

        fun rotate(vertex: Vertex): Vertex {

            val (x, y, z) = vertex

            val newX = cosY * (sinZ * y + cosZ * x) - sinY * z
            val newY = sinX * (cosY * z + sinY * (sinZ * y + cosZ * x)) + cosX * (cosZ * y - sinZ * x)
            val newZ = cosX * (cosY * z + sinY * (sinZ * y + cosZ * x)) - sinX * (cosZ * y - sinZ * x)

            return Vertex(newX + position.x, newY + position.y, newZ + position.z)
        }

        fun rotatedVertex(
            x: Float,
            y: Float,
            z: Float
        ) = rotate(Vertex(x, y, z)).let {
            Vertex(it.x + position.x, it.y + position.y, it.z + position.z)
        }

        val lines = listOf(
            Triple(-size.x, -size.y, -size.z) to Triple(size.x, -size.y, -size.z),
            Triple(-size.x, size.y, -size.z) to Triple(-size.x, -size.y, -size.z),
            Triple(size.x, size.y, -size.z) to Triple(-size.x, size.y, -size.z),
            Triple(size.x, -size.y, -size.z) to Triple(size.x, size.y, -size.z),

            Triple(-size.x, -size.y, size.z) to Triple(size.x, -size.y, size.z),
            Triple(-size.x, size.y, size.z) to Triple(-size.x, -size.y, size.z),
            Triple(size.x, size.y, size.z) to Triple(-size.x, size.y, size.z),
            Triple(size.x, -size.y, size.z) to Triple(size.x, size.y, size.z),

            Triple(-size.x, -size.y, -size.z) to Triple(-size.x, -size.y, size.z),
            Triple(-size.x, size.y, -size.z) to Triple(-size.x, size.y, size.z),
            Triple(size.x, -size.y, -size.z) to Triple(size.x, -size.y, size.z),
            Triple(size.x, size.y, -size.z) to Triple(size.x, size.y, size.z),
        )

        lines.forEach { (start, end) ->
            draw3DLine(
                node1 = rotatedVertex(start.first, start.second, start.third),
                node2 = rotatedVertex(end.first, end.second, end.third),
                color = Color.DarkGray,
                strokeWidth = 5f
            )
        }
    }
    * override fun drawCube(
        position: Vertex,
        angle: Vertex,
        scale: Vertex,
        center: Vertex,
    ) {
        val half = scale / 2f

        val vertices = listOf(
            Vertex(-half.x, -half.y, -half.z), // 0
            Vertex(half.x, -half.y, -half.z),  // 1
            Vertex(half.x, half.y, -half.z),   // 2
            Vertex(-half.x, half.y, -half.z),  // 3
            Vertex(-half.x, -half.y, half.z),  // 4
            Vertex(half.x, -half.y, half.z),   // 5
            Vertex(half.x, half.y, half.z),    // 6
            Vertex(-half.x, half.y, half.z)    // 7
        ).map { Matrix3x3.rotation(angle) * it + position }

        val faces = listOf(
            listOf(0, 1, 2, 3), // Back
            listOf(4, 5, 6, 7), // Front
            listOf(0, 4, 7, 3), // Left
            listOf(1, 5, 6, 2), // Right
            listOf(3, 2, 6, 7), // Top
            listOf(0, 1, 5, 4)  // Bottom
        )

        val faceNormals = listOf(
            Vertex(0f, 0f, -1f), // Back
            Vertex(0f, 0f, 1f),  // Front
            Vertex(-1f, 0f, 0f), // Left
            Vertex(1f, 0f, 0f),  // Right
            Vertex(0f, 1f, 0f),  // Top
            Vertex(0f, -1f, 0f)  // Bottom
        ).map { Matrix3x3.rotation(angle) * it }

        val cameraDir = camera.position - position

        val faceColors = listOf(
            Color(0xFF9E9E9E), // Back - gris
            Color(0xFF2196F3), // Front - azul
            Color(0xFFF44336), // Left - rojo
            Color(0xFF4CAF50), // Right - verde
            Color(0xFFFFC107), // Top - amarillo
            Color(0xFF9C27B0)  // Bottom - morado
        )

        val sortedFaces = faces.indices.map { i ->
            val face = faces[i]
            val centerTest = faceCenter(face, vertices)
            val distance = distanceToCamera(centerTest)
            Triple(i, face, distance)
        }.sortedBy { it.third } // más lejos primero

        sortedFaces.forEach{ (i, face, _) ->
            val normal = faceNormals[i]
            val faceCenter = faceCenter(face, vertices)
            val toCamera = camera.position - faceCenter

            val dot = normal dot toCamera
            if (dot > 0f) {
                val projected = face.mapNotNull { project(vertices[it]) }
                if (projected.size == 4) {
                    val path = Path().apply {
                        moveTo(projected[0].x, projected[0].y)
                        lineTo(projected[1].x, projected[1].y)
                        lineTo(projected[2].x, projected[2].y)
                        lineTo(projected[3].x, projected[3].y)
                        close()
                    }

                    drawScope.drawPath(
                        path = path,
                        color = faceColors[i],
                        style = Fill
                    )

                    // Bordes
                    for (j in 0 until 4) {
                        val p1 = projected[j]
                        val p2 = projected[(j + 1) % 4]
                        drawScope.drawLine(
                            color = Color.Black,
                            start = p1,
                            end = p2,
                            strokeWidth = 3f
                        )
                    }
                }
            }
        }
    }
    * fun faceCenter(face: List<Int>, vertices: List<Vertex>): Vertex {
        val sum = face.map { vertices[it] }.reduce { acc, v -> acc + v }
        return sum / face.size.toFloat()
    }
    * private fun adjustPoint(point: Offset, other: Offset, min: Offset, max: Offset): Offset {
        var adjusted = point
        if (point.x <= min.x) {
            adjusted = adjusted.copy(
                x = min.x,
                y = point.y + (min.x - point.x) * (other.y - point.y) / (other.x - point.x)
            )
        } else if (point.x >= max.x) {
            adjusted = adjusted.copy(
                x = max.x,
                y = point.y + (max.x - point.x) * (other.y - point.y) / (other.x - point.x)
            )
        }
        if (point.y <= min.y) {
            adjusted = adjusted.copy(
                y = min.y,
                x = point.x + (min.y - point.y) * (other.x - point.x) / (other.y - point.y)
            )
        } else if (point.y >= max.y) {
            adjusted = adjusted.copy(
                y = max.y,
                x = point.x + (max.y - point.y) * (other.x - point.x) / (other.y - point.y)
            )
        }
        return adjusted
    }
    * override fun drawRelativeTriangle(
        obj: SceneObject,
        localA: Vertex,
        localB: Vertex,
        localC: Vertex,
        color: Color
    ) {
        val modelMatrix = Matrix3x3.rotation(obj.rotation)
        val a = modelMatrix * (localA * obj.scale) + obj.position
        val b = modelMatrix * (localB * obj.scale) + obj.position
        val c = modelMatrix * (localC * obj.scale) + obj.position

        val normal = computeNormal(a, b, c)
        val cameraDir = (camera.position - ((a + b + c) / 3f)).normalize()

        if (normal dot cameraDir < 0f) return

        val projected = listOfNotNull(project(a), project(b), project(c))
        if (projected.size == 3) {
            val path = Path().apply {
                moveTo(projected[0].x, projected[0].y)
                lineTo(projected[1].x, projected[1].y)
                lineTo(projected[2].x, projected[2].y)
                close()
            }

            drawScope.drawPath(
                path = path,
                color = color,
                style = Fill
            )

            // Opcional: dibujar bordes
            drawScope.drawLine(Color.Black, projected[0], projected[1], 2f)
            drawScope.drawLine(Color.Black, projected[1], projected[2], 2f)
            drawScope.drawLine(Color.Black, projected[2], projected[0], 2f)
        }
    }
    * override fun drawScene(scene: Scene) {
        //Painters algorithm
        //val sorted = scene.objects.sortedBy {
        //    val dx = it.position.x - camera.position.x
        //    val dy = it.position.y - camera.position.y
        //    val dz = it.position.z - camera.position.z
        //    dx * dx + dy * dy + dz * dz // distancia al cuadrado
        //}.reversed() // más lejos primero
//
        //for (obj in sorted) {
        //    obj.draw(this, obj)
        //}

    }
    *
    * interface DrawScope3D : DrawScope {

    fun drawRelativeTriangleZBuffered(
        obj: SceneObject,
        localA: Vertex,
        localB: Vertex,
        localC: Vertex,
        color: Color
    )

    fun drawCubeObject(obj: SceneObject)

    fun drawScene(scene: Scene)
}

class DrawScope3DImpl(
    private val camera: Camera,
    private val drawScope: DrawScope
) : DrawScope by drawScope, DrawScope3D {

    private val triangleZBuffer = mutableListOf<Pair<Float, () -> Unit>>()

    override fun drawScene(scene: Scene) {
        triangleZBuffer.clear()

        for (obj in scene.objects) {
            obj.draw(this, obj)
        }

        flushZBuffer()
    }

    override fun drawCubeObject(obj: SceneObject) {
        val s = 0.5f

        val v = listOf(
            Vertex(-s, -s, -s),
            Vertex(s, -s, -s),
            Vertex(s, s, -s),
            Vertex(-s, s, -s),
            Vertex(-s, -s, s),
            Vertex(s, -s, s),
            Vertex(s, s, s),
            Vertex(-s, s, s)
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

    override fun drawRelativeTriangleZBuffered(
        obj: SceneObject,
        localA: Vertex,
        localB: Vertex,
        localC: Vertex,
        color: Color
    ) {
        val modelMatrix = Matrix3x3.rotation(obj.rotation)
        val a = modelMatrix * (localA * obj.scale) + obj.position
        val b = modelMatrix * (localB * obj.scale) + obj.position
        val c = modelMatrix * (localC * obj.scale) + obj.position

        val normal = computeNormal(a, b, c)
        val cameraDir = (camera.position - ((a + b + c) / 3f)).normalize()
        if (normal dot cameraDir < 0f) return // backface culling

        val projected = listOfNotNull(project(a), project(b), project(c))
        if (projected.size == 3) {
            val viewA = distanceToCamera(a)
            val viewB = distanceToCamera(b)
            val viewC = distanceToCamera(c)
            val zAvg = (viewA + viewB + viewC) / 3f

            triangleZBuffer.add(zAvg to {
                val path = Path().apply {
                    moveTo(projected[0].x, projected[0].y)
                    lineTo(projected[1].x, projected[1].y)
                    lineTo(projected[2].x, projected[2].y)
                    close()
                }

                drawScope.drawPath(path, color, style = Fill)
                drawScope.drawLine(Color.Black, projected[0], projected[1], 2f)
                drawScope.drawLine(Color.Black, projected[1], projected[2], 2f)
                drawScope.drawLine(Color.Black, projected[2], projected[0], 2f)
            })
        }
    }

    private fun flushZBuffer() {
        triangleZBuffer
            .sortedByDescending { it.first }
            .forEach { it.second() }

        triangleZBuffer.clear()
    }

    private fun project(vertex: Vertex): Offset? {
        val x = vertex.x - camera.position.x
        val y = vertex.y - camera.position.y
        val z = vertex.z - camera.position.z

        val cosX = kotlin.math.cos(camera.angle.y)
        val sinX = kotlin.math.sin(camera.angle.y)
        val cosY = kotlin.math.cos(camera.angle.x)
        val sinY = kotlin.math.sin(camera.angle.x)
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

    private fun computeNormal(p1: Vertex, p2: Vertex, p3: Vertex): Vertex {
        val u = p2 - p1
        val v = p3 - p1
        return v.cross(u).normalize()
    }

    private fun distanceToCamera(v: Vertex): Float {
        val dx = v.x - camera.position.x
        val dy = v.y - camera.position.y
        val dz = v.z - camera.position.z
        return dx * dx + dy * dy + dz * dz // no hace falta sqrt
    }

    private fun Vertex.normalize(): Vertex {
        val length = sqrt(x * x + y * y + z * z)
        return if (length != 0f) {
            Vertex(x / length, y / length, z / length)
        } else {
            Vertex(0f, 0f, 0f)
        }
    }

    private infix fun Vertex.cross(other: Vertex): Vertex {
        return Vertex(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        )
    }
}*/