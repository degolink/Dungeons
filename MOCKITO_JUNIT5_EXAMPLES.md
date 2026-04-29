# 🧪 Ejemplos de Mockito y JUnit 5 en el Proyecto

## 1️⃣ Anotaciones JUnit 5 Utilizadas

### `@ExtendWith(MockitoExtension.class)`
Integra Mockito con JUnit 5. Reemplaza a `@RunWith(MockitoJUnitRunner.class)` de JUnit 4.

```java
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {
    // Los mocks se inicializan automáticamente
}
```

### `@Mock`
Crea un mock automáticamente.

```java
@Mock
private UsuarioRepository usuarioRepository;

// Es equivalente a:
// usuarioRepository = Mockito.mock(UsuarioRepository.class);
```

### `@InjectMocks`
Inyecta mocks automáticamente en la clase bajo test.

```java
@InjectMocks
private UsuarioService usuarioService;

// Inyecta automáticamente:
// - usuarioRepository
// - passwordEncoder
// En el constructor de UsuarioService
```

### `@BeforeEach`
Ejecuta antes de cada test (reemplaza `@Before` de JUnit 4).

```java
@BeforeEach
void setUp() {
    usuario = Usuario.builder()
            .id(1L)
            .email("test@example.com")
            .build();
}
```

### `@DisplayName`
Nombre descriptivo del test en reportes.

```java
@Test
@DisplayName("Debería guardar usuario con contraseña encriptada")
void testGuardarUsuario() {
    // ...
}
```

---

## 2️⃣ Patrones de Mockito Utilizados

### Patrón: `when...thenReturn`
Mock que retorna un valor específico.

```java
@Test
void testObtenerPorEmail() {
    when(usuarioRepository.findByEmail("test@example.com"))
            .thenReturn(Optional.of(usuario));

    Optional<Usuario> resultado = usuarioService.obtenerPorEmail("test@example.com");

    assertTrue(resultado.isPresent());
}
```

### Patrón: `verify`
Verifica que un método fue llamado.

```java
@Test
void testVerificarLlamada() {
    usuarioService.guardarUsuario(usuario);

    // Verifica que se llamó 1 vez
    verify(usuarioRepository, times(1)).save(usuario);
}
```

### Patrón: `ArgumentMatchers`
Usa criterios flexibles en lugar de valores exactos.

```java
@Test
void testConArgumentMatcher() {
    when(usuarioRepository.save(any(Usuario.class)))
            .thenReturn(usuario);

    // Acepta cualquier Usuario, no solo el específico
    usuarioService.guardarUsuario(new Usuario());
}
```

### Patrón: `never` / `atLeast` / `atMost`
Verifica la frecuencia de llamadas.

```java
@Test
void testVerificarFrecuencia() {
    usuarioService.obtenerPorEmail("noexiste@example.com");

    // Verifica que NUNCA se guardó
    verify(usuarioRepository, never()).save(any());

    // O verifica al menos 1 vez
    verify(usuarioRepository, atLeast(1)).findByEmail(anyString());

    // O verifica máximo 3 veces
    verify(usuarioRepository, atMost(3)).findByEmail(anyString());
}
```

### Patrón: `thenThrow`
Mock que lanza excepción.

```java
@Test
void testMockConExcepcion() {
    when(usuarioRepository.findByEmail("test@example.com"))
            .thenThrow(new RuntimeException("DB error"));

    assertThrows(RuntimeException.class, () -> {
        usuarioService.obtenerPorEmail("test@example.com");
    });
}
```

---

## 3️⃣ Ejemplos del Proyecto

### Ejemplo 1: Test Básico con Mockito

**Archivo:** `UsuarioServiceTest.java`

```java
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .email("test@example.com")
                .password("password123")
                .build();
    }

    @Test
    @DisplayName("Debería guardar usuario con contraseña encriptada")
    void testGuardarUsuario() {
        // ARRANGE
        when(passwordEncoder.encode("password123"))
                .thenReturn("encryptedPassword");
        when(usuarioRepository.save(any(Usuario.class)))
                .thenReturn(usuario);

        // ACT
        Usuario resultado = usuarioService.guardarUsuario(usuario);

        // ASSERT
        assertNotNull(resultado);
        assertEquals("test@example.com", resultado.getEmail());
        
        // VERIFY
        verify(passwordEncoder, times(1)).encode("password123");
        verify(usuarioRepository, times(1)).save(usuario);
    }
}
```

**Patrones usados:**
- ✅ `@Mock` - Mockeado
- ✅ `@InjectMocks` - Inyectado
- ✅ `when...thenReturn` - Configurar mock
- ✅ `verify` - Verificar llamada
- ✅ Patrón AAA (Arrange-Act-Assert)

---

### Ejemplo 2: Test con Exception

**Archivo:** `CustomUserDetailsServiceTest.java`

```java
@Test
@DisplayName("Debería lanzar excepción cuando el usuario está inactivo")
void testLoadUserByUsernameUsuarioInactivo() {
    // ARRANGE
    usuario.setActivo(false);
    when(usuarioRepository.findByEmail("test@example.com"))
            .thenReturn(Optional.of(usuario));

    // ACT & ASSERT
    assertThrows(UsernameNotFoundException.class, () -> {
        customUserDetailsService.loadUserByUsername("test@example.com");
    });

    // VERIFY
    verify(usuarioRepository, times(1)).findByEmail("test@example.com");
}
```

**Patrones usados:**
- ✅ `assertThrows` - Verificar excepción
- ✅ `Optional.of` - Mock con valor
- ✅ `verify` - Confirmar llamada

---

### Ejemplo 3: Test de Controlador con MockMvc

**Archivo:** `AuthControllerTest.java`

```java
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .build();
    }

    @Test
    @DisplayName("POST /login con credenciales inválidas retorna error")
    void testPostLoginCredencialesInvalidas() {
        // ARRANGE
        when(usuarioService.validarCredenciales("test@example.com", "wrongPassword"))
                .thenReturn(false);

        // ACT & ASSERT
        String resultado = authController.processLogin(
                "test@example.com",
                "wrongPassword",
                model,
                null
        );

        // VERIFY
        assertEquals("login", resultado);
        verify(model, times(1))
                .addAttribute("error", "Email o contraseña incorrectos");
    }
}
```

**Patrones usados:**
- ✅ `MockMvcBuilders` - Construir MockMvc
- ✅ `assertEquals` - Verificar string retornado
- ✅ `verify` - Verificar atributo modelo

---

### Ejemplo 4: Test de Integración con SpringBootTest

**Archivo:** `SecurityIntegrationTest.java`

```java
@SpringBootTest  // Carga contexto completo
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("POST /registro debería crear nuevo usuario")
    void testRegistroCrearNuevoUsuario() throws Exception {
        // ACT & ASSERT
        mockMvc.perform(post("/registro")
                .with(csrf())  // Token CSRF
                .param("nombre", "Nuevo Usuario")
                .param("email", "nuevo@example.com")
                .param("password", "password123")
                .param("confirmPassword", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        // VERIFY en base de datos real (no mock!)
        Usuario usuario = usuarioRepository
                .findByEmail("nuevo@example.com")
                .orElse(null);
        
        assertNotNull(usuario);
        assertTrue(passwordEncoder.matches("password123", usuario.getPassword()));
    }
}
```

**Patrones usados:**
- ✅ `@SpringBootTest` - Test de integración
- ✅ `@AutoConfigureMockMvc` - MockMvc automático
- ✅ `@Autowired` - Inyección real de beans
- ✅ `.with(csrf())` - Token CSRF
- ✅ `andExpect()` - Validación de respuesta

---

## 4️⃣ Comparación JUnit 4 vs JUnit 5

| Feature | JUnit 4 | JUnit 5 |
|---------|---------|---------|
| Anotación de clase | `@RunWith(MockitoJUnitRunner.class)` | `@ExtendWith(MockitoExtension.class)` |
| Antes de cada test | `@Before` | `@BeforeEach` |
| Después de cada test | `@After` | `@AfterEach` |
| Antes de todos | `@BeforeClass` | `@BeforeAll` |
| Después de todos | `@AfterClass` | `@AfterAll` |
| Nombre descriptivo | Nombre del método | `@DisplayName` |
| Parametrizado | `@Parameterized` | `@ParameterizedTest` |

---

## 5️⃣ Buenas Prácticas Implementadas

### ✅ Patrón AAA (Arrange-Act-Assert)
```java
@Test
void testEjemplo() {
    // ARRANGE - Preparar datos
    when(mock.method()).thenReturn(value);
    
    // ACT - Ejecutar
    Object resultado = servicio.operacion();
    
    // ASSERT - Verificar
    assertEquals(expected, resultado);
}
```

### ✅ Nombres Descriptivos
```java
@DisplayName("Debería guardar usuario con contraseña encriptada")
void testGuardarUsuario() { }
```

### ✅ Usar `@BeforeEach` para Setup Común
```java
@BeforeEach
void setUp() {
    usuario = new Usuario();
    // Evita duplicación en cada test
}
```

### ✅ Verificar Comportamiento, No Implementación
```java
// ✅ BIEN - Verifica el comportamiento
verify(usuarioRepository, times(1)).save(usuario);

// ❌ MAL - Verifica detalles internos
verify(passwordEncoder, times(2)).encode(anyString());
```

### ✅ Tests Independientes
```java
// Cada test debe ser independiente
// No hay dependencia entre tests
// @BeforeEach reinicia el estado
```

---

## 6️⃣ Ejecutar Tests Específicos

```bash
# Todos los tests
./mvnw test

# Clase específica
./mvnw test -Dtest=UsuarioServiceTest

# Método específico
./mvnw test -Dtest=UsuarioServiceTest#testGuardarUsuario

# Pattern matching
./mvnw test -Dtest=*ServiceTest

# Con output verbose
./mvnw test -X
```

---

## 📚 Resumen

- **JUnit 5**: Framework de testing moderna con extensiones
- **Mockito**: Librería para crear mocks y verificar comportamiento
- **MockMvc**: Testing de controllers web
- **@ExtendWith**: Integra Mockito con JUnit 5
- **when/verify**: Configura y verifica mocks
- **SpringBootTest**: Tests de integración completos

**✅ 35 tests implementados siguiendo estas prácticas!**
