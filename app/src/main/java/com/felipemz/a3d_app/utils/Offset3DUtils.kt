package com.felipemz.a3d_app.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path

object Offset3DUtils {
    fun pathFromOffsets(offsets: List<Offset>): Path = Path().apply {
        offsets.forEachIndexed { index, offset ->
            if (index == 0) moveTo(offset.x, offset.y)
            else lineTo(offset.x, offset.y)
        }
        close()
    }

    fun isPathOutOfBounds(screenSize: Size, path: Path): Boolean {
        val bounds = path.getBounds()
        return bounds.right < 0 || bounds.left > screenSize.width || bounds.bottom < 0 || bounds.top > screenSize.height
    }

    fun adjustPoint(point: Offset, other: Offset, min: Offset, max: Offset): Offset {
        var adjusted = point
        if (point.x <= min.x) {
            adjusted = adjusted.copy(
                x = min.x,
                y = point.y + (min.x - point.x) * (other.y - point.y) / (other.x - point.x)
            )
        } else if (point.x >= max.x) {
            adjusted = adjusted.copy(
                x = max.x,
                y = point.y + (max.x - point.x) * (other.y - point.y) / (other.x - point.x)
            )
        }
        if (point.y <= min.y) {
            adjusted = adjusted.copy(
                y = min.y,
                x = point.x + (min.y - point.y) * (other.x - point.x) / (other.y - point.y)
            )
        } else if (point.y >= max.y) {
            adjusted = adjusted.copy(
                y = max.y,
                x = point.x + (max.y - point.y) * (other.x - point.x) / (other.y - point.y)
            )
        }
        return adjusted
    }
}