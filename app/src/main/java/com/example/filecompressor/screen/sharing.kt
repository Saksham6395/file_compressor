package com.example.filecompressor.screen

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.media.MediaScannerConnection
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import com.example.compressor.NativeLib
import com.example.filecompressor.viewmodel.CompressorViewModel
import kotlinx.coroutines.launch
import java.io.File
import android.os.Environment
import android.provider.OpenableColumns
import com.example.filecompressor.utils.sharedPref.formatSize
import com.example.filecompressor.utils.sharedPref.getFileSizeFromUri
import kotlinx.coroutines.withContext

@Composable
fun sharing(
    navController: NavHostController,
    viewModel: CompressorViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isProcessing by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    var compressedFilePath by remember { mutableStateOf<String?>(null) }
    var compressionRatioText by remember { mutableStateOf<String?>(null) }
    var originalSizeFormatted by remember { mutableStateOf<String?>(null) }
    var decompressedSizeFormatted by remember { mutableStateOf<String?>(null) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                isProcessing = true
                statusMessage = "Compressing..."
                coroutineScope.launch {
                    kotlinx.coroutines.delay(100)
                    val inputFd = viewModel.fileFd.value
                    val uri = viewModel.fileUri.value
                    val fileName = uri?.let { getFileName(context.contentResolver, it) } ?: "output"
                    val outputPath = getPublicOutputFilePath(fileName)

                    val result = withContext(kotlinx.coroutines.Dispatchers.IO) {
                        NativeLib.compress(inputFd, outputPath)
                    }
                    isProcessing = false

                    if (result == 0) {
                        compressedFilePath = outputPath
                        statusMessage = "Compression successful!"

                        // 🧠 Notify media scanner
                        MediaScannerConnection.scanFile(
                            context,
                            arrayOf(outputPath),
                            null,
                            null
                        )
                        val originalFileSize = uri?.let { getFileSizeFromUri(context, it) } ?: 0L
                        val decompressedFileSize = File(outputPath).length()

                        if (originalFileSize != 0L) {
                            val ratio = decompressedFileSize.toDouble() / originalFileSize
                            val percentDiff = 100 - (ratio * 100)
                            compressionRatioText = "Size changed by %.1f%% (%.2fx)".format(percentDiff, ratio)

                            originalSizeFormatted = formatSize(originalFileSize)
                            decompressedSizeFormatted = formatSize(decompressedFileSize)
                        }
                    } else {
                        statusMessage = "Compression failed 😔"
                    }
                }
            },
            enabled = !isProcessing,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text(text = if (isProcessing) "Processing..." else "Download")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                compressedFilePath?.let {
                    shareFile(context, it)
                } ?: run {
                    statusMessage = "Please download first 😅"
                }
            },
            enabled = !isProcessing && compressedFilePath != null,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text(text = "Share")
        }

        if (isProcessing) {
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator()
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = statusMessage, style = MaterialTheme.typography.bodyMedium)
        if (compressionRatioText != null && compressedFilePath != null) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Orignal File Size(.txt): ${originalSizeFormatted ?: "-"}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Compressed File Size(.sks): ${decompressedSizeFormatted ?: "-"}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = compressionRatioText!!,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// 🧠 Get output file path in internal storage

fun getPublicOutputFilePath(originalName: String): String {
    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    if (!downloadsDir.exists()) {
        downloadsDir.mkdirs()
    }
    val baseName = originalName.substringBeforeLast('.')
    val outputFile = File(downloadsDir, "${baseName}_compressed.sks")
    return outputFile.absolutePath
}


// 📤 Share the compressed file using intent
fun shareFile(context: Context, filePath: String) {
    val file = File(filePath)
    val uri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/octet-stream"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(intent, "Share compressed file via"))
}

fun getFileName(contentResolver: android.content.ContentResolver, uri: Uri): String {
    var name = "unknown"
    val returnCursor: Cursor? = contentResolver.query(uri, null, null, null, null)
    returnCursor?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && nameIndex != -1) {
            name = cursor.getString(nameIndex)
        }
    }
    return name
}