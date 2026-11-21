package com.modutrap

import android.app.Application
import com.modutrap.core.AppContainer
import com.modutrap.core.Logger

/**
 * Application entry point. Builds the [AppContainer] once so every ViewModel
 * shares the same repositories, services and Shizuku state.
 */
class BloxstrapMobileApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        Logger.writeLine("BloxstrapMobileApp", "Application initialized.")
    }
}
