
# 📱 Gestión de Reservas – Funcionalidades por Pantalla

Este documento describe de forma clara y ordenada las funcionalidades principales de cada fragmento o vista de la aplicación **Gestión de Reservas**.

---

## 🏠 HomeFragment

**Resumen diario de sesiones.**

- Muestra las sesiones programadas para el día actual.
- Permite acceder al detalle de cada sesión.
- Permite agregar comentario.
- Permite agregar datos a caja chica.
- Permite enviar correos,whatsapp o llamar al cliente.
- Integra animaciones y diseño con `RecyclerView`.
- Visualización de pagos recientes o comentarios (si está activado).

---

## 📅 CalendarioFragmentDiario

**Vista diaria de franjas horarias.**

- Muestra todas las franjas horarias del día.
- Indica el estado de cada sala: **Libre**, **Reservada**, **Bloqueada**.
- `RecyclerView` principal por franja + `RecyclerView` anidado por sala.
- Permite bloquear uno o varios días completos (selección múltiple).
- Refresca la información con `SwipeRefreshLayout`.
- Permite acceder al detalle de una sesión concreta.

---

## 📋 ListadoFragment

**Listado filtrable de sesiones.**

- Lista las sesiones programadas.
- Filtro por estado de reserva mediante `Spinner`.
- Filtro por nombre de reserva mediante editText en tiempo real.
- Permite navegar al detalle de una sesión.
- Preparado para paginación y carga progresiva.

---

## 💸 CajaChicaFragment

**Gestión de pagos manuales (efectivo).**

- Muestra ingresos y gastos de caja chica simulando caja del negocio.
- Cálculo automático del **saldo total** en caja.
- Permite:
  - ➕ Agregar pagos.
  - ✏️ Editar pagos.
  - ❌ Eliminar pagos.
- Usa `Snackbar` para mensajes y `AlertDialog` para confirmaciones.
- Refresca datos con `SwipeRefreshLayout`.
- Animación de entrada al cargar el `RecyclerView`.

---

## 💬 ComentariosFragment

**Mensajes de clientes.**

- Muestra los comentarios agregados por los trabajadores desde la API mock.
- Pensado para mostrar por fecha o palabra descripción.
- Interfaz lista para integrar filtros adicionales.

---

## ⚙️ ConfiguracionFragment

**Preferencias de usuario.**

- Activar o desactivar **notificaciones locales**.
- Gestión del modo oscuro (si está implementado).
- Botón para **cerrar sesión**.
- Botón para **cerrar sesión** de gmail si finalmente se usa.
- Guarda y recupera preferencias con `SharedPreferences`.
- Lanza workers según configuración del usuario.

---

## 🧾 DetalleSesionFragment

**Vista completa de una sesión.**

- Muestra todos los datos de la sesión:
  - Nombre del cliente.
  - Monitor asignado.
  - Salas ocupadas.
  - Fecha, hora y estado del pago.
- Accesible desde:
  - `HomeFragment`
  - `CalendarioFragmentDiario`
  - `ListadoFragment`
- Presentación estructurada de la información.

---
### 💳 PagosFragment

Esta vista permite visualizar todos los pagos registrados, incluyendo los asociados a reservas y los adicionales. Se diferencia de la sección de Caja Chica en que **no muestra los gastos** y **permite filtrar** por tipo y estado.

#### Funcionalidades principales:
- **Listado de pagos**: muestra todos los pagos registrados (tipo Web, Local, Extra), ordenados cronológicamente.
- **Filtros por tipo**: permite al usuario filtrar los pagos según su origen: `Web`, `Local`, `Extra`.
- **Filtros por estado**: los pagos pueden filtrarse también por estado (`Confirmado`, `Pendiente`, etc.).
- **Asociación a reservas**: si un pago está relacionado con una reserva, se puede navegar a su detalle.
- **Visualización limpia**: interfaz moderna que facilita la comprensión del origen y estado de cada pago.

---

## 🔐 LoginActivity

**Pantalla de inicio de sesión.**

- Autenticación con email y contraseña.
- Guarda token JWT en `SharedPreferences`.
- Redirige a `ReservasActivity` si el login es correcto.
- Opción de mantener la sesión iniciada.

---

## 🧭 ReservasActivity

**Contenedor principal de navegación.**

- Incluye `DrawerLayout` con menú lateral (`NavigationView`).
- Permite acceder a todos los fragmentos:
  - Home
  - Calendario Diario
  - Listado
  - Pagos
  - Caja Chica
  - Comentarios
  - Configuración

---



## 🧱 Arquitectura y Tecnologías

### 🧩 Arquitectura
- Patrón **MVVM** (Model-View-ViewModel)
- Uso de **ViewModel** y **LiveData** para separación de lógica de negocio y UI
- Repositorios centralizados para manejar la lógica de datos y llamadas API

### 🛠 Tecnologías usadas

- **Kotlin** como lenguaje principal
- **Retrofit** para consumo de APIs REST
- **Coroutines** para operaciones asíncronas
- **Material Components** para diseño moderno
- **SharedPreferences** para persistencia de datos local
- **WorkManager** para tareas en segundo plano
- **Mock API (JSON Server)** para desarrollo sin backend real

---

## ✅ Estado actual

✔️ Aplicación funcional con múltiples pantallas conectadas a una API mock.  
✔️ Listados dinámicos, navegación entre pantallas y control de errores implementado.  
✔️ En proceso de adaptación para conexión con API real y despliegue completo.

---
