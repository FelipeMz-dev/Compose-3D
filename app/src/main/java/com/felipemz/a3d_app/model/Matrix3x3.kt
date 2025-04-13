package com.felipemz.a3d_app.model

data class Matrix3x3(val m: Array<FloatArray>) {

    operator fun times(v: Vertex): Vertex {
        val x = m[0][0] * v.x + m[0][1] * v.y + m[0][2] * v.z
        val y = m[1][0] * v.x + m[1][1] * v.y + m[1][2] * v.z
        val z = m[2][0] * v.x + m[2][1] * v.y + m[2][2] * v.z
        return Vertex(x, y, z)
    }

    operator fun times(other: Matrix3x3): Matrix3x3 {
        val result = Array(3) { FloatArray(3) }
        for (i in 0..2) {
            for (j in 0..2) {
                result[i][j] = (0..2).sumOf { k -> m[i][k] * other.m[k][j] }
            }
        }
        return Matrix3x3(result)
    }

    companion object {
        fun rotationX(angle: Float): Matrix3x3 {
            val cos = kotlin.math.cos(angle)
            val sin = kotlin.math.sin(angle)
            return Matrix3x3(
                arrayOf(
                    floatArrayOf(1f, 0f, 0f),
                    floatArrayOf(0f, cos, -sin),
                    floatArrayOf(0f, sin, cos)
                )
            )
        }

        fun rotationY(angle: Float): Matrix3x3 {
            val cos = kotlin.math.cos(angle)
            val sin = kotlin.math.sin(angle)
            return Matrix3x3(
                arrayOf(
                    floatArrayOf(cos, 0f, sin),
                    floatArrayOf(0f, 1f, 0f),
                    floatArrayOf(-sin, 0f, cos)
                )
            )
        }

        fun rotationZ(angle: Float): Matrix3x3 {
            val cos = kotlin.math.cos(angle)
            val sin = kotlin.math.sin(angle)
            return Matrix3x3(
                arrayOf(
                    floatArrayOf(cos, -sin, 0f),
                    floatArrayOf(sin, cos, 0f),
                    floatArrayOf(0f, 0f, 1f)
                )
            )
        }

        fun rotation(angle: Vertex): Matrix3x3 {
            // Aplica rotaciones en orden Z * Y * X
            return rotationZ(angle.z) * rotationY(angle.y) * rotationX(angle.x)
        }
    }
}

private fun IntRange.sumOf(sum: (Int) -> Float): Float{
    var total = 0f
    for (i in this) {
        total += sum(i)
    }
    return total
}