package com.dungeons_and_dragons.rol.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Condicion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // <-- Esto es obligatorio

    private String nombre;
    private String descripcion;
    private int duracion; // en turnos o segundos según tu lógica

}