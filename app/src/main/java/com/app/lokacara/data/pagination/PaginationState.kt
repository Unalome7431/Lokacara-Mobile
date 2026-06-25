package com.app.lokacara.data.pagination

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PaginationGuard(
    private val isInFlight: () -> Boolean
) {
    fun canLoadMore(): Boolean = !isInFlight()
}

class PaginationController(
    initialPage: Int = 1,
    initialTotalPages: Int = 1
) {
    private val _currentPage = MutableStateFlow(initialPage)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _totalPages = MutableStateFlow(initialTotalPages)
    val totalPages: StateFlow<Int> = _totalPages.asStateFlow()

    val hasMorePages: Boolean
        get() = currentPage.value < totalPages.value

    fun updateFromResponse(currentPage: Int, lastPage: Int) {
        _currentPage.value = currentPage
        _totalPages.value = lastPage
    }

    fun nextPage(): Int = currentPage.value + 1

    fun reset(page: Int = 1, totalPages: Int = 1) {
        _currentPage.value = page
        _totalPages.value = totalPages
    }
}

class PaginationLoadingState {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    fun startLoading() { _isLoading.value = true }
    fun finishLoading() { _isLoading.value = false }
    fun startLoadingMore() { _isLoadingMore.value = true }
    fun finishLoadingMore() { _isLoadingMore.value = false }
    fun reset() {
        _isLoading.value = false
        _isLoadingMore.value = false
    }
}
