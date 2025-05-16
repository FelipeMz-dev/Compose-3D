package com.felipemz.a3d_app.ui

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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import com.felipemz.a3d_app.ui.composable.Canvas3D
import com.felipemz.a3d_app.model.Scene
import com.felipemz.a3d_app.events.SceneEvent
import com.felipemz.a3d_app.model.SceneObject
import com.felipemz.a3d_app.model.Vertex
import com.felipemz.a3d_app.type_buttons.ButtonArrowType
import com.felipemz.a3d_app.type_buttons.ButtonElevationType
import com.felipemz.a3d_app.ui.composable.GUI
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
fun Scene3D(modifier: Modifier = Modifier) {

    val scene by remember { mutableStateOf(Scene()) }

    val initScene: () -> Unit = { scene.add(exampleObjectCube()) }

    val events = remember { mutableStateOf<SceneEvent?>(null) }
    var onMovement by remember { mutableStateOf<ButtonArrowType?>(null) }
    val moveToDirection: (ButtonArrowType) -> Unit = { type ->
        scene.eventHandler(
            SceneEvent.OnCameraMove(
                when (type) {
                    ButtonArrowType.BUTTON_UP -> Vertex(0f, 0f, 0.5f)
                    ButtonArrowType.BUTTON_RIGHT -> Vertex(0.5f, 0f, 0f)
                    ButtonArrowType.BUTTON_DOWN -> Vertex(0f, 0f, -0.5f)
                    ButtonArrowType.BUTTON_LEFT -> Vertex(-0.5f, 0f, 0f)
                }
            )
        )
    }
    var onElevation by remember { mutableStateOf<ButtonElevationType?>(null) }
    val moveToElevation: (ButtonElevationType) -> Unit = { type ->
        scene.eventHandler(
            SceneEvent.OnCameraMove(
                when (type) {
                    ButtonElevationType.BUTTON_UP -> Vertex(0f, -0.5f, 0f)
                    ButtonElevationType.BUTTON_DOWN -> Vertex(0f, 0.5f, 0f)
                }
            )
        )
    }
    val objIndex by remember { mutableIntStateOf(0) }
    val currentObject by remember(scene.objects, objIndex, initScene) {
        derivedStateOf { scene.objects.getOrNull(objIndex) }
    }
    val drawBorderLine = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        initScene()
        val frameDuration = 1000L / 30
        while (true) {
            events.value?.let { event ->
                scene.eventHandler(event)
                events.value = null
            }
            val startTime = System.currentTimeMillis()
            onMovement?.let { moveToDirection(it) }
            onElevation?.let { moveToElevation(it) }
            val elapsedTime = System.currentTimeMillis() - startTime
            delay((frameDuration - elapsedTime).coerceAtLeast(0L))
        }
    }

    Box(
        modifier = modifier
            .background(Color.White)
            .pointerInput(Unit) {
                detectDragGestures { _, dragAmount ->
                    scene.eventHandler(
                        SceneEvent.OnCameraRotate(
                            Vertex(
                                x = dragAmount.y * 0.005f,
                                y = dragAmount.x * -0.005f,
                                z = 0f
                            )
                        )
                    )
                }
            }
    ) {

        Canvas3D(
            modifier = Modifier.fillMaxSize(),
            camera = scene.camera,
            showBorder = drawBorderLine.value
        ) {
            if (scene.camera.screenSize != size) scene.eventHandler(SceneEvent.OnSizeCamera(size))
            drawGrid()
            drawScene(scene)
        }

        GUI(
            camera = scene.camera,
            onMovement = { onMovement = it },
            onElevation = { onElevation = it },
            obj = currentObject,
            event = { events.value = it },
            drawOutLine = drawBorderLine,
        )
    }
}

fun exampleObjectCube() = SceneObject(
    position = Vertex(0f, -10f, 40f),
    rotation = Vertex(0f, 0f, 0f),
    scale = Vertex(10f, 10f, 10f),
    draw = { obj ->
        drawCubeObject(
            obj = obj,
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
            }
        )
    }
)