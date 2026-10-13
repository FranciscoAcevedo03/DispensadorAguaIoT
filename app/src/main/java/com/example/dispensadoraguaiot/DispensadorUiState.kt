package com.example.dispensadoraguaiot

/**
 * Representación sellada de los estados de la interfaz de usuario (UiState Pattern)
 */
sealed interface DispensadorUiState {

    // Estado de inicialización y conexión con el módulo Bluetooth HC-05
    object Conectando : DispensadorUiState

    // Estado activo con telemetría en tiempo real
    data class Conectado(
        val pesoActualGramos: Float = 0.0f,
        val temperaturaAguaCelsius: Float = 0.0f,
        val pesoObjetivoGramos: Float = 200.0f,
        val tazaDetectada: Boolean = false,
        val sirviendo: Boolean = false,
        val mensajeAlerta: String? = null
    ) : DispensadorUiState

    // Estado de falla de enlace con opción de recuperación
    data class ErrorConexion(
        val mensajeHumano: String,
        val puedeReintentar: Boolean = true
    ) : DispensadorUiState
}