package com.dungeons_and_dragons.rol.controller;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.ConcurrentModel;

import com.dungeons_and_dragons.rol.model.Batalla;
import com.dungeons_and_dragons.rol.model.Condicion;
import com.dungeons_and_dragons.rol.model.Hechizo;
import com.dungeons_and_dragons.rol.model.ItemBase;
import com.dungeons_and_dragons.rol.model.ItemInventario;
import com.dungeons_and_dragons.rol.model.Personaje;
import com.dungeons_and_dragons.rol.repository.ItemBaseRepository;
import com.dungeons_and_dragons.rol.repository.ItemInventarioRepository;
import com.dungeons_and_dragons.rol.service.BatallaService;
import com.dungeons_and_dragons.rol.service.DadosService;
import com.dungeons_and_dragons.rol.service.PersonajeService;

class NarradorControllerTest {

    @Mock
    private PersonajeService personajeService;

    @Mock
    private DadosService dadosService;

    @Mock
    private ItemInventarioRepository itemInventarioRepository;

    @Mock
    private ItemBaseRepository itemBaseRepository;

    @Mock
    private BatallaService batallaService;

    private NarradorController narradorController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        narradorController = new NarradorController(personajeService, dadosService, itemInventarioRepository, itemBaseRepository, batallaService);
    }

    @Test
    void deberiaTirarIniciativaParaUnPersonajeExistente() {
        Personaje personaje = new Personaje();
        personaje.setId(1L);
        personaje.setNombre("Lia");

        DadosService.ResultadoIniciativa resultado = new DadosService.ResultadoIniciativa("Lia", 12, 3, 15);

        when(personajeService.buscarPorId(1L)).thenReturn(Optional.of(personaje));
        when(dadosService.tirarIniciativa(personaje)).thenReturn(resultado);

        DadosService.ResultadoIniciativa response = narradorController.tirarIniciativa(1L);

        assertNotNull(response);
        assertEquals("Lia", response.getNombrePersonaje());
        assertEquals(15, response.getTotal());
    }

    @Test
    void deberiaActualizarMonedaSinPermitirValoresNegativos() {
        Personaje personaje = new Personaje();
        personaje.setId(1L);
        personaje.setCobre(0);
        personaje.setPlata(0);
        personaje.setOro(10);
        personaje.setPlatino(0);

        when(personajeService.buscarPorId(1L)).thenReturn(Optional.of(personaje));
        when(personajeService.guardar(personaje)).thenReturn(personaje);

        Map<String, Object> response = narradorController.actualizarMoneda(1L, "oro", -5);

        assertEquals(0, response.get("oro"));
    }

    @Test
    void deberiaActualizarEnergiaSinPermitirValoresNegativos() {
        Personaje personaje = new Personaje();
        personaje.setId(1L);
        personaje.setPuntosEnergia(8);

        when(personajeService.buscarPorId(1L)).thenReturn(Optional.of(personaje));
        when(personajeService.actualizarEnergia(personaje, -10)).thenAnswer(invocation -> {
            personaje.setPuntosEnergia(0);
            return personaje;
        });

        Map<String, Object> response = narradorController.actualizarEnergia(1L, -10);

        assertEquals(0, response.get("puntosEnergia"));
    }

    @Test
    void deberiaAgregarCondicionAlPersonaje() {
        Personaje personaje = new Personaje();
        personaje.setId(1L);
        personaje.setCondiciones(new ArrayList<>());

        when(personajeService.buscarPorId(1L)).thenReturn(Optional.of(personaje));
        when(personajeService.guardar(personaje)).thenAnswer(invocation -> {
            personaje.getCondiciones().get(0).setId(99L);
            return personaje;
        });

        Map<String, Object> response = narradorController.agregarCondicion(1L, "Aturdido", "Pierde el turno", 2);

        assertEquals(99L, response.get("id"));
        assertEquals("Aturdido", response.get("nombre"));
        assertEquals(1, personaje.getCondiciones().size());
    }

    @Test
    void deberiaEliminarCondicionDelPersonaje() {
        Condicion condicion = new Condicion();
        condicion.setId(8L);
        condicion.setNombre("Cegado");

        Personaje personaje = new Personaje();
        personaje.setId(1L);
        personaje.setCondiciones(new ArrayList<>());
        personaje.getCondiciones().add(condicion);

        when(personajeService.buscarPorId(1L)).thenReturn(Optional.of(personaje));
        when(personajeService.guardar(personaje)).thenReturn(personaje);

        Map<String, Object> response = narradorController.eliminarCondicion(1L, 8L);

        assertEquals(8L, response.get("id"));
        assertFalse(personaje.getCondiciones().stream().anyMatch(item -> Long.valueOf(8L).equals(item.getId())));
    }

    @Test
    void deberiaLanzarHechizoYDescontarEnergia() {
        Hechizo hechizo = new Hechizo();
        hechizo.setId(5L);
        hechizo.setNombre("Misil Mágico");
        hechizo.setNivel(2);
        hechizo.setDaño(8);
        hechizo.setDuracion(0);
        hechizo.setTipo("ataque");

        Personaje personaje = new Personaje();
        personaje.setId(1L);
        personaje.setNombre("Lia");
        personaje.setPuntosEnergia(6);
        personaje.setHechizos(new ArrayList<>());
        personaje.getHechizos().add(hechizo);

        when(personajeService.buscarPorId(1L)).thenReturn(Optional.of(personaje));
        when(personajeService.guardar(personaje)).thenReturn(personaje);

        Map<String, Object> response = narradorController.lanzarHechizo(1L, 5L);

        assertEquals("Misil Mágico", response.get("nombre"));
        assertEquals(4, response.get("energiaRestante"));
        assertEquals(8, response.get("daño"));
    }

    @Test
    void deberiaAgregarHechizoAlPersonaje() {
        Personaje personaje = new Personaje();
        personaje.setId(1L);
        personaje.setNombre("Lia");
        personaje.setPuntosEnergia(6);
        personaje.setHechizos(new ArrayList<>());

        when(personajeService.buscarPorId(1L)).thenReturn(Optional.of(personaje));
        when(personajeService.guardar(personaje)).thenAnswer(invocation -> {
            personaje.getHechizos().get(0).setId(42L);
            return personaje;
        });

        Map<String, Object> response = narradorController.agregarHechizo(
                1L,
                "Escudo",
                1,
                "defensa",
                "Aumenta la protección",
                0,
                3,
                true,
                "Bendecido",
                "Otorga protección sagrada",
                2);

        assertEquals(42L, response.get("id"));
        assertEquals("Escudo", response.get("nombre"));
        assertEquals(1, personaje.getHechizos().size());
        assertEquals(1, personaje.getHechizos().get(0).getCondiciones().size());
        assertEquals("Bendecido", personaje.getHechizos().get(0).getCondiciones().get(0).getNombre());
    }

    @Test
    void deberiaAgregarItemAlInventarioDelPersonaje() {
        Personaje personaje = new Personaje();
        personaje.setId(1L);
        personaje.setNombre("Lia");

        when(personajeService.buscarPorId(1L)).thenReturn(Optional.of(personaje));
        when(itemBaseRepository.save(any(ItemBase.class))).thenAnswer(invocation -> {
            ItemBase itemBase = invocation.getArgument(0);
            itemBase.setId(21L);
            return itemBase;
        });
        when(itemInventarioRepository.save(any(ItemInventario.class))).thenAnswer(invocation -> {
            ItemInventario item = invocation.getArgument(0);
            item.setId(77L);
            return item;
        });

        Map<String, Object> response = narradorController.agregarItemInventario(1L, "Poción", "consumible", "Recupera vida", 2, true, false);

        assertEquals(77L, response.get("id"));
        assertEquals("Poción", response.get("nombre"));
        assertEquals(2, response.get("cantidad"));
    }

    @Test
    void deberiaEliminarItemDelInventario() {
        ItemBase itemBase = new ItemBase();
        itemBase.setId(21L);
        itemBase.setNombre("Poción");
        itemBase.setTipo("consumible");

        ItemInventario itemInventario = new ItemInventario();
        itemInventario.setId(77L);
        itemInventario.setItemBase(itemBase);
        itemInventario.setCantidad(2);

        when(itemInventarioRepository.findById(77L)).thenReturn(Optional.of(itemInventario));

        Map<String, Object> response = narradorController.eliminarItemInventario(77L);

        assertEquals(77L, response.get("id"));
        assertEquals("Poción", response.get("nombre"));
        assertEquals(2, response.get("cantidad"));
    }

    @Test
    void deberiaMostrarVistaJugadorConPersonaje() {
        Personaje personaje = new Personaje();
        personaje.setId(1L);
        personaje.setNombre("Lia");

        Batalla batalla = new Batalla();
        batalla.setId(10L);
        batalla.setNombre("Asalto final");
        batalla.setEstado("EN_CURSO");
        batalla.getPersonajes().add(personaje);
        personaje.setTurnoActual(true);

        when(personajeService.buscarPorId(1L)).thenReturn(Optional.of(personaje));
        when(batallaService.buscarActivaPorPersonaje(1L)).thenReturn(Optional.of(batalla));

        ConcurrentModel model = new ConcurrentModel();
        String vista = narradorController.mostrarVistaJugador(1L, model);

        assertEquals("jugador", vista);
        assertEquals(personaje, model.getAttribute("personaje"));
        assertEquals(batalla, model.getAttribute("batallaActiva"));
        assertEquals(true, model.getAttribute("esTurnoJugador"));
    }

    @Test
    void deberiaAtacarDesdeLaVistaDelJugador() {
        Map<String, Object> resultado = Map.of(
                "evento", "Lia atacó a Goblin y causó 5 de daño.",
                "vidaRestante", 7,
                "objetivoId", 2L,
                "objetivoTipo", "villano");

        when(batallaService.atacarDesdeJugador(1L, 2L)).thenReturn(resultado);

        Map<String, Object> response = narradorController.atacarDesdeJugador(1L, 2L);

        assertEquals("Lia atacó a Goblin y causó 5 de daño.", response.get("evento"));
        assertEquals(7, response.get("vidaRestante"));
    }
}
