package com.felipemz.a3d_app.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import com.felipemz.a3d_app.drawScope3D.EPSILON

data class Triangle3D(
    val polygon: Polygon,
    val zIndex: Float = 0f,
    val color: Color = Color.Unspecified,
    val imageBitmap: ImageBitmap? = null,
    val owner: SceneObject? = null,
    val textureVertices: List<Offset>? = null
)

typealias Polygon = Triple<Vertex, Vertex, Vertex>

fun Polygon.isValid(): Boolean {
        val valid1 = second - first
        val valid2 = third - first
        val cross = valid1 cross valid2
        return cross.lengthSquared() > EPSILON
}

fun Triangle3D.toVertexUVList(): List<VertexUV>? {
    val uv = textureVertices ?: return null
    return listOf(
        VertexUV(polygon.first, uv[0]),
        VertexUV(polygon.second, uv[1]),
        VertexUV(polygon.third, uv[2])
    )
}
