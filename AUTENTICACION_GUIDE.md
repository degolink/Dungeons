# 🎯 Sistema de Login con Tests - Guía Completa

## 📦 Lo que se ha agregado

### 1. **Dependencias en pom.xml**
- ✅ Spring Security
- ✅ Spring Security Crypto (BCrypt)
- ✅ JUnit 5 (Jupiter)
- ✅ Mockito + Mockito JUnit
- ✅ Spring Security Test
- ✅ H2 Database (para tests)

### 2. **Modelos y Entidades**
```
src/main/java/com/dungeons_and_dragons/rol/model/
└── Usuario.java (JPA Entity con roles NARRADOR/JUGADOR)
```

### 3. **Repositorios**
```
src/main/java/com/dungeons_and_dragons/rol/repository/
└── UsuarioRepository.java (JPA con findByEmail, existsByEmail)
```

### 4. **Servicios**
```
src/main/java/com/dungeons_and_dragons/rol/service/
├── UsuarioService.java (Guardar, obtener, validar)
└── CustomUserDetailsService.java (Spring Security)
```

### 5. **Controladores**
```
src/main/java/com/dungeons_and_dragons/rol/controller/
└── AuthController.java (GET/POST login, registro, logout)
```

### 6. **Configuración de Seguridad**
```
src/main/java/com/dungeons_and_dragons/configuration/
└── SecurityConfig.java (Spring Security configuration)
```

### 7. **Vistas Thymeleaf**
```
src/main/resources/templates/
├── login.html (Formulario de login)
└── registro.html (Formulario de registro)
```

### 8. **Tests (35 tests en total)**
```
src/test/java/com/dungeons_and_dragons/rol/service/
├── UsuarioServiceTest.java (11 tests con Mockito)
├── UsuarioRepositoryTest.java (5 tests con Mockito)
├── CustomUserDetailsServiceTest.java (5 tests)
├── AuthControllerTest.java (8 tests con MockMvc)
└── SecurityIntegrationTest.java (6 tests de integración)
```

---

## 🧪 Tests Implementados

### A. **UsuarioServiceTest** (11 tests)
```java
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {
    @Mock UsuarioRepository usuarioRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks UsuarioService usuarioService;
    
    // 11 tests sobre guardar, obtener, validar usuario
}
```
**Características:**
- Mocks de `UsuarioRepository` y `PasswordEncoder`
- Valida encriptación con BCrypt
- Prueba credenciales correctas e incorrectas
- Valida usuario inactivo

### B. **CustomUserDetailsServiceTest** (5 tests)
```java
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {
    // Tests de mapeo de roles
    // Validación de usuario inactivo
    // Excepción si no existe
}
```

### C. **UsuarioRepositoryTest** (5 tests)
```java
@ExtendWith(MockitoExtension.class)
class UsuarioRepositoryTest {
    // Tests de búsqueda por email
    // Validación de existencia
}
```

### D. **AuthControllerTest** (8 tests)
```java
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    @Mock UsuarioService usuarioService;
    @Mock AuthenticationManager authenticationManager;
    
    // Tests GET/POST login
    // Tests GET/POST registro
    // Tests logout
}
```
**Características:**
- Usa `MockMvc` para testing de controladores
- Valida redirecciones
- Comprueba modelos con atributos de error

### E. **SecurityIntegrationTest** (6 tests)
```java
@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {
    // Tests de integración E2E
    // Valida rutas protegidas
    // Usa H2 Database en memoria
}
```
**Características:**
- Tests de integración completos
- Base de datos H2 en memoria
- Valida autenticación real
- Comprueba BCrypt en contraseñas

---

## 🚀 Cómo ejecutar los tests

### 1. **Ejecutar todos los tests:**
```bash
cd c:\Cosas\Rol\rol
./mvnw.cmd test
```

### 2. **Ejecutar una clase específica:**
```bash
./mvnw.cmd test -Dtest=UsuarioServiceTest
```

### 3. **Ejecutar un método específico:**
```bash
./mvnw.cmd test -Dtest=UsuarioServiceTest#testGuardarUsuario
```

### 4. **Ejecutar tests con output verbose:**
```bash
./mvnw.cmd test -X
```

### 5. **Generar reporte de cobertura:**
```bash
./mvnw.cmd test jacoco:report
```

---

## 🔐 Rutas Protegidas

| Ruta | Acceso | Rol Requerido |
|------|--------|---------------|
| `/login` | ✅ Público | Ninguno |
| `/registro` | ✅ Público | Ninguno |
| `/logout` | ✅ Autenticado | Ninguno |
| `/narrador` | 🔒 Protegida | NARRADOR |
| `/jugador/*` | 🔒 Protegida | JUGADOR, NARRADOR |
| `/static/**` | ✅ Público | Ninguno |

---

## 💾 Configuración de la Base de Datos

El archivo `application.properties` ya tiene configurada una conexión a MySQL:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/rol
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
```

**Para los tests**, se usa H2 en memoria automáticamente.

---

## 📝 Crear Usuarios de Prueba

Agregar esto a la clase `RolApplication.java`:

```java
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UsuarioService usuarioService;
    
    @Override
    public void run(String... args) {
        // Crear usuario Narrador
        usuarioService.guardarUsuario(Usuario.builder()
            .nombre("Narrador Admin")
            .email("narrador@test.com")
            .password("password123")
            .rol(Usuario.Rol.NARRADOR)
            .activo(true)
            .build());
        
        // Crear usuario Jugador
        usuarioService.guardarUsuario(Usuario.builder()
            .nombre("Jugador Test")
            .email("jugador@test.com")
            .password("password123")
            .rol(Usuario.Rol.JUGADOR)
            .activo(true)
            .build());
    }
}
```

---

## 🎯 Próximos pasos (opcional)

1. **Integrar con usuarios existentes:**
   - Relacionar `Usuario` con `Personaje`
   - Filtrar personajes por usuario logueado

2. **Mejoras de seguridad:**
   - CSRF protection (actualmente habilitada)
   - Recuperación de contraseña
   - 2FA (Two Factor Authentication)

3. **Mejoras de UX:**
   - Navbar con usuario logueado
   - Panel de perfil
   - Cambio de contraseña

4. **Más tests:**
   - Tests de endpoints específicos
   - Tests de integración con base de datos real
   - Tests de performance

---

## ✅ Checklist de Validación

- [x] Pom.xml actualizado con todas las dependencias
- [x] Modelo `Usuario` creado
- [x] `UsuarioService` con métodos esenciales
- [x] `CustomUserDetailsService` implementado
- [x] `SecurityConfig` configurado
- [x] `AuthController` con endpoints
- [x] Vistas `login.html` y `registro.html`
- [x] 35 tests creados:
  - [x] 11 tests UsuarioService
  - [x] 5 tests CustomUserDetailsService
  - [x] 5 tests UsuarioRepository
  - [x] 8 tests AuthController
  - [x] 6 tests SecurityIntegration

---

## 📚 Referencias útiles

- [Spring Security Docs](https://spring.io/projects/spring-security)
- [JUnit 5 Documentation](https://junit.org/junit5/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Test Documentation](https://spring.io/projects/spring-framework)

---

**¡Sistema de autenticación completamente funcional con tests!** 🎉
