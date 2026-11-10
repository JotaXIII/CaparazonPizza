package cl.caparazonpizza

import cl.caparazonpizza.modelo.Categoria
import cl.caparazonpizza.modelo.Cliente
import cl.caparazonpizza.modelo.ItemPedido
import cl.caparazonpizza.modelo.Pedido
import cl.caparazonpizza.modelo.PeriodoPromocional
import cl.caparazonpizza.modelo.PizzaMutante
import cl.caparazonpizza.modelo.Producto
import cl.caparazonpizza.modelo.esPromocionable
import cl.caparazonpizza.servicio.Inventario
import cl.caparazonpizza.servicio.Validadores
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CaparazonPizzaTest {
    private val cliente = Cliente("Luna", "luna@correo.cl", "+56 (912) 345-6789", "Centro")
    private val pizza = PizzaMutante("Suprema", BigDecimal("10000"), mapOf("queso" to 2))
    private val promocion = PeriodoPromocional(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30))

    @Test
    fun validaCorreoTelefonoCantidadYFecha() {
        assertTrue(Validadores.correoValido("nombre@dominio.com"))
        assertFalse(Validadores.correoValido("nombre@dominio"))
        assertEquals("+56 (912) 345-6789", Validadores.formatearTelefono("+56 912 345 6789"))
        assertTrue(Validadores.cantidadValida(100))
        assertFalse(Validadores.cantidadValida(101))
        assertEquals(LocalDate.of(2026, 11, 9), Validadores.fechaValida("2026-11-09"))
    }

    @Test
    fun detectaPromocionYCalculaTotal() {
        val pedido = Pedido(1, cliente, listOf(ItemPedido(pizza, 4)), LocalDate.of(2026, 11, 9))
        assertTrue(pizza.esPromocionable())
        assertEquals(BigDecimal("10000"), pedido.descuento(promocion))
        assertEquals(BigDecimal("35700"), pedido.total(promocion))
    }

    @Test
    fun combinaPedidosYDesestructuraEntidades() {
        val primero = Pedido(1, cliente, listOf(ItemPedido(pizza, 1)), LocalDate.of(2026, 11, 8))
        val segundo = Pedido(2, cliente, listOf(ItemPedido(pizza, 2)), LocalDate.of(2026, 11, 9))
        val combinado = primero + segundo
        val (clienteCombinado, items, subtotal) = combinado
        assertEquals(cliente, clienteCombinado)
        assertEquals(3, items.single().cantidad)
        assertEquals(BigDecimal("30000"), subtotal)
    }

    @Test
    fun evitaDuplicadosConIgualdadEstructural() {
        val otroCliente = Cliente("luna", "LUNA@CORREO.CL", "+56 (900) 000-0000", "Norte")
        val otroProducto = Producto("suprema", Categoria.PIZZA, BigDecimal("12000"))
        assertEquals(cliente, otroCliente)
        assertEquals(pizza, otroProducto)
        assertEquals(1, setOf(cliente, otroCliente).size)
        assertEquals(1, setOf(pizza, otroProducto).size)
    }

    @Test
    fun rechazaInventarioInsuficiente() {
        val inventario = Inventario(mapOf("queso" to 1))
        assertFailsWith<IllegalArgumentException> {
            inventario.reservar(listOf(ItemPedido(pizza, 1)))
        }
    }
}
