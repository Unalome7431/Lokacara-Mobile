package com.app.lokacara.ui.state

sealed interface UiEffect {
    data class ShowSnackbar(val message: String) : UiEffect
    data class ShowErrorSnackbar(val message: String) : UiEffect
    data class Navigate(val route: String) : UiEffect
    data class NavigateWithPopUp(val route: String, val popUpTo: String) : UiEffect
    data object NavigateBack : UiEffect
}

data class ScreenState<T>(
    val isLoading: Boolean = false,
    val data: T? = null,
    val error: String? = null
) {
    val isSuccess: Boolean get() = error == null && data != null && !isLoading
    val isEmpty: Boolean get() = error == null && data == null && !isLoading
}
