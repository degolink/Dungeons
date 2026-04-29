package com.dungeons_and_dragons.rol.dnd;

import com.dungeons_and_dragons.rol.dnd.dto.DndMonsterDTO;
import com.dungeons_and_dragons.rol.dnd.dto.DndMonsterListDTO;
import com.dungeons_and_dragons.rol.dnd.dto.DndSpellDTO;
import com.dungeons_and_dragons.rol.dnd.dto.DndSpellListDTO;
import com.dungeons_and_dragons.rol.model.Hechizo;
import com.dungeons_and_dragons.rol.model.Personaje;
import com.dungeons_and_dragons.rol.model.Villano;
import com.dungeons_and_dragons.rol.repository.HechizoRepository;
import com.dungeons_and_dragons.rol.repository.PersonajeRepository;
import com.dungeons_and_dragons.rol.repository.VillanoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Map;

/**
 * Servicio que consume la D&D 5e API pública (https://www.dnd5eapi.co/api).
 *
 * Dos operaciones principales por recurso:
 *   - listar*()    → devuelve la lista de índices desde la API (sin guardar nada)
 *   - obtener*()   → devuelve el detalle completo desde la API (sin guardar nada)
 *   - importar*()  → obtiene el detalle y lo persiste en nuestra BD local
 */
@Service
public class DndApiService {

    private static final String BASE_URL = "https://www.dnd5eapi.co/api";

    private final RestClient restClient;
    private final HechizoRepository hechizoRepository;
    private final VillanoRepository villanoRepository;
    private final PersonajeRepository personajeRepository;

    public DndApiService(HechizoRepository hechizoRepository, VillanoRepository villanoRepository,
                         PersonajeRepository personajeRepository) {
        this.restClient = RestClient.builder().baseUrl(BASE_URL).build();
        this.hechizoRepository = hechizoRepository;
        this.villanoRepository = villanoRepository;
        this.personajeRepository = personajeRepository;
    }

    // ─── HECHIZOS ────────────────────────────────────────────────────────────

    public DndSpellListDTO listarHechizos() {
        return restClient.get()
                .uri("/spells")
                .retrieve()
                .body(DndSpellListDTO.class);
    }

    public DndSpellDTO obtenerHechizo(String index) {
        return restClient.get()
                .uri("/spells/{index}", index)
                .retrieve()
                .body(DndSpellDTO.class);
    }

    /**
     * Descarga el hechizo de la API, lo convierte a nuestra entidad {@link Hechizo}
     * y lo guarda en la base de datos local.
     * <p>
     * Si se indica {@code personajeId}, el hechizo se añade directamente al personaje.
     * Si se indica {@code villanoId}, se añade al villano.
     * Si no se indica ninguno, se guarda como hechizo independiente (reutilizable).
     */
    public Hechizo importarHechizo(String index, Long personajeId, Long villanoId) {
        DndSpellDTO dto = obtenerHechizo(index);

        Hechizo hechizo = new Hechizo();
        hechizo.setNombre(dto.getName());
        hechizo.setNivel(dto.getLevel() == 0 ? 1 : dto.getLevel());
        hechizo.setTipo(dto.getSchool() != null ? dto.getSchool().getName() : "desconocido");
        hechizo.setDescripcion(dto.getDesc() != null ? String.join(" ", dto.getDesc()) : "");
        hechizo.setDaño(extraerDanio(dto));
        hechizo.setDuracion(parseDuracion(dto.getDuration()));
        hechizo.setRequiereConcentracion(dto.isConcentration());
        hechizo.setCondiciones(new ArrayList<>());

        if (personajeId != null) {
            Personaje personaje = personajeRepository.findById(personajeId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Personaje no encontrado: " + personajeId));
            personaje.getHechizos().add(hechizo);
            Personaje guardado = personajeRepository.save(personaje);
            return guardado.getHechizos().get(guardado.getHechizos().size() - 1);
        }

        if (villanoId != null) {
            Villano villano = villanoRepository.findById(villanoId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Villano no encontrado: " + villanoId));
            villano.getHechizos().add(hechizo);
            Villano guardado = villanoRepository.save(villano);
            return guardado.getHechizos().get(guardado.getHechizos().size() - 1);
        }

        return hechizoRepository.save(hechizo);
    }

    // ─── MONSTRUOS ───────────────────────────────────────────────────────────

    public DndMonsterListDTO listarMonstruos() {
        return restClient.get()
                .uri("/monsters")
                .retrieve()
                .body(DndMonsterListDTO.class);
    }

    public DndMonsterDTO obtenerMonstruo(String index) {
        return restClient.get()
                .uri("/monsters/{index}", index)
                .retrieve()
                .body(DndMonsterDTO.class);
    }

    /**
     * Descarga el monstruo de la API, lo convierte a nuestro {@link Villano}
     * y lo guarda en la base de datos local.
     * El challenge_rating (CR) del D&D se mapea al nivel del villano (mínimo 1).
     */
    public Villano importarMonstruo(String index) {
        DndMonsterDTO dto = obtenerMonstruo(index);

        Villano villano = new Villano();
        villano.setNombre(dto.getName());
        villano.setTipo(dto.getType());
        villano.setNivel(Math.max(1, (int) Math.ceil(dto.getChallengeRating())));

        villano.setFuerza(dto.getStrength());
        villano.setDestreza(dto.getDexterity());
        villano.setConstitucion(dto.getConstitution());
        villano.setInteligencia(dto.getIntelligence());
        villano.setSabiduria(dto.getWisdom());
        villano.setCarisma(dto.getCharisma());

        // Guardamos los valores base para poder restaurarlos tras efectos temporales
        villano.setFuerzaBase(dto.getStrength());
        villano.setDestrezaBase(dto.getDexterity());
        villano.setConstitucionBase(dto.getConstitution());
        villano.setInteligenciaBase(dto.getIntelligence());
        villano.setSabiduriaBase(dto.getWisdom());
        villano.setCarismaBase(dto.getCharisma());

        villano.setPuntosVida(dto.getHitPoints());
        villano.setPuntosVidaMax(dto.getHitPoints());
        villano.setPuntosVidaMaxBase(dto.getHitPoints());

        int energia = Math.max(1, dto.getHitPoints() / 2);
        villano.setPuntosEnergia(energia);
        villano.setPuntosEnergiaBase(energia);

        return villanoRepository.save(villano);
    }

    // ─── Utilidades de parseo ─────────────────────────────────────────────────

    /**
     * Extrae el daño aproximado del campo "damage.damage_at_slot_level".
     * Ej: "8d6" → numDados * (tipoDado / 2) = 8 * 3 = 24
     */
    @SuppressWarnings("unchecked")
    private int extraerDanio(DndSpellDTO dto) {
        if (dto.getDamage() == null) return 0;
        Object slotLevel = dto.getDamage().get("damage_at_slot_level");
        if (slotLevel instanceof Map<?, ?> map && !map.isEmpty()) {
            String expr = map.values().iterator().next().toString();
            return parsearExpresionDado(expr);
        }
        return 0;
    }

    /**
     * Parsea expresiones del tipo "NdM" o "NdM+X" a un valor entero aproximado.
     * Usa el valor medio del dado: NdM → N * (M/2).
     */
    private int parsearExpresionDado(String expr) {
        try {
            String[] partes = expr.split("[dD]");
            int numDados = Integer.parseInt(partes[0].trim());
            int tipoDado = Integer.parseInt(partes[1].split("\\+")[0].trim());
            return numDados * (tipoDado / 2);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Convierte la duración textual de la API ("1 minute", "Instantaneous", etc.)
     * a un número entero de turnos.
     */
    private int parseDuracion(String duration) {
        if (duration == null || duration.isBlank() || duration.contains("Instantaneous")) return 0;
        try {
            return Integer.parseInt(duration.split(" ")[0]);
        } catch (Exception e) {
            return 1;
        }
    }
}
