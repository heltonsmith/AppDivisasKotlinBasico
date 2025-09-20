package com.miapp.appdivisaskotlinbasico.Retrofit.modelos

data class ApiResponse(
    // Los campos de nivel superior que SÍ quieres capturar, como la fecha general o versión de la API (opcional)
    val version: String,
    val autor: String,
    val fecha: String, // Renombrado para evitar confusión con la fecha del dólar

    // El campo que REALMENTE nos interesa: el objeto "dolar"
    val dolar: DolarInfo
)
