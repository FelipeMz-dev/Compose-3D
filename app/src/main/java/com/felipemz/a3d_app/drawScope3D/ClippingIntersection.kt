package com.felipemz.a3d_app.drawScope3D

import com.felipemz.a3d_app.model.Vertex
import com.felipemz.a3d_app.utils.MathUtils.computeNormal

object ClippingIntersection {
    fun intersect(
        a1: Vertex,
        b1: Vertex,
        c1: Vertex,
        a2: Vertex,
        b2: Vertex,
        c2: Vertex
    ): Boolean {
        val n1 = computeNormal(a1, b1, c1)
        val d1 = -(n1 dot a1)

        val distA2 = (n1 dot a2) + d1
        val distB2 = (n1 dot b2) + d1
        val distC2 = (n1 dot c2) + d1

        val side1 = (distA2 > 0f) || (distB2 > 0f) || (distC2 > 0f)
        val side2 = (distA2 < 0f) || (distB2 < 0f) || (distC2 < 0f)

        if (!side1 || !side2) return false

        val n2 = computeNormal(a2, b2, c2)
        val d2 = -(n2 dot a2)

        val distA1 = (n2 dot a1) + d2
        val distB1 = (n2 dot b1) + d2
        val distC1 = (n2 dot c1) + d2

        val side3 = (distA1 > 0f) || (distB1 > 0f) || (distC1 > 0f)
        val side4 = (distA1 < 0f) || (distB1 < 0f) || (distC1 < 0f)

        return !(!side3 || !side4)
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
            val c = clipper[(i + 2) % clipper.size]
            val rawNormal = computeNormal(a, b, c)

            val normal = if (invert) -rawNormal else rawNormal

            result = clipPolygonByPlane(result, a, normal)
            if (result.isEmpty()) break
        }

        return triangulate(result)
    }

    private fun triangulate(poly: List<Vertex>): List<List<Vertex>> {
        if (poly.size < 3) return emptyList()
        val triangles = mutableListOf<List<Vertex>>()
        for (i in 1 until poly.size - 1) {
            triangles.add(listOf(poly[0], poly[i], poly[i + 1]))
        }
        return triangles
    }

    private fun clipPolygonByPlane(
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

    fun clipLine(start: Vertex, end: Vertex, startZ: Float, endZ: Float): Pair<Vertex, Vertex> {
        val zNear = 0.01f
        if (startZ > zNear && endZ > zNear) return start to end
        val t = (zNear - startZ) / (endZ - startZ)
        val intersection = start + (end - start) * t
        return if (startZ < zNear) (intersection to end) else (start to intersection)
    }
}