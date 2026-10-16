# Diagramas de secuencia

Los flujos que cruzan más de una capa o sistema.

## Login (y sincronización de Instagram de los tatuadores)

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant W as Cliente web
    participant A as API (core-swe)
    participant L as SincronizacionDeInstagram
    participant I as Instagram
    W->>A: GET /api/auth/public-key
    A-->>W: clave pública RSA
    U->>W: usuario y contraseña
    W->>A: POST /api/auth/login (contraseña cifrada RSA-OAEP)
    A->>A: valida reCAPTCHA, descifra, compara hash (BCrypt o Argon2 de Node)
    A-)L: evento InicioDeSesion
    A-->>W: JWT (sub, id, rol, roles)
    Note over L: en segundo plano (@Async), solo si es Tatuador
    L->>I: renueva el token si vence pronto y lee las publicaciones
    L->>L: importa las nuevas y da de baja las borradas
```

## Reserva de un turno

```mermaid
sequenceDiagram
    autonumber
    actor C as Cliente
    participant W as Cliente web
    participant A as API
    participant N as Cloudinary
    actor T as Tatuador
    W->>A: GET /api/agenda/{tatuador}/fechas-bloqueadas?desde&hasta
    W->>A: GET /api/agenda/{tatuador}/slots-dia?fecha
    A-->>W: horarios libres (horario habitual menos turnos que ocupan agenda)
    C->>W: elige horario, describe el tatuaje, adjunta hasta 3 imágenes
    W->>A: POST /api/turnos/solicitar (multipart)
    A->>A: valida fechas y que no se superponga con otro turno del tatuador
    A->>N: sube las imágenes de referencia
    A-->>W: turno Pendiente
    T->>W: abre "mis turnos"
    W->>A: GET /api/turnos/mis-turnos
    T->>W: confirma, rechaza o propone otra fecha
    W->>A: PATCH /api/turnos/gestionar/{id} {estado}
    A-->>W: turno con el estado nuevo (409 si ya estaba Completada o Cancelada)
    C->>A: POST /api/turnos/{id}/mensajes (chat entre cliente y tatuador)
```

## Vinculación de Instagram

```mermaid
sequenceDiagram
    autonumber
    actor T as Tatuador
    participant W as Cliente web
    participant A as API
    participant I as Instagram
    participant N as Cloudinary
    T->>W: "Vincular Instagram"
    W->>A: navegación a /api/instagram/auth?token=JWT
    Note over A: JwtConTokenEnInicioDeOAuth acepta el token por query solo en esta ruta
    A-->>W: 302 a Instagram con state firmado (vence en 10 min)
    T->>I: autoriza
    I-->>A: GET /api/instagram/callback?code&state
    A->>A: valida el state (firma, vencimiento y prefijo propio)
    A->>I: canjea el código por un token de larga duración (60 días)
    A->>I: datos de la cuenta (usuario y foto)
    A->>N: copia la foto de perfil
    A->>A: guarda token, usuario de Instagram y foto (transacción corta)
    A-)A: evento CuentaVinculada: primera sincronización en segundo plano
    A-->>W: 302 a /info?instagram=success
```

## Sincronización programada de Instagram (cada 2 h)

```mermaid
sequenceDiagram
    autonumber
    participant J as SincronizacionDeInstagram
    participant S as InstagramService
    participant I as Instagram
    participant N as Cloudinary
    participant D as Base
    J->>S: tatuadoresVinculados()
    loop por cada tatuador
        J->>S: mantener(tatuador)
        alt token vencido
            S->>D: borra el token y limpia usuario y foto de Instagram
        else vence en menos de 30 días
            S->>I: renueva el token
            S->>D: guarda el token nuevo
        end
        S->>I: publicaciones (todas las páginas, carruseles con sus imágenes)
        S->>D: da de baja los trabajos cuya publicación ya no está
        S->>N: copia las imágenes de las publicaciones nuevas
        S->>D: guarda un trabajo por publicación nueva
    end
```
