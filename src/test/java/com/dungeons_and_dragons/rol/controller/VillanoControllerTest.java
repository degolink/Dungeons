package com.dungeons_and_dragons.rol.controller;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.ConcurrentModel;

import com.dungeons_and_dragons.rol.model.Batalla;
import com.dungeons_and_dragons.rol.model.Villano;
import com.dungeons_and_dragons.rol.repository.VillanoRepository;
import com.dungeons_and_dragons.rol.service.BatallaService;
import com.dungeons_and_dragons.rol.service.DadosService;

class VillanoControllerTest {

    @Mock
    private VillanoRepository villanoRepository;

    @Mock
    private BatallaService batallaService;

    @Mock
    private DadosService dadosService;

    private VillanoController villanoController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        villanoController = new VillanoController(villanoRepository, batallaService, dadosService);
    }

    @Test
    void deberiaMostrarVistaVillanoConBatallaActiva() {
        Villano villano = new Villano();
        villano.setId(5L);
        villano.setNombre("Chamán oscuro");
        villano.setTurnoActual(true);

        Batalla batalla = new Batalla();
        batalla.setId(1L);
        batalla.setNombre("Asalto al campamento goblin");
        batalla.setEstado("EN_CURSO");
        batalla.getVillanos().add(villano);

        when(villanoRepository.findById(5L)).thenReturn(Optional.of(villano));
        when(batallaService.buscarActivaPorVillano(5L)).thenReturn(Optional.of(batalla));

        ConcurrentModel model = new ConcurrentModel();
        String vista = villanoController.mostrarVistaVillano(5L, model);

        assertEquals("villano", vista);
        assertEquals(villano, model.getAttribute("villano"));
        assertEquals(batalla, model.getAttribute("batallaActiva"));
        assertEquals(true, model.getAttribute("esTurnoVillano"));
    }

    @Test
    void deberiaAtacarDesdeLaVistaDelVillano() {
        Map<String, Object> resultado = Map.of(
                "evento", "Chamán oscuro atacó a Lia y causó 4 de daño.",
                "vidaRestante", 12,
                "objetivoId", 1L,
                "objetivoTipo", "personaje");

        when(batallaService.atacarDesdeVillano(5L, 1L)).thenReturn(resultado);

        Map<String, Object> response = villanoController.atacarDesdeVillano(5L, 1L);

        assertEquals("Chamán oscuro atacó a Lia y causó 4 de daño.", response.get("evento"));
        assertEquals(12, response.get("vidaRestante"));
    }
}
