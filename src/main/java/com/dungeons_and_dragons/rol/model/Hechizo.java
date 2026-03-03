package com.dungeons_and_dragons.rol.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Hechizo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;        // Nombre del hechizo
    private int nivel;            // Nivel del hechizo (1-9, según D&D)
    private String tipo;          // Ej: ataque, defensa, curación
    private String descripcion;   // Detalle de qué hace el hechizo
    private int daño;             // Daño si aplica, 0 si no hace daño
    private int duracion;         // Duración en turnos o minutos

    private boolean requiereConcentracion; // Si necesita concentración
}