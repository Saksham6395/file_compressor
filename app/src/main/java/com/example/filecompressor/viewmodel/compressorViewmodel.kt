package com.example.filecompressor.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CompressorViewModel(application: Application) : AndroidViewModel(application) {

    private val _fileUri = MutableStateFlow<Uri?>(null)
    val fileUri = _fileUri.asStateFlow()

    private val _fileFd = MutableStateFlow(-1)
    val fileFd = _fileFd.asStateFlow()

    private var persistentPfd: ParcelFileDescriptor? = null

    fun selectFile(uri: Uri) {
        _fileUri.value = uri

        val context = getApplication<Application>()
        persistentPfd = context.contentResolver.openFileDescriptor(uri, "r")

        persistentPfd?.let {
            _fileFd.value = it.fd
        }
    }

    override fun onCleared() {
        super.onCleared()
        persistentPfd?.close()  // Clean it up when ViewModel dies
    }

    fun resetSelection() {
        _fileUri.value = null
        _fileFd.value = -1
        persistentPfd?.close()
        persistentPfd = null
    }

    fun getFileName(contentResolver: ContentResolver, uri: Uri): String? {
        var name: String? = null
        val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && nameIndex >= 0) {
                name = it.getString(nameIndex)
            }
        }
        return name
    }
}