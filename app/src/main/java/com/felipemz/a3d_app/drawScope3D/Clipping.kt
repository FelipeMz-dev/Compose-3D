package com.felipemz.a3d_app.drawScope3D

import androidx.compose.ui.geometry.Offset
import com.felipemz.a3d_app.model.Polygon
import com.felipemz.a3d_app.model.Triangle3D
import com.felipemz.a3d_app.model.Vertex
import com.felipemz.a3d_app.model.VertexUV
import com.felipemz.a3d_app.model.isValid

object Clipping {

    fun splitTriangle(
        triangle: Triangle3D,
        planePoint: Vertex,
        planeNormal: Vertex
    ): Pair<List<Triangle3D>, List<Triangle3D>> {
        val poly = listOf(
            VertexUV(triangle.polygon.first, triangle.textureVertices?.getOrNull(0)?: Offset.Zero),
            VertexUV(triangle.polygon.second, triangle.textureVertices?.getOrNull(1)?: Offset.Zero),
            VertexUV(triangle.polygon.third, triangle.textureVertices?.getOrNull(2)?: Offset.Zero)
        )

        val frontClipped = clipPolygonByPlane(poly, planePoint, planeNormal, keepPositive = true)
        val backClipped = clipPolygonByPlane(poly, planePoint, planeNormal, keepPositive = false)

        fun toTriangles(vertices: List<VertexUV>): List<Triangle3D> {
            if (vertices.size < 3) return emptyList()
            val result = mutableListOf<Triangle3D>()
            for (i in 1 until vertices.size - 1) {
                val a = vertices[0]
                val b = vertices[i]
                val c = vertices[i + 1]
                if (Polygon(a.position, b.position, c.position).isValid()) {
                    result.add(
                        Triangle3D(
                            polygon = Polygon(a.position, b.position, c.position),
                            zIndex = 0f,
                            color = triangle.color,
                            imageBitmap = triangle.imageBitmap,
                            owner = triangle.owner,
                            textureVertices = listOf(a.uv, b.uv, c.uv)
                        )
                    )
                }
            }
            return result
        }

        return toTriangles(frontClipped) to toTriangles(backClipped)
    }

    private fun clipPolygonByPlane(
        polygon: List<VertexUV>,
        planePoint: Vertex,
        planeNormal: Vertex,
        keepPositive: Boolean
    ): List<VertexUV> {
        if (polygon.size < 3) return emptyList()

        val output = mutableListOf<VertexUV>()

        fun distance(v: Vertex): Float = (v - planePoint).dot(planeNormal)

        for (i in polygon.indices) {
            val current = polygon[i]
            val next = polygon[(i + 1) % polygon.size]
            val dCurrent = distance(current.position)
            val dNext = distance(next.position)

            val insideCurrent = if (keepPositive) dCurrent >= 0f else dCurrent <= 0f
            val insideNext = if (keepPositive) dNext >= 0f else dNext <= 0f

            if (insideCurrent && insideNext) {
                output.add(next)
            } else if (insideCurrent != insideNext) {
                val t = dCurrent / (dCurrent - dNext)
                val pos = current.position + (next.position - current.position) * t
                val uv = current.uv + (next.uv - current.uv) * t
                output.add(VertexUV(pos, uv))
                if (insideNext) output.add(next)
            }
        }

        return output
    }

    fun clipLine(start: Vertex, end: Vertex, startZ: Float, endZ: Float): Pair<Vertex, Vertex> {
        val zNear = 0.01f
        if (startZ > zNear && endZ > zNear) return start to end
        val t = (zNear - startZ) / (endZ - startZ)
        val intersection = start + (end - start) * t
        return if (startZ < zNear) (intersection to end) else (start to intersection)
    }
}