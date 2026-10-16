# Documentación funcional — LINKED

Para: el dueño y quien programa. Responde qué hace la plataforma y con qué reglas, sin hablar de código.

LINKED conecta clientes con tatuadores: los tatuadores muestran su portfolio (propio o importado de
Instagram) y su agenda; los clientes buscan por zona o estilo, piden turnos y conversan con el tatuador.

## 1. Actores

| Actor | Qué puede hacer |
|---|---|
| Visitante (sin sesión) | ver el feed de trabajos, el portfolio y perfil público de un tatuador, buscar tatuadores en el mapa, ver noticias y especialidades, registrarse, reportar un problema |
| Cliente | todo lo anterior; pedir turnos, ver sus reservas, chatear en sus turnos, dar likes, editar su perfil |
| Tatuador | todo lo del usuario autenticado; configurar su horario, gestionar sus turnos, publicar trabajos, vincular Instagram |
| Moderador | gestionar los tickets de soporte |
| Administrador | gestionar usuarios, roles, especialidades, menú, solicitudes para ser tatuador y tickets |

Un usuario puede tener varios roles; la interfaz muestra el más alto (Administrador > Moderador >
Tatuador > Cliente).

## 2. Casos de uso

### Registrarse
- Quién: visitante.
- Pasos: completa usuario, email, contraseña, fecha de nacimiento y, opcionalmente, nombre, apellido y
  ubicación; resuelve el reCAPTCHA (en producción).
- Reglas: usuario y email únicos; la contraseña viaja cifrada con la clave pública del servidor.
- Resultado: cuenta con rol Cliente y sesión iniciada.

### Registrarse como tatuador
- Quién: visitante.
- Pasos: como el registro, más el estudio y las especialidades.
- Resultado: queda como Cliente con una solicitud pendiente. Cuando un administrador la aprueba pasa a
  Tatuador y recibe las especialidades elegidas; si la rechaza, queda la nota del motivo.

### Pedir un turno
- Quién: cliente.
- Pasos: elige tatuador, día y horario libre; describe el tatuaje y adjunta hasta 3 imágenes de referencia.
- Reglas: el horario tiene que terminar después de empezar y no superponerse con otro turno del tatuador
  que ocupe agenda (Pendiente, Confirmada o Propuesta).
- Resultado: turno Pendiente, visible para el cliente y para el tatuador.

### Gestionar un turno
- Quién: el tatuador asignado.
- Pasos: confirma, rechaza, propone otra fecha, restaura uno rechazado o lo marca completado.
- Reglas: un turno Completada o Cancelada ya no cambia. Solo el cliente y el tatuador del turno ven y
  usan su chat.

### Configurar la agenda
- Quién: tatuador.
- Pasos: indica, por día de la semana, hora de inicio, hora de fin y duración de cada turno.
- Resultado: el cliente ve los horarios libres de cada día y los días sin disponibilidad.

### Vincular Instagram
- Quién: tatuador.
- Pasos: autoriza a LINKED en Instagram.
- Resultado: su usuario y su foto de Instagram pasan a ser los del perfil (la foto no se puede cambiar a
  mano mientras esté vinculada) y sus publicaciones se importan como trabajos.
- Reglas: se sincroniza al vincular, al iniciar sesión, cada 2 horas y a pedido. Lo borrado en Instagram
  deja de mostrarse. Si el token vence o se revoca, la cuenta se desvincula y hay que volver a vincularla.
  Desvincular conserva los trabajos ya importados.

### Recuperar la contraseña
- Quién: cualquier usuario.
- Pasos: pide el enlace con su email; lo recibe por mail y elige una contraseña nueva.
- Reglas: el enlace vale 1 hora y una sola vez; si el email no existe no se informa.

## 3. Reglas de negocio y fórmulas

- **Horarios libres de un día**: desde la hora de inicio, en pasos de la duración del turno, mientras el
  turno termine dentro del horario; se quitan los horarios en los que empieza un turno que ocupa agenda.
  Ejemplo: lunes de 10:00 a 13:00, turnos de 60 min, con un turno reservado a las 11:00 → libres 10:00 y
  12:00.
- **Día bloqueado**: un día del rango sin ningún horario libre (sin horario configurado o todo reservado).
- **Distancia en el mapa**: fórmula de Haversine sobre las coordenadas del estudio; radio por defecto 50 km.
  Ejemplo: Rosario (-32.95, -60.65) a Córdoba (-31.42, -64.18) ≈ 375 km.
- **Antigüedad del tatuador** (perfil público): años cumplidos desde el inicio de actividad; no se publica
  la fecha.
- **Datos públicos**: sin sesión no se muestran nombre real, email, fecha de nacimiento ni el usuario de
  Instagram de nadie.

## 4. Estados

Turnos, solicitudes para ser tatuador y cuentas de Instagram: tablas de transiciones y diagramas en
[diagramas/estados.md](diagramas/estados.md).

## 5. Glosario

| Término | Significado |
|---|---|
| Turno | cita pedida por un cliente a un tatuador (`tat_turno_sesion`) |
| Trabajo | publicación del portfolio de un tatuador, con una o más fotos |
| Especialidad | estilo de tatuaje (realismo, acuarela...) |
| Solicitud | pedido de un usuario para ser tatuador, que aprueba un administrador |
| Perfil | datos de linked de un usuario: estudio, inicio de actividad, ubicación e Instagram |
| Imagen de referencia | imagen que adjunta el cliente al pedir un turno |
| Sincronización | importación de las publicaciones de Instagram como trabajos |
