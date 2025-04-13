package com.felipemz.a3d_app.model

import androidx.compose.ui.geometry.Size
import kotlin.math.cos
import kotlin.math.sin

data class Camera(
    var position: Vertex = Vertex(0f, -5f, -5f),
    var angle: Vertex = Vertex(0f, 0f, 0f),
    var focalLength: Float = 500f,
    var screenSize: Size = Size(0f, 0f)
) {

    fun rotate(angle: Vertex): Camera {
        val newCam = this.copy(
            angle = this.angle.copy(
                x = (this.angle.x + angle.x).coerceIn(-1.57f, 1.57f),
                y = this.angle.y + angle.y,
                z = this.angle.z + angle.z
            )
        )
        return newCam
    }

    fun move(vertex: Vertex): Camera {
        val newCam = this.copy(
            position = position.copy(
                x = this.position.x + (sin(this.angle.y.toDouble()).toFloat() * vertex.z) + (cos(this.angle.y.toDouble())
                    .toFloat() * vertex.x),
                y = this.position.y + vertex.y,
                z = this.position.z + (cos(this.angle.y.toDouble()).toFloat() * vertex.z) - (sin(this.angle.y.toDouble())
                    .toFloat() * vertex.x)
            )
        )
        return newCam
    }

    fun moveTo(vertex: Vertex) {
        this.position = this.position.copy(
            x = this.position.x + vertex.x,
            y = this.position.y + vertex.y,
            z = this.position.z + vertex.z
        )
    }

    fun lookAt(vertex: Vertex) {
        this.angle = Vertex(
            x = vertex.x - this.position.x,
            y = vertex.y - this.position.y,
            z = vertex.z - this.position.z
        )
    }
}