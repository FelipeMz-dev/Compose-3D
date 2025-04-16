package com.felipemz.a3d_app.utils

import androidx.compose.ui.geometry.Offset
import com.felipemz.a3d_app.model.Camera
import com.felipemz.a3d_app.model.Vertex
import kotlin.math.cos
import kotlin.math.sin

class ProjectionUtils(private val camera: Camera) {

    /**
     * Projects a 3D vertex onto a 2D plane using the camera's position and angle.
     *
     * @param vertex The 3D vertex to project.
     * @return The projected 2D offset, or null if the vertex is behind the camera.
     */

    fun project(vertex: Vertex): Offset? {
        val d = vertex - camera.position

        val (cosX, sinX) = cos(camera.angle.x) to sin(camera.angle.x)
        val (cosY, sinY) = cos(camera.angle.y) to sin(camera.angle.y)
        val (cosZ, sinZ) = cos(camera.angle.z) to sin(camera.angle.z)

        val pointerX = cosY * (sinZ * d.y + cosZ * d.x) - sinY * d.z
        val pointerY = sinX * (cosY * d.z + sinY * (sinZ * d.y + cosZ * d.x)) + cosX * (cosZ * d.y - sinZ * d.x)
        val pointerZ = cosX * (cosY * d.z + sinY * (sinZ * d.y + cosZ * d.x)) - sinX * (cosZ * d.y - sinZ * d.x)

        return if (pointerZ > 0) {
            Offset(
                x = (pointerX * camera.focalLength / pointerZ) + camera.screenSize.width / 2,
                y = (pointerY * camera.focalLength / pointerZ) + camera.screenSize.height / 2
            )
        } else null
    }

    fun distanceToProjectionZ(vertex: Vertex): Float {
        val d = vertex - camera.position

        val (cosX, sinX) = cos(camera.angle.x) to sin(camera.angle.x)
        val (cosY, sinY) = cos(camera.angle.y) to sin(camera.angle.y)
        val (cosZ, sinZ) = cos(camera.angle.z) to sin(camera.angle.z)

        return cosX * (cosY * d.z + sinY * (sinZ * d.y + cosZ * d.x)) - sinX * (cosZ * d.y - sinZ * d.x)
    }

    fun computeZIndex(
        a: Vertex,
        b: Vertex,
        c: Vertex
    ) = (distanceToProjectionZ(a) + distanceToProjectionZ(b) + distanceToProjectionZ(c)) / 3f
}