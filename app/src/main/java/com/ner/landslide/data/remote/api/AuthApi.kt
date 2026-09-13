package com.ner.landslide.data.remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

data class RegisterRequestDto(
    val name: String,
    val email: String,
    val password: String,
    val role: String = "CITIZEN"
)

data class LoginRequestDto(
    val email: String,
    val password: String
)

data class UserDto(
    val uid: String,
    val name: String,
    val email: String,
    val role: String
)

data class AuthResponseDto(
    val success: Boolean = true,
    val token: String,
    val user: UserDto,
    val message: String
)

interface AuthApi {

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<AuthResponseDto>

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<AuthResponseDto>

    @GET("api/v1/auth/verify")
    suspend fun verify(@Header("Authorization") authorization: String): Response<UserDto>
}
