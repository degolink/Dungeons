# 🎲 Rol - Gestor de partidas estilo Dungeons & Dragons

Aplicación web desarrollada con **Spring Boot**, **Thymeleaf** y **MySQL** para gestionar personajes, villanos e interacciones de combate en una ambientación inspirada en **Dungeons & Dragons**.

## ✨ Características principales

- Gestión de **personajes** y **villanos**.
- Vista de **narrador** y vista de **jugador**.
- Sistema de **batalla por turnos**.
- Manejo de **hechizos**, **condiciones** e **inventario**.
- Tiradas de **iniciativa** y acciones durante el combate.
- Interfaz web renderizada con **Thymeleaf**.

## 🛠️ Tecnologías usadas

- **Java 17**
- **Spring Boot 4.0.3**
- **Spring MVC**
- **Spring Data JPA**
- **Thymeleaf**
- **MySQL**
- **Maven Wrapper** (`mvnw`, `mvnw.cmd`)

## 📁 Estructura general del proyecto

```text
src/main/java/com/dungeons_and_dragons/
├── configuration/
└── rol/
    ├── controller/
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

- `http://localhost:8080/`
- `http://localhost:8080/narrador`
- `http://localhost:8080/batallas`

## 🧪 Ejecutar pruebas

### En Windows

```powershell
mvnw.cmd test
```

### En Git Bash / Linux / macOS

```bash
./mvnw test
```

## 🧭 Rutas principales

| Ruta | Descripción |
|------|-------------|
| `/` | Redirige a la vista principal del narrador |
| `/narrador` | Panel principal para gestionar personajes |
| `/jugador/{id}` | Vista individual de un personaje |
| `/villano/{id}` | Vista individual de un villano |
| `/batallas` | Abre o crea una batalla de demostración |
| `/batallas/{id}` | Muestra el estado de una batalla |

## ℹ️ Comportamiento importante

Al arrancar la aplicación, `RolApplication` carga personajes iniciales y datos de ejemplo para facilitar las pruebas.

Además, durante el arranque se reinician varios datos en base de datos, por lo que este proyecto está pensado principalmente para **desarrollo, demo y aprendizaje**.

## 📌 Posibles mejoras futuras

- Autenticación de usuarios
- Persistencia más segura sin reinicio automático de datos
- Historial completo de combates
- Mejoras visuales en la interfaz
- Despliegue en nube o contenedores

---

Si quieres, también puedo dejarte una versión más **profesional para GitHub** o una más **simple para entregar como proyecto escolar**.
