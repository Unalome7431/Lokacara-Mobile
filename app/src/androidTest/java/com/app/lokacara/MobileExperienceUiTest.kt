package com.app.lokacara

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.app.lokacara.model.CertificateData
import com.app.lokacara.model.UpcomingEvent
import com.app.lokacara.ui.components.CertificateCard
import com.app.lokacara.ui.components.UpcomingEventSection
import com.app.lokacara.ui.theme.LokacaraMobileTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MobileExperienceUiTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun upcomingSectionShowsHeroAndSeeAllAction() {
        var seeAllClicked = false
        compose.setContent {
            LokacaraMobileTheme {
                UpcomingEventSection(
                    upcomingEvents = listOf(upcoming(1), upcoming(2)),
                    onEventClick = {},
                    onExploreClick = {},
                    onSeeAll = { seeAllClicked = true }
                )
            }
        }
        compose.onNodeWithText("PALING DEKAT").assertIsDisplayed()
        compose.onNodeWithText("Lihat Semua").performClick()
        assertTrue(seeAllClicked)
    }

    @Test
    fun certificateCardOffersPreviewRetry() {
        var retried = false
        compose.setContent {
            LokacaraMobileTheme {
                CertificateCard(
                    cert = CertificateData("1", eventId = 9, title = "Sertifikat", date = "", time = "", location = "", category = ""),
                    onRetryPreview = { retried = true }
                )
            }
        }
        compose.onNodeWithText("Muat ulang pratinjau").performClick()
        assertTrue(retried)
    }

    private fun upcoming(id: Long) = UpcomingEvent(
        id = id, title = "Event $id", date = "2026-06-22", time = "10:00",
        location = "Surakarta", type = "offline", startEpoch = 2_000 + id
    )
}
