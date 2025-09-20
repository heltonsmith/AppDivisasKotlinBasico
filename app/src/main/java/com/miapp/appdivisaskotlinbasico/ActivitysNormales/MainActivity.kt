package com.miapp.appdivisaskotlinbasico.ActivitysNormales

import android.content.Intent
import android.os.Bundle
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
import com.miapp.appdivisaskotlinbasico.R
import com.miapp.appdivisaskotlinbasico.Retrofit.RetrofitActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {

    private lateinit var etClp: EditText // Declaración de la variable etClp como una propiedad de la clase
    private lateinit var btCalcular: Button // Declaración de la variable btCalcular como una propiedad de la clase
    private lateinit var tvResultado: TextView // Declaración de la variable tvResultado como una propiedad de la clase
    private lateinit var progressBar: ProgressBar // Declaración de la variable progressBar como una propiedad de la clase

    private lateinit var btIrMain: Button // Declaración de la variable btIrMain como una propiedad de la clase
    private lateinit var btIrAsync: Button // Declaración de la variable btIrAsync como una propiedad de la clase
    private lateinit var btIrRetrofit: Button // Declaración de la variable btIrRetrofit como una propiedad de la clase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etClp = findViewById(R.id.etClp) // Inicialización de la variable etClp
        btCalcular = findViewById(R.id.btCalcular) // Inicialización de la variable btCalcular
        tvResultado = findViewById(R.id.tvResultado) // Inicialización de la variable tvResultado
        progressBar = findViewById(R.id.progressBar) // Inicialización de la variable progressBar

        // 1. Establece el título de la ActionBar
        supportActionBar?.title = "Conversor MainActivity" // El texto que desees

        // Opcional: Habilitar el botón de "hacia atrás" o "home" en la ActionBar
        // Esto mostrará una flecha de regreso si esta actividad no es la principal en la tarea.
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        btCalcular.setOnClickListener { // Agregar un listener al botón btCalcular
            val montoClpString = etClp.text.toString() // Obtener el texto del EditText como cadena
            if (montoClpString.isNotEmpty()) { // Verificar si la cadena no está vacía
                try {
                    val montoClp = montoClpString.toDouble() // Intentar convertir la cadena a un número double
                    obtenerValorDolarYConvertirManualmente(montoClp) // Llamar a la función para obtener el valor del dólar y convertir
                } catch (e: NumberFormatException) {
                    // Manejar el caso en el que la cadena no se puede convertir a un número
                    // Por ejemplo, mostrar un mensaje de error al usuario
                    Toast.makeText(this, "Por favor, ingresa un monto válido", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Manejar el caso en el que la cadena está vacía
                // Por ejemplo, mostrar un mensaje de error al usuario
                Toast.makeText(this, "Por favor, ingresa un monto", Toast.LENGTH_SHORT).show()
            }
        } // FIN del botón btCalcular

        cambiarActividadesSalir() // Llama a la función para cambiar las actividades

    } // Fin del onCreate

    // Función para obtener el valor del dólar y convertir
    private fun obtenerValorDolarYConvertirManualmente(montoClp: Double) {
        progressBar.visibility = View.VISIBLE // Mostrar la barra de progreso
        tvResultado.text = "Resultado:" // Limpiar el TextView

        // Corrutina más simple usando lifecycleScope y cambiando de contexto para la red
        lifecycleScope.launch {
            try {
                // Operación de red en un hilo de fondo (IO)
                val jsonResponse = withContext(Dispatchers.IO) { // Cambiar el contexto a IO
                    fetchIndicadoresJson() // Llamar a la función para obtener el JSON
                }

                // Parsear JSON y actualizar UI en el hilo principal
                if (jsonResponse != null) {
                    val jsonObject = JSONObject(jsonResponse) // Parsear el JSON
                    if (jsonObject.has("dolar")) { // Verificar si el objeto "dolar" existe
                        val dolarObject = jsonObject.getJSONObject("dolar") // Obtener el objeto "dolar"
                        val valorDolar = dolarObject.getDouble("valor") // Obtener el valor del dólar

                        if (valorDolar > 0) { // Verificar que el valor del dólar sea válido
                            val montoUsd = montoClp / valorDolar // Calcular el monto en dólares
                            val df = DecimalFormat("#.##") // Formatear el resultado con dos decimales
                            tvResultado.text = "Resultado: ${df.format(montoUsd)} USD" // Mostrar el resultado
                        } else {
                            tvResultado.text = "Resultado: Valor del dólar no válido." // Mostrar mensaje de error
                        }
                    } else {
                        tvResultado.text = "Resultado: No se encontró el indicador 'dolar'." // Mostrar mensaje de error
                    }
                } else {
                    tvResultado.text = "Resultado: No se pudo obtener la información." // Mostrar mensaje de error
                }

            } catch (e: IOException) {
                tvResultado.text = "Resultado: Error de conexión. Verifica tu internet." // Mostrar mensaje de error
                e.printStackTrace()
            } catch (e: JSONException) {
                tvResultado.text = "Resultado: Error al parsear los datos." // Mostrar mensaje de error
                e.printStackTrace()
            } catch (e: Exception) {
                tvResultado.text = "Resultado: Ocurrió un error inesperado." // Mostrar mensaje de error
                e.printStackTrace()
            } finally {
                progressBar.visibility = View.GONE // Ocultar la barra de progreso
            }
        }
    } // Fin de obtenerValorDolarYConvertirManualmente


    // Esta función se ejecutará en un hilo de fondo gracias a withContext(Dispatchers.IO)
    @Throws(IOException::class) // Anotación para indicar que puede lanzar IOException
    private fun fetchIndicadoresJson(): String? {
        val url = URL("https://mindicador.cl/api") // URL del API
        val connection = url.openConnection() as HttpURLConnection // Abrir la conexión
        connection.requestMethod = "GET" // Método de solicitud
        connection.connectTimeout = 10000 // 10 segundos de timeout
        connection.readTimeout = 10000    // 10 segundos de timeout

        try { // Intentar obtener la respuesta
            val inputStream = connection.inputStream // Obtener el flujo de entrada
            val reader = BufferedReader(InputStreamReader(inputStream)) // Crear un lector de flujo de entrada
            val stringBuilder = StringBuilder() // Crear un StringBuilder para construir la respuesta
            var line: String? // Variable para almacenar cada línea de la respuesta
            while (reader.readLine().also { line = it } != null) { // Leer cada línea de la respuesta
                stringBuilder.append(line) // Agregar la línea al StringBuilder
            }
            reader.close() // Cerrar el lector
            inputStream.close() // Cerrar el flujo de entrada
            return stringBuilder.toString() // Devolver la respuesta como cadena
        } finally {
            connection.disconnect() // Desconectar la conexión
        }
    } // Fin de fetchIndicadoresJson

    // Función para cambiar las actividades
    private fun cambiarActividadesSalir() {
        // Botones para navegar a las otras pantallas

        // Ir a la actividad MainActivity
        btIrMain = findViewById(R.id.btIrMain) // Inicialización de la variable btIrMain
        btIrMain.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } // FIN del botón btIrMain

        // Ir a la actividad AsyncActivity
        btIrAsync = findViewById(R.id.btIrAsync) // Inicialización de la variable btIrAsync
        btIrAsync.setOnClickListener {
            val intent = Intent(this, AsyncActivity::class.java)
            startActivity(intent)
        } // FIN del botón btIrAsync

        // Ir a la actividad RetrofitActivity
        btIrRetrofit = findViewById(R.id.btIrRetrofit) // Inicialización de la variable btIrRetrofit
        btIrRetrofit.setOnClickListener {
            val intent = Intent(this, RetrofitActivity::class.java)
            startActivity(intent)
        } // FIN del botón btIrRetrofit

        // Cerrar App
        val btSalir = findViewById<Button>(R.id.btSalir)
        btSalir.setOnClickListener {
            finishAffinity()
        } // FIN del botón btSalir
    }

} // Fin de la clase MainActivity