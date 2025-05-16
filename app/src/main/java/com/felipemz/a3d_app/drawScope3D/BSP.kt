package com.felipemz.a3d_app.drawScope3D

import com.felipemz.a3d_app.drawScope3D.SegmentsIntersection.triangleEdgeIntersectsTriangle
import com.felipemz.a3d_app.model.Polygon
import com.felipemz.a3d_app.model.Triangle3D
import com.felipemz.a3d_app.model.Vertex
import com.felipemz.a3d_app.utils.MathUtils.computeNormal

const val EPSILON = 1e-4f

object BSP {

    fun insertTriangle(
        node: BSPNode?,
        triangle: Triangle3D
    ): BSPNode {
        if (node == null) return BSPNode(triangle)

        val planeNormal = computeNormal(
            p1 = node.splitter.polygon.first,
            p2 = node.splitter.polygon.second,
            p3 = node.splitter.polygon.third
        )
        val planePoint = node.splitter.polygon.first

        val vertices = triangle.polygon.toList()
        val distances = vertices.map { (it - planePoint).dot(planeNormal) }


        val inFront = distances.any { it > EPSILON }
        val behind = distances.any { it < -EPSILON }

        return when {
            !behind -> {
                node.front = insertTriangle(node.front, triangle)
                node
            }
            !inFront -> {
                node.back = insertTriangle(node.back, triangle)
                node
            }
            else -> {
                if (trianglesMayIntersect(triangle.polygon, node.splitter.polygon)) {
                    val (frontPart, backPart) = Clipping.splitTriangle(triangle, planePoint, planeNormal)
                    frontPart.forEach { node.front = insertTriangle(node.front, it) }
                    backPart.forEach { node.back = insertTriangle(node.back, it) }
                } else {
                    if (triangle.zIndex < node.splitter.zIndex) {
                        node.front = insertTriangle(node.front, triangle)
                    } else {
                        node.back = insertTriangle(node.back, triangle)
                    }
                }
                node
            }
        }
    }

    private fun trianglesMayIntersect(
        polygon1: Polygon,
        polygon2: Polygon
    ): Boolean {
        if (AABB(polygon1).intersects(AABB(polygon2))) return true
        val vertices1 = polygon1.toList()
        val vertices2 = polygon2.toList()
        if (triangleEdgeIntersectsTriangle(vertices1, vertices2)) return true
        if (triangleEdgeIntersectsTriangle(vertices2, vertices1)) return true
        return false
    }

    fun drawBSP(
        node: BSPNode?,
        cameraPos: Vertex,
        drawTriangle: (Triangle3D) -> Unit
    ) {
        if (node == null) return

        val normal = computeNormal(
            node.splitter.polygon.first,
            node.splitter.polygon.second,
            node.splitter.polygon.third
        )
        val side = (cameraPos - node.splitter.polygon.first).dot(normal)

        if (side >= 0) {
            drawBSP(node.back, cameraPos, drawTriangle)
            drawTriangle(node.splitter)
            drawBSP(node.front, cameraPos, drawTriangle)
        } else {
            drawBSP(node.front, cameraPos, drawTriangle)
            drawTriangle(node.splitter)
            drawBSP(node.back, cameraPos, drawTriangle)
        }
    }
}