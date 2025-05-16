package com.felipemz.a3d_app.shapes

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.felipemz.a3d_app.model.Polygon
import com.felipemz.a3d_app.model.Triangle3D
import com.felipemz.a3d_app.model.Vertex

object SphereFactory {
    fun createSphere(
        radius: Float,
        latitudeBands: Int = 16,
        longitudeBands: Int = 16,
        center: Vertex = Vertex(0f, 0f, 0f),
        imageSize: Size? = null,
    ): List<Triangle3D> {
        val triangles = mutableListOf<Triangle3D>()

        for (lat in 0 until latitudeBands) {
            val theta1 = Math.PI * lat / latitudeBands
            val theta2 = Math.PI * (lat + 1) / latitudeBands

            for (lon in 0 until longitudeBands) {
                val phi1 = 2 * Math.PI * lon / longitudeBands
                val phi2 = 2 * Math.PI * (lon + 1) / longitudeBands

                val p1 = sphereVertex(theta1, phi1, radius, center)
                val p2 = sphereVertex(theta2, phi1, radius, center)
                val p3 = sphereVertex(theta2, phi2, radius, center)
                val p4 = sphereVertex(theta1, phi2, radius, center)

                val uv1 = sphereUV(theta1, phi1, imageSize)
                val uv2 = sphereUV(theta2, phi1, imageSize)
                val uv3 = sphereUV(theta2, phi2, imageSize)
                val uv4 = sphereUV(theta1, phi2, imageSize)

                // Dos triángulos por cuadrilátero
                triangles.add(
                    Triangle3D(
                        polygon = Polygon(p1, p2, p3),
                        color = Color.Unspecified,
                        zIndex = 0f,
                        imageBitmap = null,
                        textureVertices = listOf(uv1, uv2, uv3)
                    )
                )
                triangles.add(
                    Triangle3D(
                        polygon = Polygon(p1, p3, p4),
                        color = Color.Unspecified,
                        zIndex = 0f,
                        imageBitmap = null,
                        textureVertices = listOf(uv1, uv3, uv4)
                    )
                )
            }
        }

        return triangles
    }

    private fun sphereVertex(theta: Double, phi: Double, r: Float, c: Vertex): Vertex {
        val x = (r * Math.sin(theta) * Math.cos(phi)).toFloat() + c.x
        val y = (r * Math.cos(theta)).toFloat() + c.y
        val z = (r * Math.sin(theta) * Math.sin(phi)).toFloat() + c.z
        return Vertex(x, y, z)
    }

    private fun sphereUV(theta: Double, phi: Double, size: Size?): Offset {
        val u = phi / (2 * Math.PI)
        val v = theta / Math.PI
        return if (size != null) {
            Offset((u * size.width).toFloat(), (v * size.height).toFloat())
        } else {
            Offset(u.toFloat(), v.toFloat())
        }
    }
}