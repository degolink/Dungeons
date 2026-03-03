package com.dungeons_and_dragons.rol.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class Monedero {

    private int cobre;
    private int plata;
    private int electro;
    private int oro;
    private int platino;
}