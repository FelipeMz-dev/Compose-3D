package com.felipemz.a3d_app

import com.felipemz.a3d_app.model.SceneObject

class Scene {
    val objects = mutableListOf<SceneObject>()

    fun add(obj: SceneObject) {
        objects.add(obj)
    }

    fun remove(obj: SceneObject) {
        objects.remove(obj)
    }

    fun edit(index: Int, onEdit: (SceneObject) -> SceneObject) {
        if (index in objects.indices) {
            objects[index] = onEdit(objects[index])
        }
    }

    fun clear() {
        objects.clear()
    }
}