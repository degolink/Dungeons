package com.dungeons_and_dragons.rol;

import static org.junit.jupiter.api.Assertions.*;
import com.dungeons_and_dragons.rol.controller.NarradorController;
import com.dungeons_and_dragons.rol.model.Personaje;
import com.dungeons_and_dragons.rol.service.PersonajeService;
import org.springframework.ui.Model;
import org.springframework.ui.ConcurrentModel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
class NarradorControllerTests {

     @Autowired
    private NarradorController controller;

    @Autowired
    private PersonajeService personajeService;

    @Test
    void contextLoads() {
    }

    //HOME

    @Test
    void home_deberiaRedirigir() {
        String resultado = controller.home();
        assertEquals("redirect:/narrador", resultado);
    }

    //TEST CREAR PERSONAJE

    @Test
    void crearPersonaje_deberiaGuardarYRedirigir() {

        Personaje personaje = new Personaje();
        personaje.setOro(50);

        String resultado = controller.crearPersonaje(personaje);

        assertEquals("redirect:/narrador", resultado);
    }

    //TEST BORRAR PERSONAJE

    @Test
    void borrarPersonaje_deberiaEliminar() {

        Personaje personaje = new Personaje();
        personaje = personajeService.guardar(personaje);

        controller.borrarPersonaje(personaje.getId());

        assertFalse(personajeService.buscarPorId(personaje.getId()).isPresent());
    }


    //ACTUALIZAR MONEDA

    @Test
    void actualizarMoneda_deberiaActualizarOro(){

        //Crea personaje de prueba
        Personaje personaje = new Personaje();
        personaje.setOro(0);

        //Guardar personaje en DB
        personajeService.guardar(personaje);

        //Ejecutar método correctamente 
        Personaje actualizado = controller.actualizarMoneda(personaje.getId(), "oro", 100);

        //Verificar resultado
        assertEquals(100, actualizado.getOro());
    }
    

    //Test invalido

    @Test
    void actualizarMoneda_deberiaRetornar400_tipoInvalido() {


    // Crear personaje válido
    Personaje personaje = new Personaje();
    personaje.setOro(0);

    // Guardar en BD
    Personaje guardado = personajeService.guardar(personaje);

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            controller.actualizarMoneda(guardado.getId(), "diamante", 100);
        });

        assertEquals(HttpStatus.BAD_REQUEST, ((ResponseStatusException) exception).getStatusCode());
    }

    // Test de error

    @Test
    void actualizarMoneda_deberiaRetornar404_siNoExiste() {
        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            controller.actualizarMoneda(999L, "oro", 100);
        });

        assertEquals(HttpStatus.NOT_FOUND, ((ResponseStatusException) exception).getStatusCode());
    }

    //MOSTRAR PANTALLA

    @Test 
    void mostrarPantalla_deberiaRetornarVistaNarrador() {
        
        Model model = new ConcurrentModel();

        String vista = controller.mostrarPantalla(model);

        assertEquals("narrador", vista);
        assertTrue(model.containsAttribute("personajes"));
        assertTrue(model.containsAttribute("newPersonaje"));
    }


   // MÉTODO ADD_DATA
  /* @PostMapping("addData")
   public String postMethodName(@RequestBody String entity) {
    personajeService.guardar(new Personaje());
    return entity */
   }






