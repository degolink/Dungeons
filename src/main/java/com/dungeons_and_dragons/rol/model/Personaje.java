package com.dungeons_and_dragons.rol.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Personaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Datos básicos ---
    private String nombre;
    private String apodo;
    private String clase;
    private String raza;
    private String alineamiento;
    private String imagen;

    private int nivel;
    private int experiencia;

    // --- Atributos principales ---
    private int fuerza;
    private int destreza;
    private int constitucion;
    private int inteligencia;
    private int sabiduria;
    private int carisma;
    private Integer fuerzaBase;
    private Integer destrezaBase;
    private Integer constitucionBase;
    private Integer inteligenciaBase;
    private Integer sabiduriaBase;
    private Integer carismaBase;

    // --- Recursos ---
    private int puntosVida;
    private int puntosVidaMax;
    private int puntosEnergia;
    private Integer puntosVidaMaxBase;
    private Integer puntosEnergiaBase;

    // --- Combate (calculado por items equipados) ---
    private int ataque;
    private int defensa;
    private int iniciativaBonus;

    @Column(nullable = false)
    private Integer cobre = 0;

    @Column(nullable = false)
    private Integer plata = 0;

    @Column(nullable = false)
    private Integer oro = 0;

    @Column(nullable = false)
    private Integer platino = 0;

    // --- Historia ---
    @Column(length = 1000)
    private String historia;

    @Column(length = 500)
    private String motivaciones;

    private boolean activo = true;

    // --- Estado de batalla ---
    private Integer iniciativaActual = 0;
    private boolean enBatalla = false;
    private boolean turnoActual = false;
    private boolean derrotado = false;
    private Integer ordenTurno = 0;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;

    // =========================
    // RELACIONES
    // =========================

    @OneToMany(
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @JoinColumn(name = "personaje_id") 
    private List<Hechizo> hechizos = new ArrayList<>();


    @OneToMany(
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @JoinColumn(name = "personaje_id")
    private List<Condicion> condiciones = new ArrayList<>();


    @OneToMany(mappedBy = "personaje", fetch = FetchType.LAZY)
    private List<ItemInventario> inventario = new ArrayList<>();

    @OneToMany(mappedBy = "personaje", fetch = FetchType.LAZY)
    private List<ItemEquipamento> equipamiento = new ArrayList<>();
}
