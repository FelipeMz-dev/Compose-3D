package com.felipemz.a3d_app.drawScope3D

import com.felipemz.a3d_app.model.Polygon
import com.felipemz.a3d_app.model.Triangle3D
import com.felipemz.a3d_app.model.Vertex

class AABB(
    private val min: Vertex,
    private val max: Vertex,
) {
    constructor(polygon: Polygon) : this(
        Vertex(
            minOf(polygon.first.x, polygon.second.x, polygon.third.x),
            minOf(polygon.first.y, polygon.second.y, polygon.third.y),
            minOf(polygon.first.z, polygon.second.z, polygon.third.z)
        ),
        Vertex(
            maxOf(polygon.first.x, polygon.second.x, polygon.third.x),
            maxOf(polygon.first.y, polygon.second.y, polygon.third.y),
            maxOf(polygon.first.z, polygon.second.z, polygon.third.z)
        )
    )

    fun intersects(other: AABB): Boolean {
        return !(max.x < other.min.x || min.x > other.max.x ||
                max.y < other.min.y || min.y > other.max.y ||
                max.z < other.min.z || min.z > other.max.z)
    }
}