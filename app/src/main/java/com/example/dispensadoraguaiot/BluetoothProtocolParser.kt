package com.example.dispensadoraguaiot

/**
 * Procesador de tramas seriales provenientes del Arduino Uno
 */
class BluetoothProtocolParser {

    fun procesarTrama(trama: String, estadoActual: DispensadorUiState.Conectado): DispensadorUiState.Conectado {
        return when {
            // Deserialización de telemetría: BT_DATA:WEIGHT:150.5;TEMP:72.3
            trama.startsWith("BT_DATA:WEIGHT:") -> {
                val datos = trama.removePrefix("BT_DATA:").split(";")
                val peso = datos[0].substringAfter("WEIGHT:").toFloatOrNull() ?: estadoActual.pesoActualGramos
                val temp = datos[1].substringAfter("TEMP:").toFloatOrNull() ?: estadoActual.temperaturaAguaCelsius
                estadoActual.copy(
                    pesoActualGramos = peso,
                    temperaturaAguaCelsius = temp,
                    sirviendo = true,
                    mensajeAlerta = null
                )
            }
            // Mapeo humanizado de alarmas de hardware
            trama.contains("BT_WARN:NO_CUP") -> {
                estadoActual.copy(tazaDetectada = false, mensajeAlerta = "No se detecta la taza en la plataforma.")
            }
            trama.contains("BT_WARN:WATER_COLD") -> {
                estadoActual.copy(mensajeAlerta = "El agua aún no alcanza los 60 °C configurados para dispensar.")
            }
            trama.contains("BT_STATUS:FINISHED") -> {
                estadoActual.copy(sirviendo = false, mensajeAlerta = "Dosificación completada con éxito.")
            }
            else -> estadoActual
        }
    }

    fun serializarComandoPeso(gramos: Float): String {
        return "SET:${gramos}\n"
    }
}