package com.example.filecompressor.navigation

import com.example.filecompressor.screen.selection
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.filecompressor.screen.compressor
import com.example.filecompressor.screen.decompressed_sharing
import com.example.filecompressor.screen.decompressor
import com.example.filecompressor.screen.sharing
import com.example.filecompressor.viewmodel.CompressorViewModel

@Composable
fun NavGraph( navController: NavHostController,viewModel: CompressorViewModel){
    NavHost(navController = navController, startDestination = Routes.Selection.routes) {

        composable(Routes.Compress.routes){
            compressor(
                navController = navController,
                viewModel = viewModel,
                onPickFile = { triggerPicker ->
                    triggerPicker()
                }
            )
        }
        composable(Routes.Decompress.routes){
            decompressor(navController,viewModel)
        }
        composable(Routes.Selection.routes){
            selection(navController)
        }
        composable(Routes.Sharing.routes){
            sharing(navController,viewModel)
        }
        composable(Routes.Decompressed_Sharing.routes){
            decompressed_sharing(navController,viewModel)
        }
    }
}
