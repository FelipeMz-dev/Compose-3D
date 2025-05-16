package com.felipemz.a3d_app.model

import com.felipemz.a3d_app.drawScope3D.DrawScope3D
import com.felipemz.a3d_app.events.ObjectEvent
import com.felipemz.a3d_app.events.ObjectEvent.*
import java.util.concurrent.atomic.AtomicInteger

data class SceneObject(
    val id: Int = generateId(),
    val position: Vertex = Vertex(),
    val rotation: Vertex = Vertex(),
    val scale: Vertex = Vertex(),
    val center: Vertex = Vertex(0f, 0f, 0f),
    val draw: DrawScope3D.(SceneObject) -> Unit
){
    companion object {
        private val idCounter = AtomicInteger(0)
        fun generateId(): Int = idCounter.incrementAndGet()
    }

    fun eventHandler(event: ObjectEvent) = when (event) {
        is OnPosition -> this.copy(position = event.position)
        is OnAngle -> this.copy(rotation = event.angle)
        is OnSize -> this.copy(scale = event.size)
    }
}