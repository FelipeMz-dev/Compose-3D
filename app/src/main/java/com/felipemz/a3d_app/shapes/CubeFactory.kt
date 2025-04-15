package com.felipemz.a3d_app.shapes

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
}