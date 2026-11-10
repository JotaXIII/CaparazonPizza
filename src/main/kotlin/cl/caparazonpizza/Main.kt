package cl.caparazonpizza

import cl.caparazonpizza.modelo.Categoria
import cl.caparazonpizza.modelo.Cliente
import cl.caparazonpizza.modelo.ComboAlcantarilla
import cl.caparazonpizza.modelo.EstadoPedido
import cl.caparazonpizza.modelo.ItemPedido
import cl.caparazonpizza.modelo.Pedido
import cl.caparazonpizza.modelo.PeriodoPromocional
import cl.caparazonpizza.modelo.PizzaMutante
import cl.caparazonpizza.modelo.Producto
import cl.caparazonpizza.modelo.Repartidor
import cl.caparazonpizza.modelo.esPromocionable
import cl.caparazonpizza.modelo.moneda
import cl.caparazonpizza.servicio.GestorPizzeria
import cl.caparazonpizza.servicio.InspectorClases
import cl.caparazonpizza.servicio.Inventario
import cl.caparazonpizza.servicio.Validadores
import java.math.BigDecimal
import java.time.LocalDate

fun main(args: Array<String>) {
    if ("--interactivo" in args) {
        ejecutarRegistroInteractivo()
    } else {
        ejecutarDemostracion()
    }
}

private fun ejecutarDemostracion() {
    println("=".repeat(68))
    println("CAPARAZÓN PIZZA | Sistema de gestión")
    println("=".repeat(68))

    demostrarValidaciones()

    val cliente = Cliente(
        nombre = "Luna Verde",
        correo = "luna@caparazon.cl",
        telefono = "+56 (912) 345-6789",
        direccion = "Pasaje Subterráneo 404",
        notas = null
    )
    val clienteDuplicado = Cliente(
        nombre = "luna verde",
        correo = "LUNA@CAPARAZON.CL",
        telefono = "+56 (999) 999-9999",
        direccion = "Otra dirección"
    )

    val pizza = PizzaMutante(
        nombre = "Mutante Suprema",
        precio = BigDecimal("8990"),
        ingredientes = mapOf("masa" to 1, "queso" to 2, "pepperoni" to 1)
    )
    val combo = ComboAlcantarilla(
        nombre = "Combo Subterráneo",
        precio = BigDecimal("11990"),
        ingredientes = mapOf("masa" to 1, "queso" to 1, "bebida" to 1)
    )
    val bebida = Producto("Bebida Lima", Categoria.BEBIDA, BigDecimal("1800"))
    val pizzaDuplicada = Producto("mutante suprema", Categoria.PIZZA, BigDecimal("9990"))

    val gestor = GestorPizzeria(
        inventario = Inventario(mapOf("masa" to 20, "queso" to 30, "pepperoni" to 10, "bebida" to 12)),
        repartidores = listOf(
            Repartidor("Atlas", "+56 (911) 111-1111", "Centro"),
            Repartidor("Nébula", "+56 (922) 222-2222", "Norte")
        )
    )

    println("\nREGISTRO SIN DUPLICADOS")
    println("Cliente nuevo: ${gestor.clientes.registrar(cliente)}")
    println("Cliente duplicado: ${gestor.clientes.registrar(clienteDuplicado)}")
    println("Producto nuevo: ${gestor.productos.registrar(pizza)}")
    println("Producto duplicado: ${gestor.productos.registrar(pizzaDuplicada)}")
    gestor.productos.registrar(combo)
    gestor.productos.registrar(bebida)

    println("\nPRODUCTOS PROMOCIONABLES")
    gestor.productos.todos().filter { it.esPromocionable() }.forEach {
        println("- ${it.detallarProducto()}")
    }

    val promocion = PeriodoPromocional(
        inicio = LocalDate.of(2026, 11, 1),
        fin = LocalDate.of(2026, 11, 30)
    )
    val repartidor = gestor.asignarRepartidor("Centro")
    val pedido1 = Pedido(
        id = 101,
        cliente = cliente,
        items = listOf(ItemPedido(pizza, 2), ItemPedido(bebida, 1)),
        fecha = LocalDate.of(2026, 11, 9),
        repartidor = repartidor
    )
    val pedido2 = Pedido(
        id = 102,
        cliente = cliente,
        items = listOf(ItemPedido(pizza, 2), ItemPedido(combo, 1)),
        fecha = LocalDate.of(2026, 11, 9),
        repartidor = repartidor
    )

    println("\nCONTROL DE INVENTARIO")
    listOf(pedido1, pedido2).forEach { pedido ->
        try {
            println(gestor.aceptarPedido(pedido))
        } catch (error: IllegalArgumentException) {
            println("Pedido ${pedido.id} rechazado: ${error.message}")
        }
    }
    println("Existencias: ${gestor.existencias().entries.joinToString { "${it.key}=${it.value}" }}")

    val combinado = pedido1 + pedido2
    combinado.cambiarEstado(EstadoPedido.EN_CAMINO)
    val (clientePedido, productosPedido, subtotalPedido) = combinado
    val (nombreCliente, correoCliente, telefonoCliente) = clientePedido

    println("\nCLIENTE DESESTRUCTURADO")
    println("Nombre: $nombreCliente")
    println("Correo: $correoCliente")
    println("Teléfono: $telefonoCliente")
    println("Notas: ${clientePedido.notasNormalizadas()}")

    println("\nPEDIDO COMBINADO")
    println("Identificador: ${combinado.id}")
    productosPedido.forEach { println("- ${it.cantidad} x ${it.producto.nombre}: $${it.subtotal().moneda()}") }
    println("Subtotal: $${subtotalPedido.moneda()}")
    println("Descuento: $${combinado.descuento(promocion).moneda()}")
    println("IVA 19%: $${combinado.impuesto(promocion).moneda()}")
    println("Total: $${combinado.total(promocion).moneda()}")
    println("Estado: ${combinado.estado.descripcion}")
    println("Repartidor: ${combinado.repartidor?.nombre ?: "Sin asignar"}")
    println("Promoción vigente: ${promocion.contiene(combinado.fecha)}")

    println("\nREFLECTION")
    println(InspectorClases.describir(Cliente::class.java))
    println(InspectorClases.describir(Pedido::class.java))

    println("\nRESUMEN DEL DÍA")
    gestor.pedidosDelDia().forEach { pedido ->
        println("Pedido ${pedido.id} | ${pedido.cliente.nombre} | $${pedido.total(promocion).moneda()} | ${pedido.estado.descripcion}")
    }
}

private fun demostrarValidaciones() {
    println("\nVALIDACIONES")
    val entradasCorreo = ArrayDeque(listOf("correo-invalido", "luna@caparazon.cl"))
    val correo = Validadores.solicitarCorreo(entradasCorreo::removeFirstOrNull)
    val entradasTelefono = ArrayDeque(listOf("123", "+56 912 345 6789"))
    val telefono = Validadores.solicitarTelefono(entradasTelefono::removeFirstOrNull)
    val entradasCantidad = ArrayDeque(listOf("cien", "101", "4"))
    val cantidad = Validadores.solicitarCantidad(entradasCantidad::removeFirstOrNull)
    val entradasFecha = ArrayDeque(listOf("09-11-2026", "2026-11-09"))
    val fecha = Validadores.solicitarFecha(entradasFecha::removeFirstOrNull)
    println("Datos válidos: $correo | $telefono | cantidad=$cantidad | fecha=$fecha")
}

private fun ejecutarRegistroInteractivo() {
    println("CAPARAZÓN PIZZA | Registro de cliente")
    print("Nombre: ")
    val nombre = readlnOrNull()?.trim().orEmpty().ifBlank { "Cliente" }
    print("Correo: ")
    val correo = Validadores.solicitarCorreo(
        leer = { readlnOrNull() },
        escribir = { mensaje -> println(mensaje); print("Correo: ") }
    )
    print("Teléfono: ")
    val telefono = Validadores.solicitarTelefono(
        leer = { readlnOrNull() },
        escribir = { mensaje -> println(mensaje); print("Teléfono: ") }
    )
    print("Dirección: ")
    val direccion = readlnOrNull()?.trim().orEmpty().ifBlank { "Sin dirección" }
    val cliente = Cliente(nombre, correo, telefono, direccion)
    println("Cliente registrado: $cliente | $telefono | $direccion")
}
