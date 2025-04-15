package com.felipemz.a3d_app.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path

object PathUtils {
    fun pathFromOffsets(offsets: List<Offset>): Path = Path().apply {
        offsets.forEachIndexed { index, offset ->
            if (index == 0) moveTo(offset.x, offset.y)
            else lineTo(offset.x, offset.y)
        }
        close()
    }
}