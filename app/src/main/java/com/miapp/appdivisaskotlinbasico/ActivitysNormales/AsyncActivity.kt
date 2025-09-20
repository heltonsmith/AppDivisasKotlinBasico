package com.miapp.appdivisaskotlinbasico.ActivitysNormales

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
import com.miapp.appdivisaskotlinbasico.R
import com.miapp.appdivisaskotlinbasico.Retrofit.RetrofitActivity
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat

class AsyncActivity : AppCompatActivity() {

    private lateinit var etClp2: EditText // Declaración de la variable etClp2 como una propiedad de la clase
    private lateinit var btCalcular2: Button // Declaración de la variable btCalcular2 como una propiedad de la clase
    private lateinit var tvResultado2: TextView // Declaración de la variable tvResultado2 como una propiedad de la clase
    private lateinit var progressBar2: ProgressBar // Declaración de la variable progressBar2 como una propiedad de la clase

    private lateinit var btIrMain2: Button // Declaración de la variable btIrMain2 como una propiedad de la clase
    private lateinit var btIrAsync2: Button // Declaración de la variable btIrAsync2 como una propiedad de la clase
    private lateinit var btIrRetrofit2: Button // Declaración de la variable btIrRetrofit2 como una propiedad de la clase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_async)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etClp2 = findViewById(R.id.etClp2) // Inicialización de la variable etClp2
        btCalcular2 = findViewById(R.id.btCalcular2) // Inicialización de la variable btCalcular2
        tvResultado2 = findViewById(R.id.tvResultado2) // Inicialización de la variable tvResultado2
        progressBar2 = findViewById(R.id.progressBar2) // Inicialización de la variable progressBar2
        btIrAsync2 = findViewById(R.id.btIrAsync2) // Inicialización de la variable tbIrAsync

        // 1. Establece el título de la ActionBar
        supportActionBar?.title = "Conversor AsyncActivity" // El texto que desees

        // Opcional: Habilitar el botón de "hacia atrás" o "home" en la ActionBar
        // Esto mostrará una flecha de regreso si esta actividad no es la principal en la tarea.
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        btCalcular2.setOnClickListener {
            val montoClpString = etClp2.text.toString() // Obtener el texto del EditText como cadena
            if (montoClpString.isNotEmpty()) { // Verificar si la cadena no está vacía
                try {
                    val montoClp = montoClpString.toDouble() // Intentar convertir la cadena a un número double
                    // Llamamos a la nueva función que usa async internamente
                    realizarConversionConAsync(montoClp) // Llamar a la función para obtener el valor del dólar y convertir
                } catch (e: NumberFormatException) { // Manejar el caso en el que la cadena no se puede convertir a un número
                    Toast.makeText(this, "Por favor, ingresa un monto válido", Toast.LENGTH_SHORT).show() // Mostrar mensaje de error
                }
            } else { // Manejar el caso en el que la cadena está vacía
                Toast.makeText(this, "Por favor, ingresa un monto", Toast.LENGTH_SHORT).show() // Mostrar mensaje de error
            }
        } // FIN del botón btCalcular

        cambiarActividadesSalir() // Llama a la función para cambiar las actividades

    } // Fin del onCreate

    // Nueva función que orquesta la obtención del valor del dólar usando async
    private fun realizarConversionConAsync(montoClp: Double) {
        progressBar2.visibility = View.VISIBLE // Mostrar la barra de progreso
        tvResultado2.text = "Resultado:" // Limpiar el TextView

        // Iniciamos una corrutina con launch para manejar el ciclo de vida y la UI
        lifecycleScope.launch {
            Log.d("AsyncTest", "Iniciando launch para realizarConversionConAsync")
            try {
                // Paso 1: Llamar a la función que usa async para obtener el valor del dólar.
                // Esta función devolverá un Deferred<Double?>
                Log.d("AsyncTest", "Llamando a obtenerValorDolarAsync()...") // Log para rastrear
                val deferredValorDolar: Deferred<Double?> = obtenerValorDolarAsync() // Llamada a la función async

                // Aquí podrías hacer otras cosas si fuera necesario, mientras 'obtenerValorDolarAsync'
                // está trabajando en segundo plano para obtener el JSON y parsearlo.
                // Por ejemplo: Log.d("AsyncTest", "Haciendo otro trabajo mientras esperamos el valor del dólar...")

                // Paso 2: Esperar el resultado del Deferred usando await()
                // La ejecución de esta corrutina (del launch) se SUSPENDERÁ aquí hasta que
                // 'deferredValorDolar' complete su cómputo y tenga un valor.
                Log.d("AsyncTest", "Esperando el resultado con await()...") // Log para rastrear
                val valorDolar = deferredValorDolar.await() // Esperar el resultado de async
                Log.d("AsyncTest", "Resultado obtenido de await(): $valorDolar") // Log para rastrear

                // Paso 3: Usar el valor obtenido
                if (valorDolar != null && valorDolar > 0) { // Verificar que el valor del dólar sea válido
                    val montoUsd = montoClp / valorDolar // Calcular el monto en dólares
                    val df = DecimalFormat("#.##") // Formatear el resultado con dos decimales
                    tvResultado2.text = "Resultado: ${df.format(montoUsd)} USD" // Mostrar el resultado
                } else if (valorDolar == null) { // Manejar el caso en el que el valor del dólar sea null
                    tvResultado2.text = "Resultado: No se pudo obtener el valor del dólar (error de red o parseo)." // Mostrar mensaje de error
                } else { // Manejar el caso en el que el valor del dólar no sea válido
                    tvResultado2.text = "Resultado: Valor del dólar no válido." // Mostrar mensaje de error
                }

            } catch (e: Exception) { // Captura excepciones que podrían ocurrir durante await() o en el launch
                tvResultado2.text = "Resultado: Ocurrió un error inesperado durante la conversión." // Mostrar mensaje de error
                Log.e("AsyncTest", "Error en realizarConversionConAsync", e) // Registrar la excepción para depuración
                e.printStackTrace() // Imprimir la pila de llamadas para depuración
            } finally {
                progressBar2.visibility = View.GONE // Ocultar la barra de progreso
                Log.d("AsyncTest", "Fin de realizarConversionConAsync") // Log para rastrear
            }
        }
        Log.d("AsyncTest", "realizarConversionConAsync (función externa al launch) ha terminado su ejecución inicial.") // Log para rastrear
    } // Fin de realizarConversionConAsync

    /**
     * Función que utiliza async para obtener el JSON de la API y extraer el valor del dólar.
     * Devuelve un Deferred que eventualmente contendrá el valor del dólar como Double,
     * o null si ocurre un error.
     */
    private fun obtenerValorDolarAsync(): Deferred<Double?> {
        // Usamos async porque queremos que esta operación devuelva un resultado (el valor del dólar)
        // de forma asíncrona.
        // El async se ejecuta dentro del CoroutineContext del llamador (lifecycleScope en este caso),
        // pero cambiamos el Dispatcher a IO para la operación de red.
        return lifecycleScope.async(Dispatchers.IO) { // Cambiar el contexto a IO
            Log.d("AsyncTest", "Dentro de async en obtenerValorDolarAsync() en ${Thread.currentThread().name}") // Log para rastrear
            try {
                val jsonResponse = fetchIndicadoresJson() // Llamada a la función de red
                if (jsonResponse != null) { // Verificar que la respuesta no sea nula
                    val jsonObject = JSONObject(jsonResponse) // Parsear el JSON
                    if (jsonObject.has("dolar")) { // Verificar si el objeto "dolar" existe
                        val dolarObject = jsonObject.getJSONObject("dolar") // Obtener el objeto "dolar"
                        val valor = dolarObject.getDouble("valor") // Obtener el valor del dólar
                        Log.d("AsyncTest", "Valor del dólar parseado en async: $valor") // Log para rastrear
                        valor // Esto es lo que devolverá el Deferred si todoo va bien
                    } else {
                        Log.e("AsyncTest", "Error en async: No se encontró el indicador 'dolar' en el JSON.") // Log para rastrear
                        null // Devuelve null si no se encuentra 'dolar'
                    }
                } else {
                    Log.e("AsyncTest", "Error en async: jsonResponse fue null.") // Log para rastrear
                    null // Devuelve null si la respuesta JSON es nula (error de red)
                }
            } catch (e: IOException) {
                Log.e("AsyncTest", "IOException en async", e) // Log para rastrear
                null // Devuelve null en caso de error de red
            } catch (e: JSONException) {
                Log.e("AsyncTest", "JSONException en async", e) // Log para rastrear
                null // Devuelve null en caso de error de parseo JSON
            } catch (e: Exception) {
                Log.e("AsyncTest", "Excepción genérica en async", e) // Log para rastrear
                null // Devuelve null para otras excepciones
            }
        }
    } // Fin de obtenerValorDolarAsync

    // Esta función se mantiene igual, se ejecutará en un hilo de fondo
    // gracias al Dispatchers.IO en el que se llama desde obtenerValorDolarAsync()
    @Throws(IOException::class, JSONException::class)
    private fun fetchIndicadoresJson(): String? {
        Log.d("AsyncTest", "fetchIndicadoresJson() ejecutándose en ${Thread.currentThread().name}") // Log para rastrear
        val url = URL("https://mindicador.cl/api") // URL del API
        val connection = url.openConnection() as HttpURLConnection // Abrir la conexión
        // ... (resto del código de fetchIndicadoresJson se mantiene igual)
        connection.requestMethod = "GET" // Métodoo de solicitud
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

    fun cambiarActividadesSalir(){
        // Botones para navegar a las otras pantallas
        // Ir a la actividad MainActivity
        btIrMain2 = findViewById(R.id.btIrMain2) // Inicialización de la variable btIrMain2
        btIrMain2.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } // FIN del botón btIrMain2

        // Ir a la actividad AsyncActivity
        btIrAsync2 = findViewById(R.id.btIrAsync2) // Inicialización de la variable btIrMain2
        btIrAsync2.setOnClickListener {
            val intent = Intent(this, AsyncActivity::class.java)
            startActivity(intent)
        } // FIN del botón btIrAsync2

        // Ir a la actividad RetrofitActivity
        btIrRetrofit2 = findViewById(R.id.btIrRetrofit2) // Inicialización de la variable btIrMain2
        btIrRetrofit2.setOnClickListener {
            val intent = Intent(this, RetrofitActivity::class.java)
            startActivity(intent)
        } // FIN del botón btIrRetrofit2

        // Cerrar App
        val btSalir2 = findViewById<Button>(R.id.btSalir2)
        btSalir2.setOnClickListener {
            finishAffinity()
        } // FIN del botón btSalir2
    } // Fin de cambiarActividadesSalir

} // Fin de la clase AsyncActivity