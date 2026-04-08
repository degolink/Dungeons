package com.dungeons_and_dragons.rol.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.ConcurrentModel;

import com.dungeons_and_dragons.rol.model.Batalla;
import com.dungeons_and_dragons.rol.service.BatallaService;

class BatallaControllerTest {

    @Mock
    private BatallaService batallaService;

    private BatallaController batallaController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        batallaController = new BatallaController(batallaService);
    }

    @Test
    void deberiaMostrarVistaDeBatalla() {
        Batalla batalla = new Batalla();
        batalla.setId(1L);
        batalla.setNombre("Emboscada en el bosque");

        when(batallaService.buscarPorId(1L)).thenReturn(batalla);

        ConcurrentModel model = new ConcurrentModel();
        String vista = batallaController.mostrarBatalla(1L, model);

        assertEquals("batalla", vista);
        assertEquals(batalla, model.getAttribute("batalla"));
    }

    @Test
    void deberiaRedirigirALaBatallaDemo() {
        Batalla batalla = new Batalla();
        batalla.setId(3L);

        when(batallaService.obtenerOCrearBatallaDemo()).thenReturn(batalla);

        String redirect = batallaController.irABatallaDemo();

        assertEquals("redirect:/batallas/3", redirect);
    }

    @Test
    void deberiaReiniciarLaBatalla() {
        Batalla batalla = new Batalla();
        batalla.setId(1L);
        batalla.setEstado("PREPARADA");
        batalla.setRondaActual(0);

        when(batallaService.reiniciarBatalla(1L)).thenReturn(batalla);

        var response = batallaController.reiniciarBatalla(1L);

        assertEquals(1L, response.get("id"));
        assertEquals("PREPARADA", response.get("estado"));
        assertEquals(0, response.get("rondaActual"));
        assertEquals("Batalla reiniciada", response.get("mensaje"));
    }
}
