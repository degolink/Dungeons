package com.dungeons_and_dragons.rol.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class ItemEquipamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_inventario_id", unique = true, nullable = false)
    private ItemInventario itemInventario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_base_id", nullable = false)
    private ItemBase itemBase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personaje_id")
    private Personaje personaje;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "villano_id")
    private Villano villano;

    private String slot;
    private boolean equipado;
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
