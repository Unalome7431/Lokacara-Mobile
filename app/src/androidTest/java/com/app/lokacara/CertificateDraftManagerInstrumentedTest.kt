package com.app.lokacara

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.lokacara.data.CertificateDraft
import com.app.lokacara.data.CertificateDraftManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CertificateDraftManagerInstrumentedTest {
    @Test
    fun draftPersistsAndRemainsIsolatedByAccountAndEvent() = runBlocking {
        val manager = CertificateDraftManager(ApplicationProvider.getApplicationContext())
        manager.save(701, 801, CertificateDraft(fontFamily = "Playfair", distributionStatus = "distributed"))

        assertEquals("Playfair", manager.load(701, 801)?.fontFamily)
        assertEquals("distributed", manager.load(701, 801)?.distributionStatus)
        assertNull(manager.load(702, 801))
        assertNull(manager.load(701, 802))
    }
}
