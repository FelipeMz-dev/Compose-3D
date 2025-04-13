package com.felipemz.a3d_app.model

import androidx.compose.ui.graphics.Color

data class Triangle3D(
    val a: Vertex,
    val b: Vertex,
    val c: Vertex,
    val zIndex: Float = 0f,
    val color: Color = Color.Unspecified,
    val owner: SceneObject? = null
)