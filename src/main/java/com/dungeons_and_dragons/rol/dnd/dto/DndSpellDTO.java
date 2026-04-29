package com.dungeons_and_dragons.rol.dnd.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * DTO que mapea la respuesta de GET /api/spells/{index} de la D&D 5e API.
 * Campos relevantes para nuestro modelo Hechizo:
 *   name, level, school (tipo), desc (descripcion), concentration, duration
 *   damage.damage_at_slot_level → para calcular el daño aproximado
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DndSpellDTO {
    private String index;
    private String name;

    /** Descripción del hechizo como lista de párrafos. */
    private List<String> desc;

    /** Nivel del hechizo (0 = cantrip, 1-9 = slot). */
    private int level;

    /** Escuela de magia: Evocation, Abjuration, etc. */
    private DndResultItemDTO school;

    @JsonProperty("casting_time")
    private String castingTime;

    private String range;
    private String duration;
    private boolean concentration;
    private boolean ritual;

    private List<String> components;

    /**
     * Objeto con "damage_type" y "damage_at_slot_level" (Map<nivel, "NdM">).
     * Ej: { "damage_at_slot_level": { "3": "8d6", "4": "9d6" } }
     */
    @JsonProperty("damage")
    private Map<String, Object> damage;
}
