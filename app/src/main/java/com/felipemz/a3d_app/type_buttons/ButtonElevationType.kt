package com.felipemz.a3d_app.type_buttons

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

enum class ButtonElevationType(val text: String, val shape: Shape) {
    BUTTON_UP("UP", RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
    BUTTON_DOWN("DOWN", RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
}