package com.felipemz.a3d_app.type_buttons

import androidx.compose.ui.Alignment

enum class ButtonArrowType(
    val rotation: Float,
    val alignment: Alignment
) {
    BUTTON_UP(-90f, Alignment.TopCenter),
    BUTTON_RIGHT(0f, Alignment.CenterEnd),
    BUTTON_DOWN(90f, Alignment.BottomCenter),
    BUTTON_LEFT(180f, Alignment.CenterStart)
}