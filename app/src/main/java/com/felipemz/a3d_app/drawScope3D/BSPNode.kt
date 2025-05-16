package com.felipemz.a3d_app.drawScope3D

import com.felipemz.a3d_app.drawScope3D.BSP.insertTriangle
import com.felipemz.a3d_app.model.Triangle3D

data class BSPNode(
    val splitter: Triangle3D,
    var front: BSPNode? = null,
    var back: BSPNode? = null,
)

fun BSPNode?.add(triangle: Triangle3D): BSPNode {
    return this?.let { insertTriangle(this, triangle) } ?: BSPNode(triangle)
}