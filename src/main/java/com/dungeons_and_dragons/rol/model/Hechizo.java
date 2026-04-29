package com.dungeons_and_dragons.rol.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "hechizo_origen_id")
    @Builder.Default
    private List<Condicion> condiciones = new java.util.ArrayList<>();
}