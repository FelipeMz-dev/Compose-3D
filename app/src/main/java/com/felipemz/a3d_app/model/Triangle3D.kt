package com.felipemz.a3d_app.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap

data class Triangle3D(
    val a: Vertex,
    val b: Vertex,
    val c: Vertex,
    val zIndex: Float = 0f,
    val color: Color = Color.Unspecified,
    val imageBitmap: ImageBitmap? = null,
    val owner: SceneObject? = null,
    val textureVertices: List<Offset>? = null
)