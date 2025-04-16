package com.felipemz.a3d_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.imageResource
import com.felipemz.a3d_app.model.Camera
import com.felipemz.a3d_app.model.SceneObject
import com.felipemz.a3d_app.model.Vertex
import com.felipemz.a3d_app.type_buttons.ButtonArrowType
import com.felipemz.a3d_app.type_buttons.ButtonElevationType
import com.felipemz.a3d_app.ui.theme._3D_AppTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            _3D_AppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Scene3D(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    )
                }
            }
        }
    }
}

@Composable
fun Scene3D(
    modifier: Modifier = Modifier
) {

    val scene by remember { mutableStateOf(Scene()) }
    val image = ImageBitmap.imageResource(R.drawable.texture_stone)

    val initScene: () -> Unit = {
        scene.add(
            SceneObject(
                position = Vertex(0f, -10f, 20f),
                rotation = Vertex(0f, 0f, 0f),
                scale = Vertex(1f, 1f, 1f),
                draw = { obj -> drawCubeObject(
                    obj,
                    faceColor = {
                        when (it) {
                            0, 1 -> Color.Red
                            2, 3 -> Color.Green
                            4, 5 -> Color.Blue
                            6, 7 -> Color.Magenta
                            8, 9 -> Color.Cyan
                            10, 11 -> Color.Yellow
                            else -> Color.White
                        }
                    }) }
            )
        )

        scene.add(
            SceneObject(
                position = Vertex(0f, -10f, 5f),
                rotation = Vertex(0f, 0f, 0f),
                scale = Vertex(1f, 1f, 1f),
                draw = { obj -> drawCubeObject(obj, faceImage = { image }) }
            )
        )

    }

    val screenSizes = LocalView.current
    var camera by remember { mutableStateOf(Camera()) }

    LaunchedEffect(screenSizes.width) {
        camera = camera.copy(
            screenSize = Size(
                width = screenSizes.width.toFloat(),
                height = screenSizes.height.toFloat()
            )
        )
    }

    var onMovement by remember { mutableStateOf<ButtonArrowType?>(null) }
    val moveToDirection: (ButtonArrowType) -> Unit = { type ->
        camera = when (type) {
            ButtonArrowType.BUTTON_UP -> camera.move(Vertex(0f, 0f, 0.5f))
            ButtonArrowType.BUTTON_RIGHT -> camera.move(Vertex(0.5f, 0f, 0f))
            ButtonArrowType.BUTTON_DOWN -> camera.move(Vertex(0f, 0f, -0.5f))
            ButtonArrowType.BUTTON_LEFT -> camera.move(Vertex(-0.5f, 0f, 0f))
        }
    }
    var onElevation by remember { mutableStateOf<ButtonElevationType?>(null) }
    val moveToElevation: (ButtonElevationType) -> Unit = { type ->
        camera = when (type) {
            ButtonElevationType.BUTTON_UP -> camera.move(Vertex(0f, -0.5f, 0f))
            ButtonElevationType.BUTTON_DOWN -> camera.move(Vertex(0f, 0.5f, 0f))
        }
    }
    val cubeNumber = remember { mutableIntStateOf(0) }
    val drawBorderLine = remember { mutableStateOf(false) }
    val cubeAngle = remember { mutableStateOf(Vertex(0f, 0f, 0f)) }
    val cubeSize = remember { mutableFloatStateOf(10f) }

    LaunchedEffect(Unit) {
        initScene()
        while (true) {
            onMovement?.let { moveToDirection(it) }
            onElevation?.let { moveToElevation(it) }
            delay(1_000 / 30)
        }
    }

    LaunchedEffect(cubeAngle.value) {
        scene.edit(0) {
            it.copy(
                rotation = Vertex(
                    x = cubeAngle.value.x,
                    y = cubeAngle.value.y,
                    z = cubeAngle.value.z
                )
            )
        }
        scene.edit(1) {
            it.copy(
                rotation = Vertex(
                    x = cubeAngle.value.x,
                    y = cubeAngle.value.y,
                    z = cubeAngle.value.z
                )
            )
        }
    }

    LaunchedEffect(cubeSize.value) {
        scene.edit(0) {
            it.copy(
                scale = Vertex(
                    x = cubeSize.value * 2,
                    y = cubeSize.value * 2,
                    z = cubeSize.value * 2
                )
            )
        }
        scene.edit(1) {
            it.copy(
                scale = Vertex(
                    x = 10f + cubeSize.value,
                    y = 10f + cubeSize.value,
                    z = 10f + cubeSize.value
                )
            )
        }
    }

    Box(
        modifier = modifier
            .background(Color.White)
            .pointerInput(Unit) {
                detectDragGestures { _, dragAmount ->
                    camera = camera.copy(
                        angle = camera.rotate(
                            Vertex(
                                x = dragAmount.y * 0.005f,
                                y = dragAmount.x * -0.005f,
                                z = 0f
                            )
                        ).angle
                    )
                }
            }
    ) {

        Canvas3D(
            modifier = Modifier.fillMaxSize(),
            camera = camera,
            showBorder = drawBorderLine.value
        ) {
            drawGrid()
            drawScene(scene)
        }

        GUI(
            camera = camera,
            onSliderChange = { camera = camera.copy(focalLength = it) },
            onMovement = { onMovement = it },
            onElevation = { onElevation = it },
            cubeNumber = cubeNumber,
            cubeAngle = cubeAngle,
            cubeSize = cubeSize,
            drawOutLine = drawBorderLine,
        )
    }
}