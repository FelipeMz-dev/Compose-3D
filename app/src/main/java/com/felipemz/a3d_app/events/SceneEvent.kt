package com.felipemz.a3d_app.events

import androidx.compose.ui.geometry.Size
import com.felipemz.a3d_app.model.SceneObject
import com.felipemz.a3d_app.model.Vertex

sealed interface SceneEvent {
    data class OnObjectEvent(
        val event: ObjectEvent,
        val obj: SceneObject
    ) : SceneEvent

    data class OnCameraMove(val position: Vertex) : SceneEvent
    data class OnCameraRotate(val angle: Vertex) : SceneEvent
    data class OnCameraFocus(val length: Float) : SceneEvent
    data class OnSizeCamera(val size: Size) : SceneEvent
}