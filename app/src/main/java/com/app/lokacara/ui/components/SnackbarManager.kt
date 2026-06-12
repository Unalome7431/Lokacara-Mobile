package com.app.lokacara.ui.components

import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object SnackbarManager {
    private var hostState: SnackbarHostState? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    fun init(state: SnackbarHostState) {
        hostState = state
    }

    fun show(message: String) {
        scope.launch {
            hostState?.currentSnackbarData?.dismiss()
            hostState?.showSnackbar(message)
        }
    }

    fun showError(message: String) {
        show(message)
    }
}
