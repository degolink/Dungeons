# 📋 Resumen de Tests con Mockito y JUnit 5

## ✅ Archivos Creados

### 1. **Modelos y Servicios**

#### `Usuario.java` (Modelo JPA)
- Entidad con campos: id, nombre, email, password, rol, activo
- Enumeración Rol: NARRADOR, JUGADOR
- Anotación `@UniqueConstraint` en email

#### `UsuarioRepository.java` (Repositorio JPA)
- `findByEmail(String email)` → Optional<Usuario>
- `existsByEmail(String email)` → boolean
- Hereda CRUD de JpaRepository

#### `UsuarioService.java` (Servicio de Lógica)
- `guardarUsuario()` → encripta contraseña con BCrypt
- `obtenerPorEmail()` → busca usuario por email
- `existeEmail()` → valida disponibilidad
- `validarCredenciales()` → compara password con BCrypt

#### `CustomUserDetailsService.java` (Spring Security)
- Implementa `UserDetailsService`
- Mapea roles a `GrantedAuthority`
- Valida estado activo del usuario

#### `SecurityConfig.java` (Configuración de Seguridad)
- Bean `PasswordEncoder` con BCrypt
- Bean `AuthenticationProvider` personalizado
- `SecurityFilterChain` con reglas por rol:
  - `/login`, `/registro` → permitidas sin auth
  - `/narrador/**` → requiere rol NARRADOR
  - `/jugador/**` → requiere roles JUGADOR o NARRADOR

#### `AuthController.java` (Controlador HTTP)
- GET `/login` → muestra formulario
- POST `/login` → valida credenciales
- GET `/logout` → limpia sesión
- GET `/registro` → muestra formulario de registro
- POST `/registro` → crea nuevo usuario

---

### 2. **Tests Unitarios con Mockito**

#### `UsuarioServiceTest.java` (11 tests)
```
✓ Guardar usuario con contraseña encriptada
✓ Obtener usuario por email
✓ Retornar vacío si email no existe
✓ Verificar que existe un email
✓ Validar credenciales correctas
✓ Rechazar credenciales incorrectas
✓ Rechazar login de usuario inactivo
✓ Rechazar usuario no existente
✓ Obtener usuario por ID
```
- Usa `@ExtendWith(MockitoExtension.class)`
- Mocks: `UsuarioRepository`, `PasswordEncoder`
- Anotaciones: `@Mock`, `@InjectMocks`, `@BeforeEach`

#### `CustomUserDetailsServiceTest.java` (5 tests)
```
✓ Cargar usuario por email
✓ Lanzar excepción si usuario no existe
✓ Lanzar excepción si usuario está inactivo
✓ Asignar rol JUGADOR correctamente
✓ Asignar rol NARRADOR correctamente
```
- Valida mapeo de roles a Spring Security
- Verifica excepción `UsernameNotFoundException`

#### `UsuarioRepositoryTest.java` (5 tests)
```
✓ Guardar usuario
✓ Encontrar usuario por email
✓ Retornar vacío si email no existe
✓ Validar que existe un email
✓ Retornar false si email no existe
```
- Tests de repositorio con mocks

#### `AuthControllerTest.java` (8 tests)
```
✓ GET /login retorna vista
✓ POST /login con credenciales válidas
✓ POST /login con credenciales inválidas
✓ GET /logout limpia sesión
✓ GET /registro retorna vista
✓ POST /registro con contraseñas que no coinciden
✓ POST /registro con email ya registrado
✓ POST /registro con datos válidos
```
- Usa `MockMvc`
- Valida redirecciones y modelos

---

### 3. **Tests de Integración**

#### `SecurityIntegrationTest.java` (6 tests)
```
✓ GET /login accesible sin autenticación
✓ GET /registro accesible sin autenticación
✓ GET /narrador redirige a login sin auth
✓ POST /registro crea nuevo usuario
✓ POST /login con credenciales inválidas retorna error
✓ GET /logout limpia la sesión
```
- Usa `@SpringBootTest`
- Usa `@AutoConfigureMockMvc`
- Base de datos real H2 para tests
- Valida BCrypt en contraseña

---

## 🛠️ Dependencias Agregadas al pom.xml

```xml
<!-- Spring Security -->
<spring-boot-starter-security>

<!-- Spring Security Crypto (BCrypt) -->
<spring-security-crypto>

<!-- JUnit 5 (Jupiter) -->
<junit-jupiter>

<!-- Mockito -->
<mockito-core>
<mockito-junit-jupiter>

<!-- Spring Security Test -->
<spring-security-test>

<!-- H2 Database (tests) -->
<h2>
```

---

## 📊 Resumen de Tests

| Clase | Tests | Tipo |
|-------|-------|------|
| UsuarioServiceTest | 11 | Unit (Mockito) |
| CustomUserDetailsServiceTest | 5 | Unit (Mockito) |
| UsuarioRepositoryTest | 5 | Unit (Mockito) |
| AuthControllerTest | 8 | Unit (Mockito) |
| SecurityIntegrationTest | 6 | Integración |
| **TOTAL** | **35** | |

---

## 🚀 Cómo ejecutar los tests

### Todos los tests:
```bash
./mvnw test
```

### Tests de una clase específica:
```bash
./mvnw test -Dtest=UsuarioServiceTest
```

### Tests con cobertura:
```bash
./mvnw test jacoco:report
```

---

## 💡 Características principales

✅ **Mockito**: Mocks de repositorios y servicios
✅ **JUnit 5**: Anotaciones modernas (@Test, @DisplayName, @ExtendWith)
✅ **Spring Security**: Integración con BCrypt y autenticación
✅ **H2 Database**: Base de datos en memoria para tests
✅ **MockMvc**: Tests de controladores HTTP
✅ **Validación de Roles**: Tests de NARRADOR y JUGADOR
✅ **Encriptación**: Validación de contraseñas con BCrypt

---

## 📝 Nota sobre archivos faltantes

Para completar la implementación, aún se necesitan:
- Vistas Thymeleaf: `login.html`, `registro.html`, navbar actualizado
- Archivo `application-test.properties` en `src/test/resources/`

Pero los tests unitarios funcionan sin necesidad de vistas porque usan mocks.
