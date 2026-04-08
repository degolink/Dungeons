package com.dungeons_and_dragons.rol.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.dungeons_and_dragons.rol.model.Personaje;

@Service
public class DadosService {

    private final Random random = new Random();

    // Lanza 1 dado de X caras
    public int lanzarDado(int caras) {
        if (caras <= 0) {
            throw new IllegalArgumentException("El dado debe tener al menos 1 cara");
        }
        return random.nextInt(caras) + 1;
    }

    // Lanza varios dados del mismo tipo, por ejemplo 2d6
    public List<Integer> lanzarDados(int cantidad, int caras) {
        if (cantidad <= 0 || caras <= 0) {
            throw new IllegalArgumentException("Cantidad y caras deben ser mayores que 0");
        }

        List<Integer> resultados = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            resultados.add(lanzarDado(caras));
        }
        return resultados;
    }

    // Suma total de varios dados
    public int lanzarDadosYSumar(int cantidad, int caras) {
        return lanzarDados(cantidad, caras)
                .stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    // Calcula el modificador de una característica de D&D
    // Ejemplo: 10-11 = 0, 12-13 = +1, 14-15 = +2, 8-9 = -1
    public int calcularModificador(int atributo) {
        return Math.floorDiv(atributo - 10, 2);
    }

    // Calcula iniciativa: 1d20 + modificador de destreza
    public int calcularIniciativa(Personaje personaje) {
        if (personaje == null) {
            throw new IllegalArgumentException("El personaje no puede ser null");
        }

        int destreza = personaje.getDestreza();
        int modificador = calcularModificador(destreza);
        int tirada = lanzarDado(20);

        return tirada + modificador;
    }

    // Devuelve el detalle completo de la iniciativa
    public ResultadoIniciativa tirarIniciativa(Personaje personaje) {
        if (personaje == null) {
            throw new IllegalArgumentException("El personaje no puede ser null");
        }

        int tirada = lanzarDado(20);
        int modificador = calcularModificador(personaje.getDestreza());
        int total = tirada + modificador;

        return new ResultadoIniciativa(
                personaje.getNombre(),
                tirada,
                modificador,
                total
        );
    }

    public static class ResultadoIniciativa {
        private String nombrePersonaje;
        private int tirada;
        private int modificador;
        private int total;

        public ResultadoIniciativa(String nombrePersonaje, int tirada, int modificador, int total) {
            this.nombrePersonaje = nombrePersonaje;
            this.tirada = tirada;
            this.modificador = modificador;
            this.total = total;
        }

        public String getNombrePersonaje() {
            return nombrePersonaje;
        }

        public int getTirada() {
            return tirada;
        }

        public int getModificador() {
            return modificador;
        }

        public int getTotal() {
            return total;
        }
    }
}