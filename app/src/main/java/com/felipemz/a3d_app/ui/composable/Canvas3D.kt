package com.felipemz.a3d_app.ui.composable

import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.text.rememberTextMeasurer
import com.felipemz.a3d_app.drawScope3D.DrawScope3D
import com.felipemz.a3d_app.drawScope3D.DrawScope3DFactory
import com.felipemz.a3d_app.model.Camera

@Composable
fun Canvas3D(
    modifier: Modifier,
    camera: Camera,
    showBorder: Boolean,
    onDraw: DrawScope3D.() -> Unit
) {
    val textMeasurer = rememberTextMeasurer()
    Spacer(
        modifier = modifier.drawBehind {
            val drawScope3D = DrawScope3DFactory.create(
                textMeasurer = textMeasurer,
                camera = camera,
                drawScope = this,
                showOutline = showBorder
            )
            drawScope3D.onDraw()
        }
    )
}
