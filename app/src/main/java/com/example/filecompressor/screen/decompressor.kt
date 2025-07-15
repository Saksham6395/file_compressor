package com.example.filecompressor.screen


import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.filecompressor.navigation.Routes
import com.example.filecompressor.viewmodel.CompressorViewModel

@Composable
fun decompressor(navController: NavHostController,viewModel: CompressorViewModel) {
    val fileUri by viewModel.fileUri.collectAsState()
    val fileFd by viewModel.fileFd.collectAsState()

    val context = LocalContext.current

    // Trigger launcher when this lambda is invoked
    var launchPicker: (() -> Unit)? by remember { mutableStateOf(null) }

    val fileName = remember(fileUri) {
        fileUri?.let {
            viewModel.getFileName(context.contentResolver, it)
        } ?: "No file selected"
    }

    androidx.activity.compose.BackHandler {
        if (fileUri != null) {
            // First back press clears the file
            viewModel.resetSelection()
        } else {
            // Second back press navigates back to selection
            navController.popBackStack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(50.dp))

        Button(
            onClick = {
                launchPicker?.invoke()
            },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text(text = "Pick a .sks File")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (fileFd != -1) "Picked: $fileName" else "No file selected",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                navController.navigate(Routes.Decompressed_Sharing.routes){
                    popUpTo(Routes.Compress.routes) {
                        inclusive = true
                    }
                }
            },
            enabled = fileFd != -1,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(bottom = 16.dp)
        ) {
            Text(text = "Next")
        }
    }

    // Set up the file picker launcher inside the composable
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                val name = viewModel.getFileName(context.contentResolver, it)
                if (name?.endsWith(".sks") == true) {
                    context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    viewModel.selectFile(it)
                } else {
                    Toast.makeText(context, "Please pick a .sks file only 😅", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    // Assign the actual launcher to be triggered from button
    LaunchedEffect(Unit) {
        launchPicker = {
            filePickerLauncher.launch(arrayOf("*/*")) //all type of file
        }
    }
}