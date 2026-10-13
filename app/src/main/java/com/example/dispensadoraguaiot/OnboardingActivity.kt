package com.example.dispensadoraguaiot

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class OnboardingActivity : AppCompatActivity() {

    private var pasoActual = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        val tvTitulo = findViewById<TextView>(R.id.tvTituloOnboarding)
        val tvDesc = findViewById<TextView>(R.id.tvDescOnboarding)
        val tvIndicador = findViewById<TextView>(R.id.tvIndicadorPaso)
        val btnSiguiente = findViewById<Button>(R.id.btnSiguienteOnboarding)

        fun actualizarVista() {
            when (pasoActual) {
                1 -> {
                    tvIndicador.text = "Paso 1 de 3"
                    tvTitulo.text = "Dosificación Automática"
                    tvDesc.text = "Control preciso de llenado mediante celda de carga HX711 y corte seguro por relé."
                    btnSiguiente.text = "Siguiente"
                }
                2 -> {
                    tvIndicador.text = "Paso 2 de 3"
                    tvTitulo.text = "Bloqueo Térmico Crítico"
                    tvDesc.text = "Validación lógica de temperatura en tiempo real. La bomba no operará si el agua está a menos de 60 °C."
                    btnSiguiente.text = "Siguiente"
                }
                3 -> {
                    tvIndicador.text = "Paso 3 de 3"
                    tvTitulo.text = "Conectividad Bluetooth"
                    tvDesc.text = "Comunicación inalámbrica RFCOMM con Arduino para telemetría continua y tolerancia a fallos."
                    btnSiguiente.text = "Comenzar"
                }
            }
        }

        btnSiguiente.setOnClickListener {
            if (pasoActual < 3) {
                pasoActual++
                actualizarVista()
            } else {
                // Guarda la bandera de un solo uso en SharedPreferences
                val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
                prefs.edit().putBoolean("onboarding_complete", true).apply()

                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        actualizarVista()
    }
}