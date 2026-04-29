# 🎲 Rol - Gestor de partidas estilo Dungeons & Dragons

> **Autor:** master

Aplicación web desarrollada con **Spring Boot**, **Thymeleaf** y **MySQL** para gestionar personajes, villanos e interacciones de combate en una ambientación inspirada en **Dungeons & Dragons**.

## ✨ Características principales

- Gestión de **personajes** y **villanos**.
- Vista de **narrador** y vista de **jugador**.
- Sistema de **batalla por turnos**.
- Manejo de **hechizos**, **condiciones** e **inventario**.
- Tiradas de **iniciativa** y acciones durante el combate.
- Interfaz web renderizada con **Thymeleaf**.
- 🔐 **Sistema de autenticación** con login/registro
- 🌐 **Integración con la D&D 5e API** — importa hechizos y monstruos reales
- 🧪 **~75 Tests** unitarios y de integración con Mockito y JUnit 5

## 🛠️ Tecnologías usadas

- **Java 17**
- **Spring Boot 4.0.3**
- **Spring MVC**
- **Spring Data JPA**
- **Spring Security** (nuevo)
- **Thymeleaf**
- **MySQL**
- **JUnit 5** (nuevo)
- **Mockito** (nuevo)
- **Maven Wrapper** (`mvnw`, `mvnw.cmd`)

## 📁 Estructura general del proyecto

```text
src/main/java/com/dungeons_and_dragons/
├── configuration/
└── rol/
    ├── controller/
    ├── dnd/               ← integración D&D 5e API
    │   ├── DndApiService.java
    │   ├── DndApiController.java
    │   └── dto/
    ├── model/
    ├── repository/
    └── service/

src/main/resources/
├── application.properties
├── static/
└── templates/
```

## 🚀 Requisitos previos

Antes de ejecutar el proyecto, asegúrate de tener instalado:

1. **Java 17** o superior
2. **MySQL**
3. Opcionalmente, **Maven** (aunque no es necesario porque el proyecto incluye Maven Wrapper)

## ⚙️ Configuración de base de datos

El proyecto usa MySQL con esta configuración por defecto en `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/rol?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=
```

Crea la base de datos antes de iniciar la aplicación:

```sql
CREATE DATABASE rol CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

> Si tu usuario o contraseña de MySQL son distintos, actualízalos en `application.properties`.

## ▶️ Cómo ejecutar el proyecto

### En Windows

```powershell
mvnw.cmd spring-boot:run
```

### En Git Bash / Linux / macOS

```bash
./mvnw spring-boot:run
```

Una vez iniciado, abre en el navegador:

- `http://localhost:8080/login` (nuevo - página de login)
- `http://localhost:8080/` 
- `http://localhost:8080/narrador`
- `http://localhost:8080/batallas`

**Usuarios de prueba creados automáticamente:**
- Email: `narrador@test.com` | Password: `password123` (Rol: NARRADOR)
- Email: `jugador@test.com` | Password: `password123` (Rol: JUGADOR)

## 🧪 Ejecutar pruebas

### En Windows

```powershell
mvnw.cmd test
```

### En Git Bash / Linux / macOS

```bash
./mvnw test
```

### Opciones de testing

```bash
# Ejecutar una clase de test específica
mvnw.cmd test -Dtest=UsuarioServiceTest

# Ejecutar un método específico
mvnw.cmd test -Dtest=UsuarioServiceTest#testGuardarUsuario

# Con cobertura de código
mvnw.cmd test jacoco:report
```

**Total de tests:** ~75 tests unitarios e integración con Mockito y JUnit 5

## 🧭 Rutas principales

### Públicas (sin autenticación)
| Ruta | Descripción |
|------|-------------|
| `GET /login` | Formulario de login |
| `POST /login` | Procesar login |
| `GET /registro` | Formulario de registro |
| `POST /registro` | Procesar registro |
| `GET /logout` | Cerrar sesión |

### Protegidas (requieren autenticación)
| Ruta | Descripción | Rol |
|------|-------------|-----|
| `/` | Redirige a la vista principal | Autenticado |
| `/narrador` | Panel del narrador | NARRADOR |
| `/jugador/{id}` | Vista de jugador | JUGADOR, NARRADOR |
| `/villano/{id}` | Vista de villano | NARRADOR |
| `/batallas` | Gestión de batallas | NARRADOR |
| `/batallas/{id}` | Batalla específica | NARRADOR |

### API D&D 5e (requieren rol NARRADOR)
| Ruta | Método | Descripción |
|------|--------|-------------|
| `/api/dnd/hechizos` | GET | Lista hechizos disponibles en la D&D 5e API |
| `/api/dnd/hechizos/{index}` | GET | Detalle de un hechizo (ej: `fireball`) |
| `/api/dnd/importar/hechizo/{index}` | POST | Importa y guarda el hechizo en la BD |
| `/api/dnd/monstruos` | GET | Lista monstruos disponibles en la D&D 5e API |
| `/api/dnd/monstruos/{index}` | GET | Detalle de un monstruo (ej: `aboleth`) |
| `/api/dnd/importar/monstruo/{index}` | POST | Importa y guarda el monstruo como Villano en la BD |

## ℹ️ Comportamiento importante

Al arrancar la aplicación, `RolApplication` carga personajes iniciales y datos de ejemplo para facilitar las pruebas.

Además, durante el arranque se reinician varios datos en base de datos, por lo que este proyecto está pensado principalmente para **desarrollo, demo y aprendizaje**.

## 📌 Posibles mejoras futuras

- ✅ ~~Autenticación de usuarios~~ (implementado)
- ✅ ~~Integración D&D 5e API~~ (implementado)
- Persistencia más segura sin reinicio automático de datos
- Historial completo de combates
- Mejoras visuales en la interfaz
- Despliegue en nube o contenedores
- Recuperación de contraseña
- Two-Factor Authentication (2FA)
- Integración de Usuario con Personajes/Campañas

## 📚 Documentación adicional

- **[RESUMEN_FINAL.txt](RESUMEN_FINAL.txt)** - Resumen visual de todo lo implementado
- **[AUTENTICACION_GUIDE.md](AUTENTICACION_GUIDE.md)** - Guía completa del sistema de autenticación
- **[MOCKITO_JUNIT5_EXAMPLES.md](MOCKITO_JUNIT5_EXAMPLES.md)** - Ejemplos de testing con Mockito y JUnit 5
- **[ESTRUCTURA_ARCHIVOS.txt](ESTRUCTURA_ARCHIVOS.txt)** - Árbol de directorios del proyecto
- **[TESTS_SUMMARY.md](TESTS_SUMMARY.md)** - Resumen detallado de todos los tests

---

*Proyecto desarrollado por **master** como práctica de Spring Boot, testing y consumo de APIs REST.*
