package com.modutrap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.modutrap.ui.AppRoot

/** Single activity host. All UI is composed by [AppRoot]. */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppRoot((application as BloxstrapMobileApp).container)
        }
    }
}
