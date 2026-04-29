package com.dungeons_and_dragons.rol.dnd.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO que mapea la respuesta de GET /api/monsters/{index} de la D&D 5e API.
 * Campos relevantes para nuestro modelo Villano:
 *   name, type, challenge_rating (→ nivel), hit_points,
 *   strength, dexterity, constitution, intelligence, wisdom, charisma
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DndMonsterDTO {
    private String index;
    private String name;

    /** Categoría: beast, undead, humanoid, dragon, etc. */
    private String type;

    private String size;
    private String alignment;

    @JsonProperty("hit_points")
    private int hitPoints;

    @JsonProperty("hit_dice")
    private String hitDice;

    /** Valor de desafío (CR). 0.25 → nivel 1, 20 → nivel 20. */
    @JsonProperty("challenge_rating")
    private double challengeRating;

    private int strength;
    private int dexterity;
    private int constitution;
    private int intelligence;
    private int wisdom;
    private int charisma;

    /** Puntos de experiencia al derrotarlo. */
    private int xp;
}
