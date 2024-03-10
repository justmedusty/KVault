import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

@Composable
@Preview
fun app() {

    MaterialTheme {

        appContent()
    }
}

@Composable
fun appContent() {
    MaterialTheme {
        Column {
            TopAppBar(
                title = {
                    Text("KVault") }, modifier =  Modifier.align(Alignment.CenterHorizontally), actions = {
                IconButton(onClick = { /* Handle minimize */ }) {
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Minimize")
                }
                IconButton(onClick = { /* Handle maximize */ }) {
                    Icon(Icons.Filled.Add, contentDescription = "Maximize")
                }
                IconButton(onClick = { /* Handle close */ }) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            })
            Column(
                modifier = Modifier.padding(16.dp).fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Your application content goes here")
            }
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        app()
    }
}
