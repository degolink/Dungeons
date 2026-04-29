package com.dungeons_and_dragons.rol.dnd;

import com.dungeons_and_dragons.rol.dnd.dto.DndMonsterDTO;
import com.dungeons_and_dragons.rol.dnd.dto.DndMonsterListDTO;
import com.dungeons_and_dragons.rol.dnd.dto.DndSpellDTO;
import com.dungeons_and_dragons.rol.dnd.dto.DndSpellListDTO;
import com.dungeons_and_dragons.rol.model.Hechizo;
import com.dungeons_and_dragons.rol.model.Villano;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST que expone la integración con la D&D 5e API.
 *
 * Endpoints de consulta (GET) → reenvían la respuesta de la API sin persistir nada.
 * Endpoints de importación (POST) → guardan el recurso en la base de datos local.
 *
 * Base path: /api/dnd
 * Requiere rol NARRADOR (configurado en SecurityConfig).
 */
@RestController
@RequestMapping("/api/dnd")
public class DndApiController {

    private final DndApiService dndApiService;

    public DndApiController(DndApiService dndApiService) {
        this.dndApiService = dndApiService;
    }

    // ─── HECHIZOS ────────────────────────────────────────────────────────────

    /** Lista todos los hechizos disponibles en la D&D 5e API (índice + nombre). */
    @GetMapping("/hechizos")
    public ResponseEntity<DndSpellListDTO> listarHechizos() {
        return ResponseEntity.ok(dndApiService.listarHechizos());
    }

    /** Devuelve el detalle completo de un hechizo de la API sin guardarlo localmente. */
    @GetMapping("/hechizos/{index}")
    public ResponseEntity<DndSpellDTO> obtenerHechizo(@PathVariable String index) {
        return ResponseEntity.ok(dndApiService.obtenerHechizo(index));
    }

    /**
     * Importa el hechizo indicado desde la API y lo guarda en la BD local.
     * <p>
     * Parámetros opcionales:
     * <ul>
     *   <li>{@code personajeId} — asigna el hechizo directamente al personaje indicado</li>
     *   <li>{@code villanoId}   — asigna el hechizo directamente al villano indicado</li>
     * </ul>
     * Si no se proporciona ninguno, se guarda como hechizo independiente.
     */
    @PostMapping("/importar/hechizo/{index}")
    public ResponseEntity<Hechizo> importarHechizo(
            @PathVariable String index,
            @RequestParam(required = false) Long personajeId,
            @RequestParam(required = false) Long villanoId) {
        return ResponseEntity.ok(dndApiService.importarHechizo(index, personajeId, villanoId));
    }

    // ─── MONSTRUOS ───────────────────────────────────────────────────────────

    /** Lista todos los monstruos disponibles en la D&D 5e API (índice + nombre). */
    @GetMapping("/monstruos")
    public ResponseEntity<DndMonsterListDTO> listarMonstruos() {
        return ResponseEntity.ok(dndApiService.listarMonstruos());
    }

    /** Devuelve el detalle completo de un monstruo de la API sin guardarlo localmente. */
    @GetMapping("/monstruos/{index}")
    public ResponseEntity<DndMonsterDTO> obtenerMonstruo(@PathVariable String index) {
        return ResponseEntity.ok(dndApiService.obtenerMonstruo(index));
    }

    /**
     * Importa el monstruo indicado desde la API y lo guarda como {@link Villano} en la BD.
     * El challenge_rating se convierte a nivel (mínimo 1).
     */
    @PostMapping("/importar/monstruo/{index}")
    public ResponseEntity<Villano> importarMonstruo(@PathVariable String index) {
        return ResponseEntity.ok(dndApiService.importarMonstruo(index));
    }
}
