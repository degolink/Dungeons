package com.dungeons_and_dragons.rol.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class DadosServiceTest {

    private final DadosService dadosService = new DadosService();

    @Test
    void deberiaCalcularModificadorCorrectamenteParaValoresImparesBajos() {
        assertEquals(-1, dadosService.calcularModificador(9));
        assertEquals(-2, dadosService.calcularModificador(7));
        assertEquals(0, dadosService.calcularModificador(10));
        assertEquals(3, dadosService.calcularModificador(16));
    }
}
