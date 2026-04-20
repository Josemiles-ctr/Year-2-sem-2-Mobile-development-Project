package com.example.mobiledev.data.services

import com.example.mobiledev.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface EmergencyApiService {
    @GET("api/hospital/requests")
    suspend fun getEmergencyRequests(
        @Query("status") status: String? = null,
        @Query("date_from") dateFrom: Long? = null,
        @Query("date_to") dateTo: Long? = null,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): List<EmergencyRequest>

    @PUT("api/hospital/emergencies/{id}/status")
    suspend fun updateEmergencyStatus(
        @Path("id") id: String,
        @Body status: EmergencyStatus
    ): Response<Unit>

    @GET("api/hospital/ambulances")
    suspend fun getAmbulances(): List<Ambulance>

    @POST("api/hospital/emergencies/{requestId}/assign/{ambulanceId}")
    suspend fun assignAmbulance(
        @Path("requestId") requestId: String,
        @Path("ambulanceId") ambulanceId: String
    ): Response<Unit>
}
