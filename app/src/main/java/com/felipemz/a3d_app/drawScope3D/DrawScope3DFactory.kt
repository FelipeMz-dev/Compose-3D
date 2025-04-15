package com.felipemz.a3d_app.drawScope3D

import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import com.felipemz.a3d_app.model.Camera

object DrawScope3DFactory {
    fun create(
        textMeasurer: TextMeasurer,
        camera: Camera,
        drawScope: DrawScope,
        showOutline: Boolean
    ): DrawScope3D {
        return DrawScope3DImpl(
            textMeasurer = textMeasurer,
            camera = camera,
            drawScope = drawScope,
            showOutline = showOutline
        )
    }
}