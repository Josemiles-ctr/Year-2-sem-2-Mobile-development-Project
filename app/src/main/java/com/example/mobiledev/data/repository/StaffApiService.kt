package com.example.mobiledev.data.repository

import com.example.mobiledev.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface StaffApiService {
    @POST("api/hospital/staff/invite")
    suspend fun inviteStaff(@Body request: InviteStaffRequest): Response<Unit>

    @GET("api/hospital/staff")
    suspend fun getStaffList(): List<StaffMember>

    @PUT("api/hospital/staff/{id}")
    suspend fun updateStaff(
        @Path("id") id: String,
        @Body request: UpdateStaffRequest
    ): Response<Unit>

    @DELETE("api/hospital/staff/{id}")
    suspend fun removeStaff(@Path("id") id: String): Response<Unit>

    @GET("api/hospital/staff/invitations")
    suspend fun getPendingInvitations(): List<StaffInvitation>

    @POST("api/hospital/staff/invitations/{id}/resend")
    suspend fun resendInvitation(@Path("id") id: String): Response<Unit>

    @DELETE("api/hospital/staff/invitations/{id}")
    suspend fun cancelInvitation(@Path("id") id: String): Response<Unit>
}
