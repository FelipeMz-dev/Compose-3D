package com.felipemz.a3d_app.drawScope3D

import com.felipemz.a3d_app.model.Vertex

object TriangleIntersect {

    const val EPSILON = 1e-5f

    fun intersect(
        a1: Vertex,
        b1: Vertex,
        c1: Vertex,
        a2: Vertex,
        b2: Vertex,
        c2: Vertex
    ): Boolean {
        val n1 = (b1 - a1).cross(c1 - a1)
        val d1 = -(n1 dot a1)

        val distA2 = (n1 dot a2) + d1
        val distB2 = (n1 dot b2) + d1
        val distC2 = (n1 dot c2) + d1

        val side1 = (distA2 > EPSILON) || (distB2 > EPSILON) || (distC2 > EPSILON)
        val side2 = (distA2 < -EPSILON) || (distB2 < -EPSILON) || (distC2 < -EPSILON)


        if (!side1 || !side2) return false

        val n2 = (b2 - a2).cross(c2 - a2)
        val d2 = -(n2 dot a2)

        val distA1 = (n2 dot a1) + d2
        val distB1 = (n2 dot b1) + d2
        val distC1 = (n2 dot c1) + d2

        val side3 = (distA1 > 0f) || (distB1 > 0f) || (distC1 > 0f)
        val side4 = (distA1 < 0f) || (distB1 < 0f) || (distC1 < 0f)

        return !(!side3 || !side4)
    }
}