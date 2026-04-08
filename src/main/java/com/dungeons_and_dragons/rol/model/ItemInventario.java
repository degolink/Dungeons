package com.dungeons_and_dragons.rol.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class ItemInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Cantidad del objeto (para apilables)
    private int cantidad;

    // Si el objeto está equipado (solo para armas, armaduras, accesorios)
    private boolean equipado;

    // Si el objeto ha sido usado (para consumibles)
    private boolean consumido;

    // Referencia al catálogo global de objetos
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_base_id", nullable = false)
    private ItemBase itemBase;

    // Relación con el personaje que lo posee
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personaje_id")
    private Personaje personaje;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "villano_id")
    private Villano villano;

    // Calcula el peso total de este ítem según la cantidad
    public double getPesoTotal() {
        return itemBase.getPeso() * cantidad;
    }

    // Marca el objeto como consumido y reduce cantidad
    public void usar() {
        if (itemBase.isApilable()) {
            if (cantidad > 0) {
                cantidad--;
                consumido = true;
            }
        } else {
            consumido = true;
            cantidad = 0;
        }
    }

    // Método para equipar el objeto
    public void equipar() {
        if (!consumido) {
            equipado = true;
        }
    }

    // Método para desequipar
    public void desequipar() {
        equipado = false;
    }
}