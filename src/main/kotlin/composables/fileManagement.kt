package composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import fileio.addFileToVault
import javax.swing.JFileChooser


@Composable
fun filePickerDialog(
    showDialog: MutableState<Boolean>, onDismiss: () -> Unit, vaultName: String
) {
    if (showDialog.value) {
        Dialog(
            onDismissRequest = { onDismiss() }, properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            // Here you can place your file picker UI
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                val fileChooser = JFileChooser()
                fileChooser.dragEnabled = true
                val result = fileChooser.showOpenDialog(null)
                if (result == JFileChooser.APPROVE_OPTION) {
                    val selectedFile = fileChooser.selectedFile
                    addFileToVault(selectedFile.absolutePath, vaultName)
                    showDialog.value = false
                }
                onDismiss()
            }
        }
    }
}


