package com.felipemz.a3d_app

import com.felipemz.a3d_app.model.Triangle3D

class ZBuffer {
    private val buffer = mutableListOf<Triangle3D>()

    fun add(triangle: Triangle3D) {
        buffer.add(triangle)
    }

    fun addAll(triangles: List<Triangle3D>) {
        buffer.addAll(triangles)
    }

    fun remove(triangle: Triangle3D) {
        buffer.remove(triangle)
    }

    fun clear() {
        buffer.clear()
    }

    fun get(): List<Triangle3D> {
        return buffer
    }

    fun getSorted(): List<Triangle3D> {
        return buffer.sortedByDescending { it.zIndex }
    }
}