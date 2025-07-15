package com.example.filecompressor.screen

import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import com.example.decompressor.NativeLib
import com.example.filecompressor.utils.sharedPref.formatSize
import com.example.filecompressor.utils.sharedPref.getFileSizeFromUri
import com.example.filecompressor.viewmodel.CompressorViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun decompressed_sharing(
    navController: NavHostController,
    viewModel: CompressorViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isProcessing by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    var decompressedFilePath by remember { mutableStateOf<String?>(null) }
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
                    val outputPath = getdecompressPublicOutputFilePath(fileName)
                    Log.d("DECOMPRESS_PATH", "Final decompressed output path = $outputPath")

                    val result = withContext(kotlinx.coroutines.Dispatchers.IO) {
                        NativeLib.decompress(inputFd, outputPath)
                    }
                     Log.d("DEBUG_LOG", "Output path generated = $outputPath")
                    isProcessing = false

                    if (result == 0) {
                        decompressedFilePath = outputPath
                        statusMessage = "Decompression successful!"

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
                        } else {
                            compressionRatioText = null
                        }
                    }
                    else {
                        statusMessage = "Decompression failed 😔"
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
                decompressedFilePath?.let {
                    shareDecompressedFile(context, it)
                } ?: run {
                    statusMessage = "Please download first 😅"
                }
            },
            enabled = !isProcessing && decompressedFilePath != null,
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
        if (compressionRatioText != null && decompressedFilePath != null) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Compressed File Size(.sks): ${originalSizeFormatted ?: "-"}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Decompressed File Size(.txt): ${decompressedSizeFormatted ?: "-"}",
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

fun getdecompressPublicOutputFilePath(originalName: String): String {
    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    if (!downloadsDir.exists()) {
        downloadsDir.mkdirs()
    }

    // ✅ Clean and normalize the name
    val cleanName = originalName
        .replace("_compressed_compressed.sks", "")
        .replace("_compressed.sks", "")
        .replace(".sks", "")
        .replace(".txt", "") // just in case

    val outputFile = File(downloadsDir, "${cleanName}_decompressed.txt")
    return outputFile.absolutePath
}

fun shareDecompressedFile(context: Context, filePath: String) {
    val file = File(filePath)
    val uri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain" // not octet-stream, since it’s decompressed
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(intent, "Share decompressed file via"))
}