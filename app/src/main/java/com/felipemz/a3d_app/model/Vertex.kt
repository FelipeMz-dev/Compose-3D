package com.felipemz.a3d_app.model

import androidx.compose.ui.geometry.Offset
import kotlin.math.sqrt

data class Vertex(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f
) {
    operator fun plus(other: Vertex): Vertex {
        return Vertex(
            x = this.x + other.x,
            y = this.y + other.y,
            z = this.z + other.z
        )
    }

    operator fun minus(other: Vertex): Vertex {
        return Vertex(
            x = this.x - other.x,
            y = this.y - other.y,
            z = this.z - other.z
        )
    }

    operator fun plus(other: Float): Vertex {
        return Vertex(
            x = this.x + other,
            y = this.y + other,
            z = this.z + other
        )
    }

    operator fun times(other: Float): Vertex {
        return Vertex(
            x = this.x * other,
            y = this.y * other,
            z = this.z * other
        )
    }

    operator fun times(other: Vertex): Vertex {
        return Vertex(
            x = this.x * other.x,
            y = this.y * other.y,
            z = this.z * other.z
        )
    }

    operator fun div(other: Float): Vertex {
        return Vertex(
            x = this.x / other,
            y = this.y / other,
            z = this.z / other
        )
    }

    infix fun dot(other: Vertex): Float =
        x * other.x + y * other.y + z * other.z

    infix fun cross(other: Vertex): Vertex {
        return Vertex(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        )
    }

    operator fun unaryMinus(): Vertex {
        return Vertex(-x, -y, -z)
    }

    fun normalize(): Vertex {
        val length = sqrt(x * x + y * y + z * z)
        return if (length != 0f) Vertex(x / length, y / length, z / length) else this
    }

    fun length(): Float = sqrt(x * x + y * y + z * z)

    fun lengthSquared(): Float = x * x + y * y + z * z

    fun distanceTo(other: Vertex): Float {
        return (this - other).length()
    }

    fun midpoint(other: Vertex): Vertex {
        return (this + other) / 2f
    }
}

data class VertexUV(val position: Vertex, val uv: Offset)
