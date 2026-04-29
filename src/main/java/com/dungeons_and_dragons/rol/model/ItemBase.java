package com.dungeons_and_dragons.rol.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class ItemBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre del objeto, ejemplo: Espada Larga, Poción de Curación
    private String nombre;

    // Tipo de objeto: arma, armadura, consumible, accesorio, etc.
    private String tipo;

    // Descripción detallada del objeto
    @Column(length = 1000)
    private String descripcion;

    // Peso del objeto (para calcular carga del personaje)
    private double peso;

    // Valor en oro base (puede usarse para comercio)
    private int valorOro;

    // Rareza: comun, poco común, raro, muy raro, legendario
    private String rareza;

    // Apilable: true si se pueden tener varias unidades en una sola entrada de inventario
    private boolean apilable;

    // Si el objeto es mágico o especial
    private boolean magico;
    private boolean equipable;
    private String slotEquipamento;
    private int bonusFuerza;
    private int bonusDestreza;
    private int bonusConstitucion;
    private int bonusInteligencia;
    private int bonusSabiduria;
    private int bonusCarisma;
    private int bonusVida;
    private int bonusEnergia;
    private int bonusAtaque;
    private int bonusDefensa;
    private int bonusIniciativa;

}
