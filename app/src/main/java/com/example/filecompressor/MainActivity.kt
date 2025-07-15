package com.example.filecompressor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.compose.rememberNavController
import com.example.filecompressor.navigation.NavGraph
import com.example.filecompressor.ui.theme.FileCompressorTheme
import com.example.filecompressor.viewmodel.CompressorViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: CompressorViewModel by viewModels()
        setContent {
            FileCompressorTheme {
                val navController= rememberNavController()
                NavGraph(navController = navController,viewModel)
            }
        }
    }
}