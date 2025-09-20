package com.miapp.appdivisaskotlinbasico.Retrofit.modelos

import com.google.gson.annotations.SerializedName

// Data class para el objeto "dolar" que contiene el campo "valor"
data class DolarInfo(
    @SerializedName("valor") val valor: Double
    // Puedes añadir otros campos de "dolar" si los necesitas, como "fecha", "nombre", etc.
    // @SerializedName("nombre") val nombre: String,
    // @SerializedName("fecha") val fecha: String
)

// Data class principal que espera un campo "dolar" de tipo DolarInfo
// Gson ignorará los otros campos del JSON principal (uf, ivp, euro, etc.)
// si no están definidos aquí.
data class ApiResponse(
    @SerializedName("dolar") val dolar: DolarInfo
    // Si necesitas la fecha general de la API o la versión, también podrías añadirlas:
    // @SerializedName("fecha") val fechaApi: String,
    // @SerializedName("version") val versionApi: String
)
