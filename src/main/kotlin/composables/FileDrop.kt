package composables

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.onExternalDrag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Preview
fun fileDrop() {
    Box(modifier = Modifier.padding(5.dp).fillMaxSize().background(Color.White)) {
        val colors = listOf(Color.Black, Color.Blue)
        var activeColorIndex by remember { mutableStateOf(0) }
        fun switchActiveColor() {
            activeColorIndex = (activeColorIndex + 1) % colors.size
        }

        val activeColor = colors[activeColorIndex]

        Column {

            Box(modifier = Modifier.size(width = 400.dp, height = 300.dp).padding(5.dp)
                .border(width = 2.dp, activeColor).onExternalDrag(onDragStart = { switchActiveColor() },
                    onDragExit = { switchActiveColor() },
                    onDrop = { externalDragValue ->

                    })) {
                Column(modifier = Modifier.align(Alignment.Center)) {
                    dragAndDropDescription(
                        modifier = Modifier.align(Alignment.CenterHorizontally), color = activeColor
                    )
                }
            }
        }

    }

}


@Composable
fun dragAndDropDescription(modifier: Modifier, color: Color) {
    val modifier = modifier.padding(vertical = 2.dp)
    println("Icon recomposition $color")
    Icon(
        imageVector = Icons.AutoMirrored.Filled.Send, modifier = modifier, tint = color, contentDescription = ""
    )
    println("Text recomposition: $color")
    Text(
        "Drag & drop a file", fontSize = 20.sp, modifier = modifier, color = color
    )
}

