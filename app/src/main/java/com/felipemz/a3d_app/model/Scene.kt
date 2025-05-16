package com.felipemz.a3d_app.model

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import com.felipemz.a3d_app.events.SceneEvent

class Scene {

    private val _camera = mutableStateOf(Camera())
    val camera get() = _camera.value

    private val _idIndex = mutableIntStateOf(1)
    val idIndex: Int get() = _idIndex.intValue

    private val _objects = mutableStateOf(listOf<SceneObject>())
    val objects: List<SceneObject> get() = _objects.value

    fun add(obj: SceneObject) {
        _objects.value += obj
    }

    private fun editObject(obj: SceneObject, update: (SceneObject) -> SceneObject) {
        if (obj in objects) {
            val updatedObject = update(obj)
            _objects.value = _objects.value.map {
                if (it == obj) updatedObject else it
            }
        }
    }

    private fun editCamera(update: (Camera) -> Camera) {
        _camera.value = update(camera)
    }

    fun eventHandler(event: SceneEvent) {
        when (event) {
            is SceneEvent.OnObjectEvent -> editObject(event.obj) { it.eventHandler(event.event) }
            is SceneEvent.OnCameraFocus -> editCamera { it.copy(focalLength = event.length) }
            is SceneEvent.OnCameraRotate -> editCamera { it.rotate(event.angle) }
            is SceneEvent.OnCameraMove -> editCamera { it.move(event.position) }
            is SceneEvent.OnSizeCamera -> editCamera { it.copy(screenSize = event.size) }
            else -> Unit
        }
    }
}
