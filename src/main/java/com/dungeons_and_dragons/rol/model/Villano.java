package com.dungeons_and_dragons.rol.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Entity
@Data
public class Villano {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String tipo;
    private int nivel;

    private int fuerza;
    private int destreza;
    private int constitucion;
    private int inteligencia;
    private int sabiduria;
    private int carisma;

    private int puntosVida;
    private int puntosVidaMax;
    private int puntosEnergia;

    private Integer iniciativaActual = 0;
    private boolean enBatalla = false;
    private boolean turnoActual = false;
    private boolean derrotado = false;
    private Integer ordenTurno = 0;

    @OneToMany(
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @JoinColumn(name = "villano_id")
    private List<Hechizo> hechizos = new ArrayList<>();

    @OneToMany(
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @JoinColumn(name = "villano_id")
    private List<Condicion> condiciones = new ArrayList<>();

    @OneToMany(mappedBy = "villano", fetch = FetchType.LAZY)
    private List<ItemInventario> inventario = new ArrayList<>();
}
