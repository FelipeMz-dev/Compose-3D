package com.felipemz.a3d_app.utils

import com.felipemz.a3d_app.model.Camera
import com.felipemz.a3d_app.model.Vertex

object MathUtils {
    fun computeNormal(p1: Vertex, p2: Vertex, p3: Vertex): Vertex {
        val u = p2 - p1
        val v = p3 - p1
        return v.cross(u).normalize()
    }

    fun computeDirection(cameraPosition: Vertex, a: Vertex, b: Vertex, c: Vertex): Vertex {
        return (cameraPosition - ((a + b + c) / 3f)).normalize()
    }

    fun scaleByCentroid(a: Vertex, b: Vertex, c: Vertex, scaleFactor: Float): Triple<Vertex, Vertex, Vertex> {
        val centroid = (a + b + c) / 3f
        return Triple(
            centroid + (a - centroid) * scaleFactor,
            centroid + (b - centroid) * scaleFactor,
            centroid + (c - centroid) * scaleFactor
        )
    }
}