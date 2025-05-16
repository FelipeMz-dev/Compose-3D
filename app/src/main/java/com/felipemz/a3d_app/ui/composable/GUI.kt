package com.felipemz.a3d_app.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.felipemz.a3d_app.events.ObjectEvent.OnAngle
import com.felipemz.a3d_app.events.ObjectEvent.OnPosition
import com.felipemz.a3d_app.events.ObjectEvent.OnSize
import com.felipemz.a3d_app.events.SceneEvent
import com.felipemz.a3d_app.model.Camera
import com.felipemz.a3d_app.model.SceneObject
import com.felipemz.a3d_app.type_buttons.ButtonArrowType
import com.felipemz.a3d_app.type_buttons.ButtonElevationType

@Composable
fun BoxScope.GUI(
    camera: Camera,
    onMovement: (ButtonArrowType?) -> Unit,
    onElevation: (ButtonElevationType?) -> Unit,
    obj: SceneObject?,
    event: (SceneEvent) -> Unit,
    drawOutLine: MutableState<Boolean>,
) {

    val controllerType = remember { mutableStateOf(VertexControllerType.POSITION) }

    Column(
        modifier = Modifier
            .fillMaxWidth(0.6f)
            .padding(4.dp)
            .align(Alignment.TopEnd)
    ) {
        Text(
            text = "FocalLength: ${camera.focalLength}",
            color = Color.Black,
        )

        Slider(
            value = camera.focalLength,
            onValueChange = { event(SceneEvent.OnCameraFocus(it)) },
            valueRange = 100f..1000f,
        )

        Column(modifier = Modifier.weight(0.8f)) {
            Text(
                text = "drawOutLine: ${drawOutLine.value}",
                color = Color.Black,
            )

            Switch(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                checked = drawOutLine.value,
                onCheckedChange = { drawOutLine.value = it }
            )
        }
    }

    Column(
        modifier = Modifier
            .padding(8.dp)
            .align(Alignment.BottomEnd),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        obj?.let {
            FieldVertexController(
                modifier = Modifier,
                controllerType = controllerType,
                step = if (controllerType.value == VertexControllerType.ANGLE) 5f else 0.5f,
                vertex = when (controllerType.value) {
                    VertexControllerType.POSITION -> obj.position
                    VertexControllerType.ANGLE -> obj.rotation.toDegrees()
                    VertexControllerType.SCALE -> obj.scale
                }
            ) { event(SceneEvent.OnObjectEvent(it, obj)) }
        }

        ControlMovementCamera(onMovement, onElevation)
    }

    PrinterCamera(camera)
}

enum class VertexControllerType { POSITION, ANGLE, SCALE }

@Composable
private fun ControlMovementCamera(
    onMovement: (ButtonArrowType?) -> Unit,
    onElevation: (ButtonElevationType?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(modifier = Modifier.size(120.dp)) {
            ButtonArrowType.entries.forEach { type ->
                Icon(
                    modifier = Modifier
                        .align(type.alignment)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .padding(12.dp)
                        .rotate(type.rotation)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = { onMovement(type) },
                                onTap = { onMovement(null) },
                            )
                        },
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null
                )
            }
        }

        Column(
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {

            ButtonElevationType.entries.forEach { type ->
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(type.shape)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .padding(12.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = { onElevation(type) },
                                onTap = { onElevation(null) },
                            )
                        },
                    text = type.text,
                )
            }
        }
    }
}

@Composable
private fun PrinterCamera(camera: Camera) {
    Column {
        Text(
            text = "CamPosition:\n x:${camera.position.x}\n y:${camera.position.y}\n z:${camera.position.z}",
            color = Color.Black,
        )

        Text(
            text = "CamRotation:\n x:${camera.angle.x}\n y:${camera.angle.y}\n z:${camera.angle.z}",
            color = Color.Black,
        )
    }
}