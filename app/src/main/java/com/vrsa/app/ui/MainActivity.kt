package com.vrsa.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vrsa.app.VrsaApplication
import com.vrsa.app.ui.theme.VrsaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as VrsaApplication).container
        setContent {
            VrsaTheme {
                val viewModel: EditorViewModel =
                    viewModel(factory = EditorViewModel.factory(container.repository))
                EditorScreen(
                    viewModel = viewModel,
                    scheduler = container.scheduler,
                    notifier = container.notifier,
                )
            }
        }
    }
}
