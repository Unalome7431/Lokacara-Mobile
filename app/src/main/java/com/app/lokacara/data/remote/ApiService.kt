package com.app.lokacara.data.remote

import com.app.lokacara.data.remote.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── Auth ──
    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @POST("api/auth/logout")
    suspend fun logout(): MessageResponse

    @POST("api/auth/refresh")
    suspend fun refreshToken(): RefreshTokenResponse

    @POST("api/auth/password/change")
    suspend fun changePassword(@Body body: Map<String, String>): MessageResponse

    @POST("api/auth/password/email")
    suspend fun forgotPassword(@Body body: Map<String, String>): MessageResponse

    @POST("api/auth/password/reset")
    suspend fun resetPassword(@Body body: Map<String, String>): MessageResponse

    // ── Discovery ──
    @GET("api/events/feed")
    suspend fun getFeedEvents(): EventListResponse

    @GET("api/events/search")
    suspend fun searchEvents(
        @Query("keyword") keyword: String? = null,
        @Query("category_id") categoryId: Int? = null,
        @Query("page") page: Int = 1
    ): PaginatedEventsResponse

    @GET("api/events/{event}")
    suspend fun getEventDetail(@Path("event") eventId: Long): EventDetailResponse

    @GET("api/categories")
    suspend fun getCategories(): CategoryListResponse

    @GET("api/locations")
    suspend fun getLocations(): LocationListResponse

    // ── Profile ──
    @GET("api/user")
    suspend fun getCurrentUser(): UserDto

    @DELETE("api/user")
    suspend fun deleteAccount(): MessageResponse

    @GET("api/profile")
    suspend fun getProfile(): ProfileResponse

    @PATCH("api/profile")
    suspend fun updateProfile(@Body body: Map<String, String>): ProfileResponse

    @Multipart
    @POST("api/profile/avatar")
    suspend fun uploadAvatar(@Part avatar: MultipartBody.Part): ProfileResponse

    // ── Participant ──
    @GET("api/dashboard")
    suspend fun getDashboard(): DashboardResponse

    @POST("api/events/{event}/join")
    suspend fun joinEvent(@Path("event") eventId: Long): MessageResponse

    @DELETE("api/events/{event}/join")
    suspend fun leaveEvent(@Path("event") eventId: Long): MessageResponse

    @GET("api/events/{event}/attendance/qr")
    suspend fun getQrTicket(@Path("event") eventId: Long): QrTicketResponse

    @POST("api/events/{event}/report")
    suspend fun reportEvent(
        @Path("event") eventId: Long,
        @Body body: Map<String, String>
    ): MessageResponse

    @GET("api/events/{event}/certificate")
    @Streaming
    suspend fun downloadCertificate(@Path("event") eventId: Long): ResponseBody

    // ── Notifications ──
    @GET("api/notifications")
    suspend fun getNotifications(): NotificationListResponse

    // ── Bookmarks ──
    @GET("api/bookmarks")
    suspend fun getBookmarks(): BookmarkListResponse

    // ── Config ──
    @GET("api/config/tabs")
    suspend fun getConfigTabs(): ConfigTabsResponse

    // ── Organizer ──
    @GET("api/organizer/events")
    suspend fun getMyEvents(@Query("page") page: Int = 1): PaginatedEventsResponse

    @Multipart
    @POST("api/organizer/events")
    suspend fun createEvent(
        @Part("title") title: RequestBody,
        @Part("category_id") categoryId: RequestBody?,
        @Part("description") description: RequestBody,
        @Part("type") type: RequestBody,
        @Part("location_name") locationName: RequestBody?,
        @Part("address") address: RequestBody?,
        @Part("latitude") latitude: RequestBody?,
        @Part("longitude") longitude: RequestBody?,
        @Part("platform_name") platformName: RequestBody?,
        @Part("link") link: RequestBody?,
        @Part("start_datetime") startDatetime: RequestBody,
        @Part("end_datetime") endDatetime: RequestBody,
        @Part("capacity") capacity: RequestBody?,
        @Part poster: MultipartBody.Part?
    ): CreateEventResponse

    @Multipart
    @POST("api/organizer/events/{event}")
    suspend fun updateEvent(
        @Path("event") eventId: Long,
        @Part("title") title: RequestBody,
        @Part("category_id") categoryId: RequestBody?,
        @Part("description") description: RequestBody,
        @Part("type") type: RequestBody,
        @Part("location_name") locationName: RequestBody?,
        @Part("address") address: RequestBody?,
        @Part("latitude") latitude: RequestBody?,
        @Part("longitude") longitude: RequestBody?,
        @Part("platform_name") platformName: RequestBody?,
        @Part("link") link: RequestBody?,
        @Part("start_datetime") startDatetime: RequestBody,
        @Part("end_datetime") endDatetime: RequestBody,
        @Part("capacity") capacity: RequestBody?,
        @Part poster: MultipartBody.Part?
    ): CreateEventResponse

    @DELETE("api/organizer/events/{event}")
    suspend fun deleteEvent(@Path("event") eventId: Long): MessageResponse

    @GET("api/organizer/events/{event}/attendees")
    suspend fun getAttendees(
        @Path("event") eventId: Long,
        @Query("page") page: Int = 1
    ): AttendeesResponse

    @POST("api/organizer/events/{event}/attendance/scan")
    suspend fun scanQr(
        @Path("event") eventId: Long,
        @Body body: ScanRequest
    ): ScanResponse

    @PATCH("api/organizer/events/{event}/attendance/{registration}/toggle")
    suspend fun toggleAttendance(
        @Path("event") eventId: Long,
        @Path("registration") registrationId: Long
    ): ScanResponse

    @POST("api/organizer/events/{event}/reminders")
    suspend fun sendReminders(@Path("event") eventId: Long): MessageResponse

    // ── Certificates (Organizer) ──
    @Multipart
    @POST("api/organizer/events/{event}/certificates/template")
    suspend fun uploadCertificateTemplate(
        @Path("event") eventId: Long,
        @Part template: MultipartBody.Part
    ): MessageResponse

    @POST("api/organizer/events/{event}/certificates/distribute")
    suspend fun distributeCertificates(
        @Path("event") eventId: Long,
        @Body body: Map<String, Any>
    ): MessageResponse
}
