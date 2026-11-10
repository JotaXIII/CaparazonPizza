package cl.caparazonpizza.modelo

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

enum class Categoria {
    PIZZA,
    BEBIDA,
    COMBO,
    INGREDIENTE
}

enum class EstadoPedido(val descripcion: String) {
    EN_PREPARACION("En preparación"),
    LISTO("Listo"),
    EN_CAMINO("En camino"),
    ENTREGADO("Entregado")
}

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Promocionable(val descuento: Int)

open class Usuario(
    open val nombre: String,
    open val contacto: String
)

class Cliente(
    override val nombre: String,
    val correo: String,
    val telefono: String,
    val direccion: String,
    val notas: String? = null
) : Usuario(nombre, correo) {
    operator fun component1() = nombre
    operator fun component2() = correo
    operator fun component3() = telefono

    fun notasNormalizadas(): String = notas?.takeIf { it.isNotBlank() } ?: "Sin notas"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Cliente) return false
        return nombre.equals(other.nombre, ignoreCase = true) &&
            correo.equals(other.correo, ignoreCase = true)
    }

    override fun hashCode(): Int = 31 * nombre.lowercase().hashCode() + correo.lowercase().hashCode()

    override fun toString(): String = "$nombre <$correo>"
}

data class Repartidor(
    override val nombre: String,
    override val contacto: String,
    val sector: String,
    var disponible: Boolean = true
) : Usuario(nombre, contacto) {
    fun entregarPedido(pedido: Pedido) {
        pedido.cambiarEstado(EstadoPedido.ENTREGADO)
        disponible = true
    }
}

open class Producto(
    val nombre: String,
    val categoria: Categoria,
    val precio: BigDecimal,
    val ingredientes: Map<String, Int> = emptyMap()
) {
    fun detallarProducto(): String = "$nombre | ${categoria.name.lowercase()} | $${precio.moneda()}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Producto) return false
        return nombre.equals(other.nombre, ignoreCase = true) && categoria == other.categoria
    }

    override fun hashCode(): Int = 31 * nombre.lowercase().hashCode() + categoria.hashCode()

    override fun toString(): String = nombre
}

@Promocionable(descuento = 15)
class PizzaMutante(
    nombre: String,
    precio: BigDecimal,
    ingredientes: Map<String, Int>
) : Producto(nombre, Categoria.PIZZA, precio, ingredientes)

@Promocionable(descuento = 20)
class ComboAlcantarilla(
    nombre: String,
    precio: BigDecimal,
    ingredientes: Map<String, Int>
) : Producto(nombre, Categoria.COMBO, precio, ingredientes)

data class ItemPedido(val producto: Producto, val cantidad: Int) {
    init {
        require(cantidad in 1..100) { "La cantidad debe estar entre 1 y 100." }
    }

    fun subtotal(): BigDecimal = producto.precio.multiply(cantidad.toBigDecimal())
}

class Pedido(
    val id: Int,
    val cliente: Cliente,
    val items: List<ItemPedido>,
    val fecha: LocalDate,
    estadoInicial: EstadoPedido = EstadoPedido.EN_PREPARACION,
    val repartidor: Repartidor? = null
) {
    var estado: EstadoPedido = estadoInicial
        private set

    init {
        require(items.isNotEmpty()) { "El pedido debe contener al menos un producto." }
    }

    val subtotal: BigDecimal
        get() = items.fold(BigDecimal.ZERO) { total, item -> total + item.subtotal() }.dinero()

    val cantidadPizzas: Int
        get() = items.filter { it.producto.categoria == Categoria.PIZZA }.sumOf { it.cantidad }

    fun descuento(promocion: PeriodoPromocional): BigDecimal {
        val descuentoCantidad = if (cantidadPizzas > 3) subtotal.porcentaje(10) else BigDecimal.ZERO
        val descuentoPromocion = if (promocion.contiene(fecha)) {
            items.filter { it.producto.esPromocionable() }
                .fold(BigDecimal.ZERO) { total, item ->
                    val porcentaje = item.producto.javaClass.getAnnotation(Promocionable::class.java).descuento
                    total + item.subtotal().porcentaje(porcentaje)
                }
        } else {
            BigDecimal.ZERO
        }
        return (descuentoCantidad + descuentoPromocion).coerceAtMost(subtotal).dinero()
    }

    fun impuesto(promocion: PeriodoPromocional): BigDecimal =
        (subtotal - descuento(promocion)).porcentaje(19)

    fun total(promocion: PeriodoPromocional): BigDecimal =
        (subtotal - descuento(promocion) + impuesto(promocion)).dinero()

    fun cambiarEstado(nuevoEstado: EstadoPedido) {
        estado = nuevoEstado
    }

    operator fun component1() = cliente
    operator fun component2() = items
    operator fun component3() = subtotal

    operator fun plus(otro: Pedido): Pedido {
        require(cliente == otro.cliente) { "Solo se pueden combinar pedidos del mismo cliente." }
        val agrupados = (items + otro.items)
            .groupBy { it.producto }
            .map { (producto, lineas) -> ItemPedido(producto, lineas.sumOf { it.cantidad }) }
        return Pedido(
            id = minOf(id, otro.id),
            cliente = cliente,
            items = agrupados,
            fecha = maxOf(fecha, otro.fecha),
            repartidor = repartidor ?: otro.repartidor
        )
    }
}

data class PeriodoPromocional(
    val inicio: LocalDate,
    val fin: LocalDate
) {
    init {
        require(!fin.isBefore(inicio)) { "El fin de la promoción no puede preceder a su inicio." }
    }

    fun contiene(fecha: LocalDate): Boolean = fecha in inicio..fin
}

fun Producto.esPromocionable(): Boolean = javaClass.isAnnotationPresent(Promocionable::class.java)

fun BigDecimal.dinero(): BigDecimal = setScale(0, RoundingMode.HALF_UP)

fun BigDecimal.moneda(): String = dinero().toPlainString()

fun BigDecimal.porcentaje(valor: Int): BigDecimal =
    multiply(valor.toBigDecimal()).divide(100.toBigDecimal(), 2, RoundingMode.HALF_UP).dinero()
