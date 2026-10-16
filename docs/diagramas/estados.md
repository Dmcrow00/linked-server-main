# Máquinas de estado

Tienen que coincidir con el enum que las implementa.

## Turno (`EstadoTurno`, `tat_turno_sesion.estado`)

El turno nace Pendiente al pedirlo el cliente. El backend solo impide cambiar un turno ya Completada o
Cancelada (409 `TURNO_CERRADO`), igual que Node; el resto de los cambios los decide el tatuador asignado
(`PATCH /api/turnos/gestionar/{id}` o `PUT /api/turnos/{id}`). Las flechas son las que usa hoy el cliente
web.

```mermaid
stateDiagram-v2
    [*] --> Pendiente: el cliente solicita el turno
    Pendiente --> Confirmada: el tatuador acepta
    Pendiente --> Rechazada: el tatuador rechaza
    Pendiente --> Propuesta: el tatuador propone otra fecha
    Propuesta --> Confirmada
    Propuesta --> Rechazada
    Rechazada --> Pendiente: el tatuador lo restaura
    Confirmada --> Completada: el tatuador lo da por hecho
    Pendiente --> Cancelada
    Confirmada --> Cancelada
    Completada --> [*]
    Cancelada --> [*]
```

Los turnos Pendiente, Confirmada y Propuesta ocupan la agenda del tatuador (no se ofrecen esos horarios y
no se puede pedir otro que se superponga); Cancelada y Rechazada la liberan.

## Solicitud para ser tatuador (`SolicitudStatus`, `tat_solicitud_tatuador.solicitud_status`)

```mermaid
stateDiagram-v2
    [*] --> pendiente: el usuario se registra como tatuador (queda Cliente)
    pendiente --> aprobada: un administrador aprueba (pasa a Tatuador con sus especialidades)
    pendiente --> rechazada: un administrador rechaza (con notas)
    aprobada --> [*]
    rechazada --> [*]
```

Una solicitud ya procesada no se puede volver a aprobar ni rechazar (400).

## Cuenta de Instagram de un tatuador (`tat_instagram_token`)

```mermaid
stateDiagram-v2
    [*] --> SinVincular
    SinVincular --> Vinculada: callback del OAuth con código válido
    Vinculada --> Vinculada: la tarea programada renueva el token (vence en menos de 30 días)
    Vinculada --> SinVincular: el tatuador desconecta
    Vinculada --> SinVincular: token vencido o revocado (tarea programada o sincronización)
```
