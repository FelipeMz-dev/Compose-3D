package com.felipemz.a3d_app.events

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import com.felipemz.a3d_app.model.Vertex

sealed interface ObjectEvent {
    data class OnPosition(val position: Vertex) : ObjectEvent
    data class OnAngle(val angle: Vertex) : ObjectEvent
    data class OnSize(val size: Vertex) : ObjectEvent
    data class OnTexture(val texture: ImageBitmap) : SceneEvent
    data class OnColor(val color: Color) : SceneEvent
}
