package com.miapp.appdivisaskotlinbasico.Retrofit.modelos.interfaceRetrofit

import com.miapp.appdivisaskotlinbasico.Retrofit.modelos.ApiResponse
import retrofit2.Response
import retrofit2.http.GET

interface MindicadorApiService {
    @GET("api")
    suspend fun getIndicadores(): Response<ApiResponse> // Ahora espera nuestra ApiResponse simplificada
}