package cl.caparazonpizza.servicio

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object Validadores {
    private val patronCorreo = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val patronTelefono = Regex("^\\+(\\d{2})\\s?\\(?(\\d{3})\\)?\\s?(\\d{3})[- ]?(\\d{4})$")
    private val formatoFecha = DateTimeFormatter.ISO_LOCAL_DATE

    fun correoValido(correo: String): Boolean = patronCorreo.matches(correo.trim())

    fun formatearTelefono(telefono: String): String? {
        val coincidencia = patronTelefono.matchEntire(telefono.trim()) ?: return null
        val (pais, area, central, linea) = coincidencia.destructured
        return "+$pais ($area) $central-$linea"
    }

    fun cantidadValida(cantidad: Int): Boolean = cantidad in 1..100

    fun fechaValida(fecha: String): LocalDate? = try {
        LocalDate.parse(fecha.trim(), formatoFecha)
    } catch (_: DateTimeParseException) {
        null
    }

    fun solicitarCorreo(leer: () -> String?, escribir: (String) -> Unit = ::println): String {
        while (true) {
            val valor = leer()?.trim().orEmpty()
            if (correoValido(valor)) return valor
            escribir("Correo inválido. Usa el formato nombre@dominio.com.")
        }
    }

    fun solicitarTelefono(leer: () -> String?, escribir: (String) -> Unit = ::println): String {
        while (true) {
            val valor = leer()?.trim().orEmpty()
            formatearTelefono(valor)?.let { return it }
            escribir("Teléfono inválido. Usa el formato +XX (XXX) XXX-XXXX.")
        }
    }

    fun solicitarCantidad(leer: () -> String?, escribir: (String) -> Unit = ::println): Int {
        while (true) {
            val valor = try {
                leer()?.trim()?.toInt()
            } catch (_: NumberFormatException) {
                null
            }
            if (valor != null && cantidadValida(valor)) return valor
            escribir("Cantidad inválida. Ingresa un número entre 1 y 100.")
        }
    }

    fun solicitarFecha(leer: () -> String?, escribir: (String) -> Unit = ::println): LocalDate {
        while (true) {
            val valor = fechaValida(leer().orEmpty())
            if (valor != null) return valor
            escribir("Fecha inválida. Usa el formato AAAA-MM-DD.")
        }
    }
}
