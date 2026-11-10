# Caparazón Pizza

Aplicación de consola para administrar clientes, productos, inventario, promociones, pedidos y repartidores de una pizzería urbana dirigida por cuatro tortugas cocineras originales.

## Funcionalidades

- Validación y normalización de correo y teléfono.
- Control de cantidades y periodos promocionales.
- Gestión de inventario y asignación de repartidores.
- Cálculo de subtotal, impuesto, descuentos y total.
- Productos promocionables mediante anotaciones.
- Inspección de clases mediante reflection.
- Combinación de pedidos con el operador `+`.
- Desestructuración de clientes y pedidos.
- Prevención de clientes y productos duplicados.
- Manejo seguro de entradas inválidas, errores y valores nulos.

## Ejecución

Desde IntelliJ IDEA, abre la carpeta del proyecto y ejecuta `Main.kt`.

Con Gradle:

```text
gradlew run
```

La ejecución predeterminada muestra una demostración completa. Para capturar un cliente desde la consola:

```text
gradlew run --args="--interactivo"
```

## Pruebas

```text
gradlew test
```
