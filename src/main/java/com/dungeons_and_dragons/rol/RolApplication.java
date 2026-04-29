package com.dungeons_and_dragons.rol;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import com.dungeons_and_dragons.rol.model.Hechizo;
import com.dungeons_and_dragons.rol.model.ItemBase;
import com.dungeons_and_dragons.rol.model.ItemInventario;
import com.dungeons_and_dragons.rol.model.Personaje;
import com.dungeons_and_dragons.rol.repository.BatallaRepository;
import com.dungeons_and_dragons.rol.repository.ItemBaseRepository;
import com.dungeons_and_dragons.rol.repository.ItemEquipamentoRepository;
import com.dungeons_and_dragons.rol.repository.ItemInventarioRepository;
import com.dungeons_and_dragons.rol.repository.PersonajeRepository;
import com.dungeons_and_dragons.rol.repository.VillanoRepository;
import com.dungeons_and_dragons.rol.service.ItemEquipamentoService;

@SpringBootApplication
@ComponentScan(basePackages = {"com.dungeons_and_dragons"})
public class RolApplication {

	public static void main(String[] args) {
		SpringApplication.run(RolApplication.class, args);
	}

	@Bean
	CommandLineRunner cargarPersonajesIniciales(
			PersonajeRepository personajeRepository,
			BatallaRepository batallaRepository,
			ItemInventarioRepository itemInventarioRepository,
			ItemEquipamentoRepository itemEquipamentoRepository,
			VillanoRepository villanoRepository,
			ItemBaseRepository itemBaseRepository,
			ItemEquipamentoService itemEquipamentoService) {
		return args -> {
			itemEquipamentoRepository.deleteAll();
			itemInventarioRepository.deleteAll();
			villanoRepository.deleteAll();
			batallaRepository.deleteAll();
			personajeRepository.deleteAll();
			itemBaseRepository.deleteAll();

			List<Personaje> personajesGuardados = personajeRepository.saveAll(List.of(
					crearThorgar(),
					crearSylvara(),
					crearEldrin(),
					crearLyra()));

			Map<String, Personaje> personajesPorNombre = new LinkedHashMap<>();
			for (Personaje personaje : personajesGuardados) {
				personajesPorNombre.put(personaje.getNombre(), personaje);
			}

			cargarItemsEpicos(itemBaseRepository, itemInventarioRepository, personajesPorNombre);
			itemEquipamentoService.sincronizarEquipamentosExistentes();
		};
	}

	private Personaje crearThorgar() {
		Personaje thorgar = new Personaje();
		thorgar.setNombre("Thorgar");
		thorgar.setApodo("El Muro de Hierro");
		thorgar.setImagen("/img/Thorgar2.gif");
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
		thorgar.setPuntosEnergia(72);
		thorgar.getHechizos().addAll(crearHechizosIniciales("guerrero", thorgar.getNivel()));
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
		sylvara.setPuntosEnergia(90);
		sylvara.getHechizos().addAll(crearHechizosIniciales("picaro", sylvara.getNivel()));
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
		eldrin.getHechizos().addAll(crearHechizosIniciales("mago", eldrin.getNivel()));
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
		lyra.getHechizos().addAll(crearHechizosIniciales("clerigo", lyra.getNivel()));
		lyra.setOro(6000);
		lyra.setHistoria("Elegida por los dioses para mantener el equilibrio.");
		lyra.setMotivaciones("Curar y proteger al mundo del caos.");
		lyra.setFechaCreacion(LocalDateTime.now());
		lyra.setFechaModificacion(LocalDateTime.now());
		return lyra;
	}

	private void cargarItemsEpicos(ItemBaseRepository itemBaseRepository,
			ItemInventarioRepository itemInventarioRepository,
			Map<String, Personaje> personajesPorNombre) {
		List<ItemBase> itemsEpicos = itemBaseRepository.saveAll(List.of(
				crearItemBase("Espada Vorpal", "arma", "Espada legendaria capaz de decapitar enemigos con golpes críticos.", 3.5, 5000, "legendario", false, true),
				crearItemBase("Espada Matadragones", "arma", "Inflige daño adicional contra dragones y criaturas voladoras.", 4.0, 4500, "legendario", false, true),
				crearItemBase("Martillo del Trueno", "arma", "Permite invocar rayos y aturdir enemigos al impactar.", 6.0, 5200, "legendario", false, true),
				crearItemBase("Hoja Vampírica", "arma", "Absorbe vida del enemigo al infligir daño.", 2.8, 4800, "muy raro", false, true),
				crearItemBase("Daga de la Sombra", "arma", "Permite teletransportarse entre sombras y mejora ataques sigilosos.", 1.2, 4200, "muy raro", false, true),
				crearItemBase("Armadura del Titán", "armadura", "Reduce significativamente el daño físico recibido.", 18.0, 6000, "legendario", false, true),
				crearItemBase("Armadura Élfica Viviente", "armadura", "Se adapta al portador y mejora su agilidad.", 7.0, 5500, "muy raro", false, true),
				crearItemBase("Armadura Infernal", "armadura", "Otorga resistencia al fuego pero consume lentamente la vida.", 16.0, 5800, "legendario", false, true),
				crearItemBase("Anillo de los Tres Deseos", "accesorio", "Permite alterar la realidad un número limitado de veces.", 0.1, 10000, "legendario", false, true),
				crearItemBase("Amuleto de Protección Absoluta", "accesorio", "Otorga resistencia contra magia y daño.", 0.2, 7000, "muy raro", false, true),
				crearItemBase("Capa de Invisibilidad", "accesorio", "Permite volverse invisible por periodos cortos.", 1.5, 9000, "legendario", false, true),
				crearItemBase("Poción de Inmortalidad", "consumible", "Vuelve invulnerable temporalmente.", 0.5, 2000, "muy raro", true, true),
				crearItemBase("Elixir del Poder", "consumible", "Aumenta todos los atributos temporalmente.", 0.5, 1800, "raro", true, true),
				crearItemBase("Orbe del Caos", "artefacto", "Genera efectos mágicos impredecibles extremadamente poderosos.", 2.0, 12000, "legendario", false, true),
				crearItemBase("Grimorio Prohibido", "artefacto", "Contiene hechizos antiguos de gran poder con riesgo para el usuario.", 3.0, 11000, "legendario", false, true)));

		Map<String, ItemBase> itemsPorNombre = new LinkedHashMap<>();
		for (ItemBase item : itemsEpicos) {
			itemsPorNombre.put(item.getNombre(), item);
		}

		itemInventarioRepository.saveAll(List.of(
				crearItemInventario(personajesPorNombre.get("Thorgar"), itemsPorNombre.get("Espada Vorpal"), 1, true),
				crearItemInventario(personajesPorNombre.get("Thorgar"), itemsPorNombre.get("Espada Matadragones"), 1, false),
				crearItemInventario(personajesPorNombre.get("Thorgar"), itemsPorNombre.get("Martillo del Trueno"), 1, false),
				crearItemInventario(personajesPorNombre.get("Thorgar"), itemsPorNombre.get("Armadura del Titán"), 1, true),
				crearItemInventario(personajesPorNombre.get("Thorgar"), itemsPorNombre.get("Armadura Infernal"), 1, false),
				crearItemInventario(personajesPorNombre.get("Sylvara"), itemsPorNombre.get("Hoja Vampírica"), 1, false),
				crearItemInventario(personajesPorNombre.get("Sylvara"), itemsPorNombre.get("Daga de la Sombra"), 1, true),
				crearItemInventario(personajesPorNombre.get("Sylvara"), itemsPorNombre.get("Armadura Élfica Viviente"), 1, true),
				crearItemInventario(personajesPorNombre.get("Sylvara"), itemsPorNombre.get("Capa de Invisibilidad"), 1, true),
				crearItemInventario(personajesPorNombre.get("Eldrin"), itemsPorNombre.get("Anillo de los Tres Deseos"), 1, true),
				crearItemInventario(personajesPorNombre.get("Eldrin"), itemsPorNombre.get("Orbe del Caos"), 1, false),
				crearItemInventario(personajesPorNombre.get("Eldrin"), itemsPorNombre.get("Grimorio Prohibido"), 1, false),
				crearItemInventario(personajesPorNombre.get("Lyra"), itemsPorNombre.get("Amuleto de Protección Absoluta"), 1, true),
				crearItemInventario(personajesPorNombre.get("Lyra"), itemsPorNombre.get("Poción de Inmortalidad"), 2, false),
				crearItemInventario(personajesPorNombre.get("Lyra"), itemsPorNombre.get("Elixir del Poder"), 3, false)));
	}

	private ItemBase crearItemBase(String nombre, String tipo, String descripcion, double peso, int valorOro,
			String rareza, boolean apilable, boolean magico) {
		ItemBase itemBase = new ItemBase();
		itemBase.setNombre(nombre);
		itemBase.setTipo(tipo);
		itemBase.setDescripcion(descripcion);
		itemBase.setPeso(peso);
		itemBase.setValorOro(valorOro);
		itemBase.setRareza(rareza);
		itemBase.setApilable(apilable);
		itemBase.setMagico(magico);
		itemBase.setEquipable(esTipoEquipable(tipo));
		itemBase.setSlotEquipamento(deducirSlot(tipo));
		return itemBase;
	}

	private ItemInventario crearItemInventario(Personaje personaje, ItemBase itemBase, int cantidad, boolean equipado) {
		ItemInventario itemInventario = new ItemInventario();
		itemInventario.setPersonaje(personaje);
		itemInventario.setItemBase(itemBase);
		itemInventario.setCantidad(cantidad);
		itemInventario.setEquipado(equipado);
		itemInventario.setConsumido(false);
		return itemInventario;
	}

	private boolean esTipoEquipable(String tipo) {
		String valor = tipo == null ? "" : tipo.trim().toLowerCase();
		return switch (valor) {
			case "arma", "armadura", "escudo", "casco", "botas", "anillo", "amuleto", "accesorio" -> true;
			default -> false;
		};
	}

	private String deducirSlot(String tipo) {
		String valor = tipo == null ? "" : tipo.trim().toLowerCase();
		return switch (valor) {
			case "arma" -> "arma";
			case "armadura" -> "armadura";
			case "escudo" -> "escudo";
			case "casco" -> "casco";
			case "botas" -> "botas";
			case "anillo" -> "anillo";
			case "amuleto", "accesorio" -> "accesorio";
			default -> valor.isBlank() ? "miscelaneo" : valor;
		};
	}

	private List<Hechizo> crearHechizosIniciales(String clase, int nivel) {
		return switch (clase.toLowerCase()) {
			case "guerrero" -> List.of(
					crearHechizo("Golpe Heroico", Math.max(1, nivel / 3), "ataque", "Un ataque demoledor potenciado por la voluntad del guerrero.", 6 + nivel / 2, 0, false),
					crearHechizo("Grito de Guerra", 1, "defensa", "Refuerza el ánimo y la presencia en combate.", 3 + nivel / 3, 2, false));
			case "picaro", "pícaro" -> List.of(
					crearHechizo("Daga Sombría", Math.max(1, nivel / 3), "ataque", "Una estocada envuelta en sombras y sigilo.", 5 + nivel / 2, 0, false),
					crearHechizo("Nube de Humo", 1, "utilidad", "Confunde al enemigo y crea una distracción perfecta.", 4 + nivel / 3, 1, false));
			case "clerigo", "clérigo" -> List.of(
					crearHechizo("Luz Sanadora", Math.max(1, nivel / 4), "curacion", "Un resplandor sagrado que restaura vitalidad.", 6 + nivel / 2, 0, false),
					crearHechizo("Llama Sagrada", Math.max(1, nivel / 3), "ataque", "Fuego divino que castiga a los impuros.", 5 + nivel / 2, 0, false));
			case "mago" -> List.of(
					crearHechizo("Bola de Fuego", Math.max(1, nivel / 3), "ataque", "Una explosión ígnea de gran poder destructivo.", 7 + nivel, 0, false),
					crearHechizo("Escudo Arcano", 1, "defensa", "Una barrera mística protege al lanzador.", 4 + nivel / 3, 3, true));
			default -> List.of(crearHechizo("Descarga Arcana", 1, "ataque", "Una chispa mágica útil para cualquier aventurero.", 4 + nivel / 2, 0, false));
		};
	}

	private Hechizo crearHechizo(String nombre, int nivel, String tipo, String descripcion, int danio, int duracion,
			boolean requiereConcentracion) {
		Hechizo hechizo = new Hechizo();
		hechizo.setNombre(nombre);
		hechizo.setNivel(nivel);
		hechizo.setTipo(tipo);
		hechizo.setDescripcion(descripcion);
		hechizo.setDaño(danio);
		hechizo.setDuracion(duracion);
		hechizo.setRequiereConcentracion(requiereConcentracion);
		return hechizo;
	}
}
