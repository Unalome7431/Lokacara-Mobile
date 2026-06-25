package com.app.lokacara.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.draftDataStore by preferencesDataStore(name = "event_draft")

data class EventDraft(
    val namaEvent: String = "",
    val penyelenggara: String = "",
    val kontak: String = "",
    val waktuMulai: String = "",
    val waktuSelesai: String = "",
    val isOnline: Boolean = true,
    val aplikasiTempat: String = "",
    val alamat: String = "",
    val city: String = "",
    val deskripsi: String = "",
    val kuota: Int = 50,
    val isFreePrice: Boolean = true,
    val priceAmount: String = "",
    val selectedCategoryId: Int? = null,
    val latitude: String = "",
    val longitude: String = "",
    val posterUriString: String = ""
)

class DraftManager(private val context: Context) {
    companion object {
        val NAMA_EVENT = stringPreferencesKey("draft_nama_event")
        val PENYELENGGARA = stringPreferencesKey("draft_penyelenggara")
        val KONTAK = stringPreferencesKey("draft_kontak")
        val WAKTU_MULAI = stringPreferencesKey("draft_waktu_mulai")
        val WAKTU_SELESAI = stringPreferencesKey("draft_waktu_selesai")
        val IS_ONLINE = booleanPreferencesKey("draft_is_online")
        val APLIKASI_TEMPAT = stringPreferencesKey("draft_aplikasi_tempat")
        val ALAMAT = stringPreferencesKey("draft_alamat")
        val CITY = stringPreferencesKey("draft_city")
        val DESKRIPSI = stringPreferencesKey("draft_deskripsi")
        val KUOTA = intPreferencesKey("draft_kuota")
        val IS_FREE_PRICE = booleanPreferencesKey("draft_is_free_price")
        val PRICE_AMOUNT = stringPreferencesKey("draft_price_amount")
        val CATEGORY_ID = intPreferencesKey("draft_category_id")
        val LATITUDE = stringPreferencesKey("draft_latitude")
        val LONGITUDE = stringPreferencesKey("draft_longitude")
        val POSTER_URI = stringPreferencesKey("draft_poster_uri")
        val HAS_DRAFT = booleanPreferencesKey("draft_has")
    }

    val hasDraft: Flow<Boolean> = context.draftDataStore.data.map { prefs ->
        prefs[HAS_DRAFT] ?: false
    }

    suspend fun saveDraft(draft: EventDraft) {
        context.draftDataStore.edit { prefs ->
            prefs[HAS_DRAFT] = true
            prefs[NAMA_EVENT] = draft.namaEvent
            prefs[PENYELENGGARA] = draft.penyelenggara
            prefs[KONTAK] = draft.kontak
            prefs[WAKTU_MULAI] = draft.waktuMulai
            prefs[WAKTU_SELESAI] = draft.waktuSelesai
            prefs[IS_ONLINE] = draft.isOnline
            prefs[APLIKASI_TEMPAT] = draft.aplikasiTempat
            prefs[ALAMAT] = draft.alamat
            prefs[CITY] = draft.city
            prefs[DESKRIPSI] = draft.deskripsi
            prefs[KUOTA] = draft.kuota
            prefs[IS_FREE_PRICE] = draft.isFreePrice
            prefs[PRICE_AMOUNT] = draft.priceAmount
            if (draft.selectedCategoryId != null) prefs[CATEGORY_ID] = draft.selectedCategoryId
            else prefs.remove(CATEGORY_ID)
            prefs[LATITUDE] = draft.latitude
            prefs[LONGITUDE] = draft.longitude
            prefs[POSTER_URI] = draft.posterUriString
        }
    }

    suspend fun loadDraft(): EventDraft? {
        val prefs = context.draftDataStore.data.first()
        if (prefs[HAS_DRAFT] != true) return null
        return EventDraft(
            namaEvent = prefs[NAMA_EVENT] ?: "",
            penyelenggara = prefs[PENYELENGGARA] ?: "",
            kontak = prefs[KONTAK] ?: "",
            waktuMulai = prefs[WAKTU_MULAI] ?: "",
            waktuSelesai = prefs[WAKTU_SELESAI] ?: "",
            isOnline = prefs[IS_ONLINE] ?: true,
            aplikasiTempat = prefs[APLIKASI_TEMPAT] ?: "",
            alamat = prefs[ALAMAT] ?: "",
            city = prefs[CITY] ?: "",
            deskripsi = prefs[DESKRIPSI] ?: "",
            kuota = prefs[KUOTA] ?: 50,
            isFreePrice = prefs[IS_FREE_PRICE] ?: true,
            priceAmount = prefs[PRICE_AMOUNT] ?: "",
            selectedCategoryId = prefs[CATEGORY_ID],
            latitude = prefs[LATITUDE] ?: "",
            longitude = prefs[LONGITUDE] ?: "",
            posterUriString = prefs[POSTER_URI] ?: ""
        )
    }

    suspend fun deleteDraft() {
        context.draftDataStore.edit { prefs ->
            prefs[HAS_DRAFT] = false
        }
    }
}
