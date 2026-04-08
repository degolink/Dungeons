package com.dungeons_and_dragons.rol;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.dungeons_and_dragons.rol.model.Personaje;
import com.dungeons_and_dragons.rol.repository.BatallaRepository;
import com.dungeons_and_dragons.rol.repository.ItemInventarioRepository;
import com.dungeons_and_dragons.rol.repository.PersonajeRepository;
import com.dungeons_and_dragons.rol.repository.VillanoRepository;

@SpringBootApplication
public class RolApplication {

	public static void main(String[] args) {
		SpringApplication.run(RolApplication.class, args);
	}

	@Bean
	CommandLineRunner cargarPersonajesIniciales(
			PersonajeRepository personajeRepository,
			BatallaRepository batallaRepository,
			ItemInventarioRepository itemInventarioRepository,
			VillanoRepository villanoRepository) {
		return args -> {
			itemInventarioRepository.deleteAll();
			villanoRepository.deleteAll();
			batallaRepository.deleteAll();
			personajeRepository.deleteAll();

			personajeRepository.saveAll(List.of(
					crearThorgar(),
					crearSylvara(),
					crearEldrin(),
					crearLyra()));
		};
	}

	private Personaje crearThorgar() {
		Personaje thorgar = new Personaje();
		thorgar.setNombre("Thorgar");
		thorgar.setApodo("El Muro de Hierro");
		thorgar.setImagen("/img/Thorgar.png");
		thorgar.setClase("Guerrero");
		thorgar.setRaza("Enano");
		thorgar.setAlineamiento("Legal Bueno");
		thorgar.setNivel(18);
		thorgar.setExperiencia(355000);
		thorgar.setFuerza(20);
		thorgar.setDestreza(12);
		thorgar.setConstitucion(20);
		thorgar.setInteligencia(10);
		thorgar.setSabiduria(14);
		thorgar.setCarisma(10);
		thorgar.setPuntosVidaMax(230);
		thorgar.setPuntosVida(230);
		thorgar.setPuntosEnergia(0);
		thorgar.setOro(5000);
		thorgar.setHistoria("Veterano de mil batallas, defensor de fortalezas enanas.");
		thorgar.setMotivaciones("Proteger a su grupo a toda costa.");
		thorgar.setFechaCreacion(LocalDateTime.now());
		thorgar.setFechaModificacion(LocalDateTime.now());
		return thorgar;
	}

	private Personaje crearSylvara() {
		Personaje sylvara = new Personaje();
		sylvara.setNombre("Sylvara");
		sylvara.setApodo("Sombra Carmesí");
		sylvara.setImagen("/img/Sylvara.png");
		sylvara.setClase("Pícaro");
		sylvara.setRaza("Elfo");
		sylvara.setAlineamiento("Caótico Neutral");
		sylvara.setNivel(18);
		sylvara.setExperiencia(355000);
		sylvara.setFuerza(10);
		sylvara.setDestreza(20);
		sylvara.setConstitucion(14);
		sylvara.setInteligencia(14);
		sylvara.setSabiduria(12);
		sylvara.setCarisma(16);
		sylvara.setPuntosVidaMax(150);
		sylvara.setPuntosVida(150);
		sylvara.setPuntosEnergia(0);
		sylvara.setOro(4000);
		sylvara.setHistoria("Asesina de élite entrenada en las sombras.");
		sylvara.setMotivaciones("Acumular poder y secretos.");
		sylvara.setFechaCreacion(LocalDateTime.now());
		sylvara.setFechaModificacion(LocalDateTime.now());
		return sylvara;
	}

	private Personaje crearEldrin() {
		Personaje eldrin = new Personaje();
		eldrin.setNombre("Eldrin");
		eldrin.setApodo("El Tejedor Arcano");
		eldrin.setImagen("/img/Eldrin.png");
		eldrin.setClase("Mago");
		eldrin.setRaza("Humano");
		eldrin.setAlineamiento("Neutral Bueno");
		eldrin.setNivel(18);
		eldrin.setExperiencia(355000);
		eldrin.setFuerza(8);
		eldrin.setDestreza(14);
		eldrin.setConstitucion(14);
		eldrin.setInteligencia(20);
		eldrin.setSabiduria(16);
		eldrin.setCarisma(12);
		eldrin.setPuntosVidaMax(130);
		eldrin.setPuntosVida(130);
		eldrin.setPuntosEnergia(150);
		eldrin.setOro(8000);
		eldrin.setHistoria("Archimago que ha dominado múltiples escuelas de magia.");
		eldrin.setMotivaciones("Descubrir conocimiento prohibido.");
		eldrin.setFechaCreacion(LocalDateTime.now());
		eldrin.setFechaModificacion(LocalDateTime.now());
		return eldrin;
	}

	private Personaje crearLyra() {
		Personaje lyra = new Personaje();
		lyra.setNombre("Lyra");
		lyra.setApodo("Luz del Alba");
		lyra.setImagen("/img/Lyra.png");
		lyra.setClase("Clérigo");
		lyra.setRaza("Aasimar");
		lyra.setAlineamiento("Legal Bueno");
		lyra.setNivel(18);
		lyra.setExperiencia(355000);
		lyra.setFuerza(12);
		lyra.setDestreza(10);
		lyra.setConstitucion(16);
		lyra.setInteligencia(12);
		lyra.setSabiduria(20);
		lyra.setCarisma(18);
		lyra.setPuntosVidaMax(180);
		lyra.setPuntosVida(180);
		lyra.setPuntosEnergia(140);
		lyra.setOro(6000);
		lyra.setHistoria("Elegida por los dioses para mantener el equilibrio.");
		lyra.setMotivaciones("Curar y proteger al mundo del caos.");
		lyra.setFechaCreacion(LocalDateTime.now());
		lyra.setFechaModificacion(LocalDateTime.now());
		return lyra;
	}
}
