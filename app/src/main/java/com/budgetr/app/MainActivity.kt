package com.budgetr.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.budgetr.app.ui.navigation.BudgetrNavGraph
import com.budgetr.app.ui.theme.BudgetrTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BudgetrTheme {
                BudgetrNavGraph()
            }
        }
    }
}
