package com.miapp.appdivisaskotlinbasico.Retrofit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.miapp.appdivisaskotlinbasico.ActivitysNormales.AsyncActivity
import com.miapp.appdivisaskotlinbasico.ActivitysNormales.MainActivity
import com.miapp.appdivisaskotlinbasico.R
import com.miapp.appdivisaskotlinbasico.Retrofit.api_retrofit.MindicadorApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.DecimalFormat

class RetrofitActivity : AppCompatActivity() {

    private lateinit var etClp3: EditText // Declaración de la variable etClp3 como una propiedad de la clase
    private lateinit var btCalcular3: Button // Declaración de la variable btCalcular3 como una propiedad de la clase
    private lateinit var tvResultado3: TextView // Declaración de la variable tvResultado3 como una propiedad de la clase
    private lateinit var progressBar3: ProgressBar // Declaración de la variable progressBar3 como una propiedad de la clase

    private lateinit var btIrMain3: Button // Declaración de la variable btIrMain3 como una propiedad de la clase
    private lateinit var btIrAsync3: Button // Declaración de la variable btIrAsync3 como una propiedad de la clase
    private lateinit var btIrRetrofit3: Button // Declaración de la variable btIrRetrofit3 como una propiedad de la clase

    // Instancia de Retrofit y el servicio se crearán aquí
    private var apiService: MindicadorApiService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_retrofit)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etClp3 = findViewById(R.id.etClp3) // Inicialización de la variable etClp3
        btCalcular3 = findViewById(R.id.btCalcular3) // Inicialización de la variable btCalcular3
        tvResultado3 = findViewById(R.id.tvResultado3) // Inicialización de la variable tvResultado3
        progressBar3 = findViewById(R.id.progressBar3) // Inicialización de la variable progressBar3
        btIrAsync3 = findViewById(R.id.btIrAsync3) // Inicialización de la variable tbIrAsync3

        // 1. Establece el título de la ActionBar
        supportActionBar?.title = "Conversor RetrofitActivity" // El texto que desees

        // Opcional: Habilitar el botón de "hacia atrás" o "home" en la ActionBar
        // Esto mostrará una flecha de regreso si esta actividad no es la principal en la tarea.
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Inicializar Retrofit y el servicio
        setupRetrofit()
        btCalcular3.setOnClickListener {
            val montoClpString = etClp3.text.toString()
            if (montoClpString.isNotEmpty()) {
                try {
                    val montoClp = montoClpString.toDouble()
                    realizarConversionEnRetrofitActivity(montoClp)
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Por favor, ingresa un monto válido", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Por favor, ingresa un monto", Toast.LENGTH_SHORT).show()
            }
        }

        cambiarActividadesSalir() // Llama a la función para cambiar las actividades

    } // Fin del onCreate

    // Función para realizar la conversión en RetrofitActivity
    private fun setupRetrofit() {
        try {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://mindicador.cl/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            apiService = retrofit.create(MindicadorApiService::class.java)
        } catch (e: Exception) {
            Log.e("RetrofitSetup", "Error al inicializar Retrofit", e)
            Toast.makeText(this, "Error al inicializar el servicio de red.", Toast.LENGTH_LONG).show()
            // Podrías deshabilitar el botón de calcular si esto falla
            btCalcular3.isEnabled = false
        }
    } // Fin de setupRetrofit

    // Función para obtener el valor del dólar desde la API
    private fun realizarConversionEnRetrofitActivity(montoClp: Double) {
        if (apiService == null) { // Verificar si el servicio API está inicializado
            tvResultado3.text = "Error: Servicio API no inicializado." // Mostrar mensaje de error
            Toast.makeText(this, "Servicio API no disponible.", Toast.LENGTH_SHORT).show() // Mostrar mensaje de error
            return // Salir de la función si el servicio API no está disponible
        }

        progressBar3.visibility = View.VISIBLE // Mostrar la barra de progreso
        tvResultado3.text = "Resultado:" // Limpiar el TextView

        // Corrutina más simple usando lifecycleScope y cambiando de contexto para la red
        lifecycleScope.launch {
            Log.d("RetrofitActivityLog", "Iniciando conversión en RetrofitActivity") // Log para depuración
            try {
                val valorDolar = obtenerValorDolarDesdeApi() // Llama a la función suspend

                Log.d("RetrofitActivityLog", "Valor del dólar obtenido: $valorDolar") // Log para depuración

                // Verificar si el valor del dólar es válido antes de continuar
                if (valorDolar != null && valorDolar > 0) {
                    val montoUsd = montoClp / valorDolar // Calcular el monto en dólares
                    val df = DecimalFormat("#.##") // Formatear el resultado con dos decimales
                    tvResultado3.text = "Resultado: ${df.format(montoUsd)} USD" // Mostrar el resultado
                } else {
                    tvResultado3.text = "Resultado: No se pudo obtener el valor del dólar." // Mostrar mensaje de error
                }

            } catch (e: Exception) { // Capturar cualquier excepción
                tvResultado3.text = "Resultado: Ocurrió un error: ${e.message}" // Mostrar mensaje de error
                Log.e("RetrofitActivityLog", "Error durante la conversión", e) // Log para depuración
            } finally {
                progressBar3.visibility = View.GONE // Ocultar la barra de progreso
            }
        } // Fin de launch
    } // Fin de realizarConversionEnRetrofitActivity


    /**
     * Función suspend que utiliza la instancia de apiService de esta Activity.
     */
    private suspend fun obtenerValorDolarDesdeApi(): Double? {
        var valorDolar: Double? = null // Variable para almacenar el valor del dólar

        // Asegurarse de que apiService no sea nulo, aunque ya se verifica antes de llamar
        val service = apiService ?: return null

        return withContext(Dispatchers.IO) { // Cambiar el contexto a IO
            Log.d("RetrofitActivityLog", "Obteniendo valor del dólar desde API...") // Log para depuración
            try {
                val response = service.getIndicadores() // Usa la instancia de servicio de la Activity
                if (response.isSuccessful) { // Verificar si la respuesta es exitosa
                    Log.d("RetrofitActivityLog", "Respuesta exitosa: ${response.body()}") // Log para depuración
                    valorDolar =
                        response.body()?.dolar?.valor // Extraer el valor del dólar de la respuesta
                    Log.d("RetrofitActivityLog", "Valor del dólar parseado: $valorDolar") // Log para depuración
                    valorDolar
                } else {
                    Log.e("RetrofitActivityLog", "Error API: ${response.code()} - ${response.message()}") // Log para depuración
                    null // Devolver null en caso de error
                }
            } catch (e: Exception) {
                Log.e("RetrofitActivityLog", "Excepción al obtener valor dólar desde API", e) // Log para depuración
                null // Devolver null en caso de excepción
            } finally {
                valorDolar
            }
        } // Fin de withContext
    } // Fin de obtenerValorDolarDesdeApi

    fun cambiarActividadesSalir(){
        // Botones para navegar a las otras pantallas
        // Ir a la actividad MainActivity
        btIrMain3 = findViewById(R.id.btIrMain3) // Inicialización de la variable btIrAsync3
        btIrMain3.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } // FIN del botón btIrMain3

        // Ir a la actividad AsyncActivity
        btIrAsync3 = findViewById(R.id.btIrAsync3) // Inicialización de la variable btIrAsync3
        btIrAsync3.setOnClickListener {
            val intent = Intent(this, AsyncActivity::class.java)
            startActivity(intent)
        } // FIN del botón btIrAsync3

        // Ir a la actividad RetrofitActivity
        btIrRetrofit3 = findViewById(R.id.btIrRetrofit3) // Inicialización de la variable btIrAsync3
        btIrRetrofit3.setOnClickListener {
            val intent = Intent(this, RetrofitActivity::class.java)
            startActivity(intent)
        } // FIN del botón btIrRetrofit3

        // Cerrar App
        val btSalir3 = findViewById<Button>(R.id.btSalir3)
        btSalir3.setOnClickListener {
            finishAffinity()
        } // FIN del botón btSalir3
    } // Fin de cambiarActividadesSalir

} // Fin de la clase RetrofitActivity