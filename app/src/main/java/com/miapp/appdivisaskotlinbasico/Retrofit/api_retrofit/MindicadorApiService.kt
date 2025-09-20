package com.miapp.appdivisaskotlinbasico.Retrofit.api_retrofit

import retrofit2.Response
import retrofit2.http.GET

interface MindicadorApiService {
    @GET("api")
    suspend fun getIndicadores(): Response<ApiResponse> // Ahora espera nuestra ApiResponse simplificada
}