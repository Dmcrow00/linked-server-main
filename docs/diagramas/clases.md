# Diagramas de clases

Un diagrama por módulo. Lo que viene de `dmc00-core` (usuarios, login, menú, errores) está en
`dmc00-core/docs/diagramas/`.

## Usuarios y perfil

```mermaid
classDiagram
    direction LR
    class UserController
    class UserService {
        <<interface>>
        +register(CreateRequest) TokenResponse
        +registerTatuador(RegisterTatuadorRequest) TokenResponse
        +update(id, UpdateRequest) Response
        +updateProfilePhoto(id, archivo) Response
    }
    class UserServiceImpl
    class SweUsrService {
        <<core-swe>>
    }
    class AuthService {
        <<core-swe>>
    }
    class PerfilUsuario {
        Long id
        String estudio
        LocalDate fechaInicioActividad
        String localidad
        Double lat
        Double lng
        String instagramHandle
    }
    class SweUsrapl {
        <<core-swe>>
    }
    class AlmacenDeImagenes {
        <<interface>>
    }
    UserController --> UserService
    UserService <|.. UserServiceImpl
    UserServiceImpl --> SweUsrService : alta, roles, contraseña, baja
    UserServiceImpl --> AuthService : JWT del registro
    UserServiceImpl --> PerfilUsuario
    UserServiceImpl --> AlmacenDeImagenes : foto de perfil
    PerfilUsuario --> SweUsrapl : mismo id
```

## Instagram

```mermaid
classDiagram
    direction LR
    class InstagramController
    class SincronizacionDeInstagram {
        +sincronizarTodas()
        +alIniciarSesion(InicioDeSesion)
        +alVincularCuenta(CuentaVinculada)
    }
    class InstagramService {
        <<interface>>
        +initiateOAuth(tatuadorId) String
        +handleCallback(code, state)
        +syncPosts(tatuadorId) SincronizacionInstagramDto
        +mantener(tatuadorId)
    }
    class InstagramServiceImpl
    class InstagramApi {
        <<interface>>
    }
    class InstagramGraphApi
    class AlmacenDeImagenes {
        <<interface>>
    }
    class CloudinaryAlmacenDeImagenes
    class JwtConTokenEnInicioDeOAuth
    InstagramController --> InstagramService
    SincronizacionDeInstagram --> InstagramService
    InstagramService <|.. InstagramServiceImpl
    InstagramServiceImpl --> InstagramApi
    InstagramServiceImpl --> AlmacenDeImagenes
    InstagramApi <|.. InstagramGraphApi
    AlmacenDeImagenes <|.. CloudinaryAlmacenDeImagenes
```

`InstagramApi` y `AlmacenDeImagenes` son puertos: los tests usan `InstagramApiFalsa` y un almacén falso
(`DoblesDePrueba`).

## Turnos y agenda

```mermaid
classDiagram
    direction LR
    class TurnosController
    class AgendaController
    class TurnosService {
        <<interface>>
    }
    class AgendaService {
        <<interface>>
    }
    class TurnosServiceImpl
    class AgendaServiceImpl
    class TurnoSesion {
        String estado
        LocalDateTime fechaHoraInicio
        LocalDateTime fechaHoraFin
    }
    class EstadoTurno {
        <<enumeration>>
        +esFinal() boolean
        +puedePasarA(EstadoTurno) boolean
    }
    class TurnoTatuador
    class TurnoMensaje
    class ImagenRef
    class HorarioHabitual
    TurnosController --> TurnosService
    AgendaController --> AgendaService
    TurnosService <|.. TurnosServiceImpl
    AgendaService <|.. AgendaServiceImpl
    TurnosServiceImpl --> TurnoSesion
    TurnoSesion --> EstadoTurno
    TurnoSesion "1" --> "*" TurnoTatuador
    TurnoSesion "1" --> "*" TurnoMensaje
    TurnoSesion "1" --> "*" ImagenRef
    AgendaServiceImpl --> HorarioHabitual
```

## Trabajos, búsqueda y solicitudes

```mermaid
classDiagram
    direction LR
    class TrabajosService {
        <<interface>>
    }
    class BusquedaService {
        <<interface>>
    }
    class SolicitudTatuadorService {
        <<interface>>
    }
    class TrabajosServiceImpl
    class BusquedaServiceImpl
    class SolicitudTatuadorServiceImpl
    class Trabajo
    class TrabajoFoto
    class TrabajoFavorito
    class SolicitudTatuador
    class PerfilUsuarioRepository {
        +findAllTatuadoresConEspecialidades()
        +porUsuario(ids) Map
    }
    TrabajosService <|.. TrabajosServiceImpl
    BusquedaService <|.. BusquedaServiceImpl
    SolicitudTatuadorService <|.. SolicitudTatuadorServiceImpl
    TrabajosServiceImpl --> Trabajo
    Trabajo "1" --> "*" TrabajoFoto
    Trabajo "1" --> "*" TrabajoFavorito
    TrabajosServiceImpl --> PerfilUsuarioRepository : estudio del tatuador
    BusquedaServiceImpl --> PerfilUsuarioRepository
    SolicitudTatuadorServiceImpl --> SolicitudTatuador
```
