package com.felipemz.a3d_app.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.felipemz.a3d_app.events.ObjectEvent
import com.felipemz.a3d_app.events.ObjectEvent.OnAngle
import com.felipemz.a3d_app.events.ObjectEvent.OnPosition
import com.felipemz.a3d_app.events.ObjectEvent.OnSize
import com.felipemz.a3d_app.model.Vertex
import kotlinx.coroutines.delay

@Composable
fun FieldVertexController(
    modifier: Modifier,
    controllerType: MutableState<VertexControllerType>,
    step: Float,
    vertex: Vertex,
    objectEventHandler: (ObjectEvent) -> Unit,
) {

    fun event(value: Vertex) {
        objectEventHandler(
            when (controllerType.value) {
                VertexControllerType.POSITION -> OnPosition(value)
                VertexControllerType.ANGLE -> OnAngle(value.toRadians())
                VertexControllerType.SCALE -> OnSize(value)
            }
        )
    }

    Column(modifier = modifier) {

        Row {
            VertexControllerType.entries.forEach {
                val alpha = if (it == controllerType.value) 0.5f else 0.2f
                Text(
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { controllerType.value = it }
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
                        .padding(horizontal = 2.dp),
                    text = it.name,
                    color = Color.Black
                )
            }
        }

        Row {
            FieldVertex(
                modifier = Modifier.weight(1f),
                label = "X",
                step = step,
                value = vertex.x,
                onValueChange = { event(vertex.copy(x = it)) }
            )
            FieldVertex(
                modifier = Modifier.weight(1f),
                label = "Y",
                step = step,
                value = vertex.y,
                onValueChange = { event(vertex.copy(y = it)) }
            )
            FieldVertex(
                modifier = Modifier.weight(1f),
                label = "Z",
                step = step,
                value = vertex.z,
                onValueChange = { event(vertex.copy(z = it)) }
            )
        }
    }
}

@Composable
fun FieldVertex(
    modifier: Modifier,
    label: String,
    step: Float,
    value: Float,
    onValueChange: (Float) -> Unit
) {

    var textValue by remember(value) { mutableStateOf(value.toString()) }
    var stepMovement by remember { mutableFloatStateOf(0f) }
    var continuesMovement by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(stepMovement) {
        if (stepMovement != 0f) onValueChange(value + stepMovement)
        stepMovement = 0f
    }

    LaunchedEffect(continuesMovement, value) {
        var currentValue = value
        if (continuesMovement != 0f) {
            currentValue += continuesMovement
            onValueChange(currentValue)
        }
    }

    Column(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.padding(2.dp),
                text = "$label: ",
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            StepButtons(
                modifier = Modifier,
                onStep = { stepMovement = it * step },
                onContinue = { continuesMovement = it * step }
            )
        }

        BasicTextField(
            modifier = Modifier
                .padding(2.dp)
                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(4.dp))
                .padding(4.dp),
            value = textValue,
            onValueChange = { newValue ->
                textValue = newValue
                textValue.toFloatOrNull()?.let {
                    onValueChange(it)
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
        )
    }
}

@Composable
private fun StepButtons(
    modifier: Modifier,
    onStep: (Int) -> Unit,
    onContinue: (Int) -> Unit,
) {
    Row(modifier = modifier) {

        Text(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onStep(-1) },
                        onLongPress = {
                            onContinue(-1)
                        },
                        onPress = {
                            tryAwaitRelease()
                            onContinue(0)
                        }
                    )
                },
            text = "-",
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onStep(1) },
                        onLongPress = { onContinue(1) },
                        onPress = {
                            tryAwaitRelease()
                            onContinue(0)
                        }
                    )
                },
            text = "+",
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}