package com.felipemz.a3d_app.drawScope3D

import com.felipemz.a3d_app.model.Vertex
import com.felipemz.a3d_app.utils.MathUtils.computeNormal
import kotlin.math.absoluteValue

object SegmentsIntersection {
    fun triangleEdgeIntersectsTriangle(
        a: List<Vertex>,
        b: List<Vertex>
    ): Boolean {
        val planePoint = b[0]
        val planeNormal = computeNormal(b[0], b[1], b[2])

        for (i in 0..2) {
            val start = a[i]
            val end = a[(i + 1) % 3]
            val dir = end - start

            val denom = dir.dot(planeNormal)
            if (denom.absoluteValue < EPSILON) continue

            val t = (planePoint - start).dot(planeNormal) / denom
            if (t < 0f || t > 1f) continue

            val intersection = start + dir * t
            if (pointInTriangle(intersection, b[0], b[1], b[2])) return true
        }

        return false
    }

    private fun pointInTriangle(
        p: Vertex,
        a: Vertex,
        b: Vertex,
        c: Vertex
    ): Boolean {
        val v0 = c - a
        val v1 = b - a
        val v2 = p - a

        val dot00 = v0 dot v0
        val dot01 = v0 dot v1
        val dot02 = v0 dot v2
        val dot11 = v1 dot v1
        val dot12 = v1 dot v2

        val denom = dot00 * dot11 - dot01 * dot01
        if (denom == 0f) return false

        val u = (dot11 * dot02 - dot01 * dot12) / denom
        val v = (dot00 * dot12 - dot01 * dot02) / denom
        return u >= -EPSILON && v >= -EPSILON && u + v <= 1.0001
    }
}