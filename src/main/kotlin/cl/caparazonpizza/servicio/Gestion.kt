package cl.caparazonpizza.servicio

import cl.caparazonpizza.modelo.Cliente
import cl.caparazonpizza.modelo.ItemPedido
import cl.caparazonpizza.modelo.Pedido
import cl.caparazonpizza.modelo.Producto
import cl.caparazonpizza.modelo.Repartidor

class Registro<T> {
    private val elementos = linkedSetOf<T>()

    fun registrar(elemento: T): Boolean = elementos.add(elemento)

    fun todos(): List<T> = elementos.toList()
}

class Inventario(inicial: Map<String, Int>) {
    private val existencias = inicial.toMutableMap()

    init {
        require(existencias.values.all { it >= 0 }) { "El inventario no admite cantidades negativas." }
    }

    fun faltantes(items: List<ItemPedido>): Map<String, Int> {
        val requeridos = mutableMapOf<String, Int>()
        items.forEach { item ->
            item.producto.ingredientes.forEach { (ingrediente, cantidad) ->
                requeridos.merge(ingrediente, cantidad * item.cantidad, Int::plus)
            }
        }
        return requeridos.mapNotNull { (ingrediente, cantidad) ->
            val deficit = cantidad - existencias.getOrDefault(ingrediente, 0)
            if (deficit > 0) ingrediente to deficit else null
        }.toMap()
    }

    fun reservar(items: List<ItemPedido>) {
        val faltantes = faltantes(items)
        require(faltantes.isEmpty()) {
            "Stock insuficiente: " + faltantes.entries.joinToString { "${it.key} (${it.value})" }
        }
        items.forEach { item ->
            item.producto.ingredientes.forEach { (ingrediente, cantidad) ->
                existencias.compute(ingrediente) { _, actual -> (actual ?: 0) - cantidad * item.cantidad }
            }
        }
    }

    fun estado(): Map<String, Int> = existencias.toMap()
}

class GestorPizzeria(
    private val inventario: Inventario,
    private val repartidores: List<Repartidor>
) {
    val clientes = Registro<Cliente>()
    val productos = Registro<Producto>()
    private val pedidos = mutableListOf<Pedido>()

    fun aceptarPedido(pedido: Pedido): String {
        inventario.reservar(pedido.items)
        pedidos += pedido
        return "Pedido ${pedido.id} aceptado."
    }

    fun asignarRepartidor(sector: String): Repartidor? {
        val repartidor = repartidores.find { it.disponible && it.sector.equals(sector, ignoreCase = true) }
            ?: repartidores.find { it.disponible }
        repartidor?.disponible = false
        return repartidor
    }

    fun pedidosDelDia(): List<Pedido> = pedidos.toList()

    fun existencias(): Map<String, Int> = inventario.estado()
}

object InspectorClases {
    fun describir(tipo: Class<*>): String {
        val propiedades = tipo.declaredFields
            .filterNot { it.isSynthetic }
            .map { it.name }
            .sorted()
        val metodos = tipo.declaredMethods
            .filterNot { it.isSynthetic }
            .map { it.name }
            .distinct()
            .sorted()
        return buildString {
            appendLine("Clase: ${tipo.simpleName}")
            appendLine("Propiedades: ${propiedades.joinToString()}")
            append("Métodos: ${metodos.joinToString()}")
        }
    }
}
