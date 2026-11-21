package com.modutrap.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.modutrap.BloxstrapMobileApp
import com.modutrap.core.AppContainer

/** Base for all ViewModels: hands them the shared [AppContainer]. */
abstract class BaseViewModel(protected val app: Application) : AndroidViewModel(app) {
    protected val container: AppContainer
        get() = (app as BloxstrapMobileApp).container
}
