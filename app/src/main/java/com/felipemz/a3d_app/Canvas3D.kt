package com.felipemz.a3d_app

import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import com.felipemz.a3d_app.model.Camera

@Composable
fun Canvas3D(
    modifier: Modifier,
    camera: Camera,
    showBorder: Boolean,
    onDraw: DrawScope3D.() -> Unit
) {
    val textMeasurer = rememberTextMeasurer()
    Spacer(modifier.drawBehind {
        draw3DScene(
            textMeasurer = textMeasurer,
            camera = camera,
            onDraw = onDraw,
            showBorder = showBorder
        )
    })
}

fun DrawScope.draw3DScene(
    textMeasurer: TextMeasurer,
    camera: Camera,
    showBorder: Boolean,
    onDraw: DrawScope3D.() -> Unit
) {
    val drawScope3D = DrawScope3DImpl(
        textMeasurer = textMeasurer,
        camera = camera,
        drawScope = this,
        showOutline = showBorder
    )
    drawScope3D.onDraw()
}