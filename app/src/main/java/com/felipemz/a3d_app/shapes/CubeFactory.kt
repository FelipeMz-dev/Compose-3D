package com.felipemz.a3d_app.shapes

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.felipemz.a3d_app.model.Vertex

object CubeFactory {
    fun createVertices(size: Float): List<Vertex> {
        return listOf(
            Vertex(-size, -size, -size),
            Vertex(size, -size, -size),
            Vertex(size, size, -size),
            Vertex(-size, size, -size),
            Vertex(-size, -size, size),
            Vertex(size, -size, size),
            Vertex(size, size, size),
            Vertex(-size, size, size)
        )
    }

    fun createTriangles(vertices: List<Vertex>): List<Triple<Vertex, Vertex, Vertex>> {
        return listOf(
            Triple(vertices[0], vertices[1], vertices[2]), Triple(vertices[0], vertices[2], vertices[3]),
            Triple(vertices[4], vertices[6], vertices[5]), Triple(vertices[4], vertices[7], vertices[6]),
            Triple(vertices[0], vertices[3], vertices[7]), Triple(vertices[0], vertices[7], vertices[4]),
            Triple(vertices[1], vertices[5], vertices[6]), Triple(vertices[1], vertices[6], vertices[2]),
            Triple(vertices[3], vertices[2], vertices[6]), Triple(vertices[3], vertices[6], vertices[7]),
            Triple(vertices[0], vertices[4], vertices[5]), Triple(vertices[0], vertices[5], vertices[1])
        )
    }

    fun createTextures(
        trianglePosition: Int,
        sizeImage: Size
    ): List<Offset> {
        val w = sizeImage.width
        val h = sizeImage.height
        return when (trianglePosition) {
            0 -> listOf(Offset(0f, 0f), Offset(w, 0f), Offset(w, h))
            1 -> listOf(Offset(0f, 0f), Offset(w, h), Offset(0f, h))
            2 -> listOf(Offset(0f, h), Offset(w, 0f), Offset(w, h))
            3 -> listOf(Offset(0f, h), Offset(0f, 0f), Offset(w, 0f))
            4 -> listOf(Offset(w, 0f), Offset(w, h), Offset(0f, h))
            5 -> listOf(Offset(w, 0f), Offset(0f, h), Offset(0f, 0f))
            6 -> listOf(Offset(0f, h), Offset(0f, 0f), Offset(w, 0f))
            7 -> listOf(Offset(0f, h), Offset(w, 0f), Offset(w, h))
            8 -> listOf(Offset(0f, 0f), Offset(w, 0f), Offset(w, h))
            9 -> listOf(Offset(0f, 0f), Offset(w, h), Offset(0f, h))
            10 -> listOf(Offset(0f, 0f), Offset(w, 0f), Offset(w, h))
            11 -> listOf(Offset(0f, 0f), Offset(w, h), Offset(0f, h))
            else -> listOf(Offset.Zero, Offset.Zero, Offset.Zero)
        }
    }
}