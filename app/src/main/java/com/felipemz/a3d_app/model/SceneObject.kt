package com.felipemz.a3d_app.model

import com.felipemz.a3d_app.DrawScope3D

data class SceneObject(
    val position: Vertex,
    val rotation: Vertex,
    val scale: Vertex,
    val center: Vertex = Vertex(0f, 0f, 0f),
    val draw: DrawScope3D.(SceneObject) -> Unit
)