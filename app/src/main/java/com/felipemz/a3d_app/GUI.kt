package com.felipemz.a3d_app

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
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.felipemz.a3d_app.model.Camera
import com.felipemz.a3d_app.model.Vertex
import com.felipemz.a3d_app.type_buttons.ButtonArrowType
import com.felipemz.a3d_app.type_buttons.ButtonElevationType

@Composable
fun BoxScope.GUI(
    camera: Camera,
    onSliderChange: (Float) -> Unit,
    onMovement: (ButtonArrowType?) -> Unit,
    onElevation: (ButtonElevationType?) -> Unit,
    cubeNumber: MutableIntState,
    cubeAngle: MutableState<Vertex>,
    cubeSize: MutableState<Float>,
    backLine: MutableState<Boolean>,
){
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
            onValueChange = onSliderChange,
            valueRange = 100f..800f,
        )

        //Text(
        //    text = "cubeNumber: ${cubeNumber.intValue}",
        //    color = Color.Black,
        //)
//
        //Slider(
        //    value = cubeNumber.intValue.toFloat(),
        //    onValueChange = { cubeNumber.intValue = it.toInt() },
        //    valueRange = 0f..100f,
        //)
    }

    Column(
        modifier = Modifier
            .padding(8.dp)
            .align(Alignment.BottomEnd)
    ) {

        Row {
            Column(modifier = Modifier.weight(1.2f)) {
                Text(
                    text = "cubeSize: ${cubeSize.value.toString().filterIndexed { index, _ -> index < 5 }}",
                    color = Color.Black,
                )

                Slider(
                    value = cubeSize.value,
                    onValueChange = { cubeSize.value = it },
                    valueRange = 0.1f..10f,
                )
            }

            Column(modifier = Modifier.weight(0.8f)) {
                Text(
                    text = "backLine: ${backLine.value}",
                    color = Color.Black,
                )

                Switch(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    checked = backLine.value,
                    onCheckedChange =  { backLine.value = it }
                )
            }
        }

        Text(
            text = "Angle:",
            color = Color.Black,
        )

        Row {

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "x: ${Math.toDegrees(cubeAngle.value.x.toDouble()).toString().filterIndexed { index, _ -> index < 5 }}",
                    color = Color.Black,
                )

                Slider(
                    value = cubeAngle.value.x,
                    onValueChange = { cubeAngle.value = cubeAngle.value.copy(x = it) },
                    valueRange = 0f..Math.toRadians(360.0).toFloat(),
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "y: ${Math.toDegrees(cubeAngle.value.y.toDouble()).toString().filterIndexed { index, _ -> index < 5 }}",
                    color = Color.Black,
                )

                Slider(
                    value = cubeAngle.value.y,
                    onValueChange = { cubeAngle.value = cubeAngle.value.copy(y = it) },
                    valueRange = 0f..Math.toRadians(360.0).toFloat(),
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "z: ${Math.toDegrees(cubeAngle.value.z.toDouble()).toString().filterIndexed { index, _ -> index < 5 }}",
                    color = Color.Black,
                )

                Slider(
                    value = cubeAngle.value.z,
                    onValueChange = { cubeAngle.value = cubeAngle.value.copy(z = it) },
                    valueRange = 0f..Math.toRadians(360.0).toFloat(),
                )
            }
        }

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

    PrinterCamera(camera)
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