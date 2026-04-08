package com.dungeons_and_dragons.rol.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Entity
@Data
public class Batalla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String estado = "PREPARADA";
    private int rondaActual = 0;
    private Integer indiceTurnoActual = 0;

    @ManyToMany
    @JoinTable(
        name = "batalla_personajes",
        joinColumns = @JoinColumn(name = "batalla_id"),
        inverseJoinColumns = @JoinColumn(name = "personaje_id")
    )
    private List<Personaje> personajes = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "batalla_id")
    private List<Villano> villanos = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "batalla_logs", joinColumns = @JoinColumn(name = "batalla_id"))
    @Column(name = "evento", length = 500)
    private List<String> logCombate = new ArrayList<>();
}
