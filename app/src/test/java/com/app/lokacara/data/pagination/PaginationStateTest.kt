package com.app.lokacara.data.pagination

import com.app.lokacara.data.mergeEventsById
import com.app.lokacara.model.Event
import org.junit.Assert.*
import org.junit.Test

class PaginationStateTest {

    @Test
    fun `PaginationController hasMorePages returns true when current page lt total pages`() {
        val controller = PaginationController(initialPage = 1, initialTotalPages = 5)
        assertTrue(controller.hasMorePages)
    }

    @Test
    fun `PaginationController hasMorePages returns false when on last page`() {
        val controller = PaginationController(initialPage = 5, initialTotalPages = 5)
        assertFalse(controller.hasMorePages)
    }

    @Test
    fun `PaginationController nextPage returns incremented page`() {
        val controller = PaginationController(initialPage = 1, initialTotalPages = 5)
        assertEquals(2, controller.nextPage())
    }

    @Test
    fun `PaginationController updateFromResponse updates state`() {
        val controller = PaginationController()
        controller.updateFromResponse(currentPage = 2, lastPage = 10)
        assertEquals(2, controller.currentPage.value)
        assertEquals(10, controller.totalPages.value)
        assertTrue(controller.hasMorePages)
    }

    @Test
    fun `PaginationController reset restores defaults`() {
        val controller = PaginationController()
        controller.updateFromResponse(currentPage = 3, lastPage = 5)
        controller.reset()
        assertEquals(1, controller.currentPage.value)
        assertEquals(1, controller.totalPages.value)
        assertFalse(controller.hasMorePages)
    }

    @Test
    fun `PaginationLoadingState starts and finishes loading correctly`() {
        val state = PaginationLoadingState()
        assertFalse(state.isLoading.value)
        state.startLoading()
        assertTrue(state.isLoading.value)
        state.finishLoading()
        assertFalse(state.isLoading.value)
    }

    @Test
    fun `PaginationLoadingState starts and finishes loadingMore correctly`() {
        val state = PaginationLoadingState()
        assertFalse(state.isLoadingMore.value)
        state.startLoadingMore()
        assertTrue(state.isLoadingMore.value)
        state.finishLoadingMore()
        assertFalse(state.isLoadingMore.value)
    }

    @Test
    fun `PaginationLoadingState reset clears both states`() {
        val state = PaginationLoadingState()
        state.startLoading()
        state.startLoadingMore()
        state.reset()
        assertFalse(state.isLoading.value)
        assertFalse(state.isLoadingMore.value)
    }

    @Test
    fun `PaginationGuard canLoadMore when not in flight`() {
        val guard = PaginationGuard { false }
        assertTrue(guard.canLoadMore())
    }

    @Test
    fun `PaginationGuard cannotLoadMore when in flight`() {
        val guard = PaginationGuard { true }
        assertFalse(guard.canLoadMore())
    }

    @Test
    fun `mergeEventsById merges and deduplicates by id`() {
        val existing = listOf(
            Event(id = 1, title = "A", description = "", date = "", location = "", price = "", category = ""),
            Event(id = 2, title = "B", description = "", date = "", location = "", price = "", category = "")
        )
        val incoming = listOf(
            Event(id = 2, title = "B Updated", description = "", date = "", location = "", price = "", category = ""),
            Event(id = 3, title = "C", description = "", date = "", location = "", price = "", category = "")
        )
        val merged = mergeEventsById(existing, incoming)
        assertEquals(3, merged.size)
        assertTrue(merged.any { it.id == 1L })
        assertTrue(merged.any { it.id == 2L })
        assertTrue(merged.any { it.id == 3L })
    }

    @Test
    fun `mergeEventsById preserves existing when id overlaps`() {
        val existing = listOf(
            Event(id = 1, title = "Keep Me", description = "", date = "", location = "", price = "", category = "")
        )
        val incoming = listOf(
            Event(id = 1, title = "New Title", description = "", date = "", location = "", price = "", category = "")
        )
        val merged = mergeEventsById(existing, incoming)
        assertEquals(1, merged.size)
        assertEquals("Keep Me", merged.first().title)
    }
}
