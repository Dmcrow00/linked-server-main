# Modelo entidad-relación

Las tablas con el mismo nombre que en la base. Se actualiza en la misma rama que la migración.
Las columnas de auditoría de `BaseEntity` (`version`, `creationuser`, `creationtimestamp`,
`modificationuser`, `modificationtimestamp`, `status`) están en casi todas las tablas y no se repiten.

## Usuarios (tablas `swe_*` de core-swe y perfil de linked)

`swe_*` las crearon las V1..V16 de linked y las completa core-swe (historial `flyway_historial_swe`,
ADR-0003 de dmc00-core). Lo propio de linked está en `tat_perfil_usuario` (V19, ADR-0004 de dmc00-core).
Las columnas viejas de `swe_usrapl` (`estudio`, `fecha_inicio_actividad`, `localidad`, `lat`, `lng`,
`instagram_handle`) siguen en la tabla hasta la versión siguiente, pero ya no se leen.

```mermaid
erDiagram
    swe_usrapl ||--|| swe_usrauth : "credenciales"
    swe_usrapl ||--o{ swe_usrrolapl : "roles"
    swe_rolapl ||--o{ swe_usrrolapl : ""
    swe_rolapl ||--o{ swe_accmodapl : "permisos (sin uso)"
    swe_itemmenu |o--o{ swe_itemmenu : "submenú"
    swe_usrapl ||--o| tat_perfil_usuario : "perfil de linked"
    tat_perfil_usuario ||--o{ tat_tatuador_especialidad : ""
    tat_especialidad ||--o{ tat_tatuador_especialidad : ""
    swe_usrapl {
        bigint id PK
        varchar username UK
        varchar email UK
        varchar realname
        varchar surname
        date birth_date
        text profile_photo
        timestamp delete_date
    }
    swe_usrauth {
        bigint id PK
        bigint id_usrapl FK
        varchar password "hash BCrypt/Argon2 o SIN-CONTRASENA"
        text reset_password_token
        timestamp reset_password_expires
    }
    swe_rolapl {
        bigint id PK
        varchar description UK "Administrador, Moderador, Tatuador, Cliente"
    }
    swe_usrrolapl {
        bigint id PK
        bigint id_usrapl FK
        bigint id_rolapl FK
    }
    swe_itemmenu {
        bigint id PK
        bigint id_supitemmenu FK
        varchar title
        varchar route
        int order_number
        varchar roles_permitidos "ids o descripciones de rol"
    }
    tat_perfil_usuario {
        bigint id_usrapl PK, FK
        varchar estudio
        date fecha_inicio_actividad
        varchar localidad
        double lat
        double lng
        varchar instagram_handle
    }
    tat_especialidad {
        bigint id PK
        varchar nombre UK
        text descripcion
    }
    tat_tatuador_especialidad {
        bigint id_tatuador PK, FK "swe_usrapl.id"
        bigint id_especialidad PK, FK
    }
```

## Turnos y agenda

```mermaid
erDiagram
    swe_usrapl ||--o{ tat_turno_sesion : "cliente"
    tat_turno_sesion ||--|{ tat_turno_tatuador : ""
    swe_usrapl ||--o{ tat_turno_tatuador : "tatuador"
    tat_turno_sesion ||--o{ tat_imagen_ref : "referencias"
    tat_turno_sesion ||--o{ tat_turno_mensajes : "chat"
    swe_usrapl ||--o{ tat_turno_mensajes : "autor"
    swe_usrapl ||--o{ tat_horario_habitual : "tatuador"
    tat_turno_sesion {
        bigint id PK
        bigint id_cliente FK
        varchar estado "EstadoTurno"
        timestamp fecha_hora_inicio
        timestamp fecha_hora_fin
        varchar descripcion_cliente
    }
    tat_turno_tatuador {
        bigint id_tatuador PK, FK
        bigint id_turno_sesion PK, FK
    }
    tat_imagen_ref {
        bigint id PK
        bigint id_turno_sesion FK
        text image_path "URL de Cloudinary"
    }
    tat_turno_mensajes {
        bigint id PK
        bigint id_turno_sesion FK
        bigint id_usuario_envia FK
        text texto
        boolean leido
    }
    tat_horario_habitual {
        bigint id PK
        bigint id_tatuador FK
        int dia_semana "0 = domingo ... 6 = sábado"
        time hora_inicio
        time hora_fin
        int duracion_turno_min
    }
```

## Portfolio, Instagram, solicitudes y soporte

```mermaid
erDiagram
    swe_usrapl ||--o{ tat_trabajo : "tatuador"
    tat_trabajo ||--o{ tat_trabajo_foto : ""
    tat_trabajo ||--o{ tat_trabajo_favorito : "likes"
    swe_usrapl ||--o{ tat_trabajo_favorito : "cliente"
    swe_usrapl ||--o| tat_instagram_token : "cuenta vinculada"
    swe_usrapl ||--o{ tat_solicitud_tatuador : "solicitante"
    hd_support_ticket ||--o{ hd_usr_st : ""
    swe_usrapl ||--o{ hd_usr_st : ""
    tat_trabajo {
        bigint id PK
        bigint id_tatuador FK
        text descripcion
        varchar instagram_media_id "si vino de Instagram"
    }
    tat_trabajo_foto {
        bigint id PK
        bigint id_trabajo FK
        text image_path
    }
    tat_trabajo_favorito {
        bigint id PK
        bigint id_cliente FK
        bigint id_trabajo FK
    }
    tat_instagram_token {
        bigint id PK
        bigint id_tatuador FK, UK
        varchar instagram_user_id
        text access_token
        timestamp token_expires_at
    }
    tat_solicitud_tatuador {
        bigint id PK
        bigint id_user FK
        varchar estudio
        jsonb especialidades_ids
        varchar solicitud_status "pendiente, aprobada, rechazada"
        text notas
        varchar admin_user
    }
    hd_support_ticket {
        bigint id PK
        boolean ticket_open
        text description
        varchar category
        varchar priority
        text admin_response
        varchar contact_email
    }
    hd_usr_st {
        bigint id_ticket FK
        bigint id_user FK
    }
    tat_ruta_local_retirada {
        bigint id PK
        varchar tabla
        bigint id_fila
        bigint id_padre
        text ruta "ADR-0001"
    }
```
