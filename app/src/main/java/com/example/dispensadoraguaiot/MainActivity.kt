package com.example.dispensadoraguaiot

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.progressindicator.LinearProgressIndicator
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private val parser = BluetoothProtocolParser()
    private var estadoActual = DispensadorUiState.Conectado()

    private var btSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    private val BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        solicitarPermisosSiFaltan()

        val btnReconectar = findViewById<Button>(R.id.btnReconectar)
        val btnDispensar = findViewById<Button>(R.id.btnDispensar)
        val btnPerfil = findViewById<Button>(R.id.btnPerfil)

        btnPerfil.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        btnReconectar.setOnClickListener {
            if (tienePermisos()) {
                conectarHC05()
            } else {
                solicitarPermisosSiFaltan()
            }
        }

        btnDispensar.setOnClickListener {
            if (estadoActual.temperaturaAguaCelsius < 60.0f) {
                Toast.makeText(this, "Alerta: Temperatura insuficiente (< 60 °C)", Toast.LENGTH_SHORT).show()
            } else {
                enviarComandoArduino("SET:200\n")
                Toast.makeText(this, "Comando enviado al Arduino", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun tienePermisos(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun solicitarPermisosSiFaltan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !tienePermisos()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN),
                101
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun conectarHC05() {
        try {
            val btManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val btAdapter = btManager.adapter

            if (btAdapter == null || !btAdapter.isEnabled) {
                Toast.makeText(this, "Por favor, enciende el Bluetooth de tu celular", Toast.LENGTH_SHORT).show()
                return
            }

            val dispositivo = btAdapter.bondedDevices.find { it.name == "HC-05" }

            if (dispositivo != null) {
                val tvEstado = findViewById<TextView>(R.id.tvEstadoConexion)
                tvEstado.text = "Conectando a HC-05..."
                tvEstado.setTextColor(Color.parseColor("#F57F17"))

                thread {
                    try {
                        btSocket = dispositivo.createRfcommSocketToServiceRecord(BT_UUID)
                        btSocket?.connect()
                        outputStream = btSocket?.outputStream

                        runOnUiThread {
                            tvEstado.text = "Estado: Conectado (HC-05)"
                            tvEstado.setTextColor(Color.parseColor("#2E7D32"))
                        }
                        escucharDatosSeriales(btSocket?.inputStream)

                    } catch (e: Exception) {
                        runOnUiThread {
                            tvEstado.text = "Error de conexión"
                            tvEstado.setTextColor(Color.parseColor("#C62828"))
                            Toast.makeText(this, "Fallo al conectar. Revisa el HC-05.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "HC-05 no encontrado. Empareja el Bluetooth en Ajustes primero.", Toast.LENGTH_LONG).show()
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Falta otorgar permiso de Dispositivos Cercanos", Toast.LENGTH_SHORT).show()
            solicitarPermisosSiFaltan()
        } catch (e: Exception) {
            Toast.makeText(this, "Error inesperado al conectar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun escucharDatosSeriales(inputStream: InputStream?) {
        if (inputStream == null) return
        val buffer = ByteArray(1024)
        var bytes: Int

        while (true) {
            try {
                bytes = inputStream.read(buffer)
                val tramaEntrante = String(buffer, 0, bytes)

                estadoActual = parser.procesarTrama(tramaEntrante, estadoActual)
                actualizarInterfaz()

            } catch (e: Exception) {
                runOnUiThread {
                    val tvEstado = findViewById<TextView>(R.id.tvEstadoConexion)
                    tvEstado.text = "Conexión perdida"
                    tvEstado.setTextColor(Color.parseColor("#C62828"))
                }
                break
            }
        }
    }

    private fun actualizarInterfaz() {
        runOnUiThread {
            findViewById<TextView>(R.id.tvTemperatura).text = "${estadoActual.temperaturaAguaCelsius} °C"

            val tvEstadoTemp = findViewById<TextView>(R.id.tvEstadoTemp)
            if (estadoActual.temperaturaAguaCelsius >= 60.0f) {
                tvEstadoTemp.text = "Óptima para dispensar (> 60 °C)"
                tvEstadoTemp.setTextColor(Color.parseColor("#388E3C"))
            } else {
                tvEstadoTemp.text = "Calentando agua..."
                tvEstadoTemp.setTextColor(Color.parseColor("#F57F17"))
            }

            findViewById<TextView>(R.id.tvPeso).text = "${estadoActual.pesoActualGramos} g / ${estadoActual.pesoObjetivoGramos} g"
            findViewById<LinearProgressIndicator>(R.id.progresoPeso).progress = estadoActual.pesoActualGramos.toInt()

            estadoActual.mensajeAlerta?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun enviarComandoArduino(comando: String) {
        try {
            outputStream?.write(comando.toByteArray())
        } catch (e: Exception) {
            Toast.makeText(this, "Error al enviar comando Bluetooth", Toast.LENGTH_SHORT).show()
        }
    }
}