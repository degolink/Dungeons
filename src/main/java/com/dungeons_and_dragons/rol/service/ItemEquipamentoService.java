package com.dungeons_and_dragons.rol.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dungeons_and_dragons.rol.model.ItemBase;
import com.dungeons_and_dragons.rol.model.ItemEquipamento;
import com.dungeons_and_dragons.rol.model.ItemInventario;
import com.dungeons_and_dragons.rol.model.Personaje;
import com.dungeons_and_dragons.rol.model.Villano;
import com.dungeons_and_dragons.rol.repository.ItemEquipamentoRepository;
import com.dungeons_and_dragons.rol.repository.ItemInventarioRepository;
import com.dungeons_and_dragons.rol.repository.PersonajeRepository;
import com.dungeons_and_dragons.rol.repository.VillanoRepository;

@Service
public class ItemEquipamentoService {

    private final ItemEquipamentoRepository itemEquipamentoRepository;
    private final ItemInventarioRepository itemInventarioRepository;
    private final PersonajeRepository personajeRepository;
    private final VillanoRepository villanoRepository;

    public ItemEquipamentoService(
            ItemEquipamentoRepository itemEquipamentoRepository,
            ItemInventarioRepository itemInventarioRepository,
            PersonajeRepository personajeRepository,
            VillanoRepository villanoRepository) {
        this.itemEquipamentoRepository = itemEquipamentoRepository;
        this.itemInventarioRepository = itemInventarioRepository;
        this.personajeRepository = personajeRepository;
        this.villanoRepository = villanoRepository;
    }

    @Transactional
    public ItemInventario equipar(ItemInventario itemInventario) {
        ItemBase itemBase = itemInventario.getItemBase();
        normalizarItemBase(itemBase);

        if (itemInventario.isConsumido()) {
            return itemInventario;
        }

        if (!itemBase.isEquipable()) {
            itemInventario.equipar();
            return itemInventarioRepository.save(itemInventario);
        }

        String slot = resolveSlot(itemBase);

        if (itemInventario.getPersonaje() != null) {
            desequiparConflictoPersonaje(itemInventario.getPersonaje().getId(), slot, itemInventario.getId());
        }

        if (itemInventario.getVillano() != null) {
            desequiparConflictoVillano(itemInventario.getVillano().getId(), slot, itemInventario.getId());
        }

        itemInventario.equipar();
        ItemInventario actualizado = itemInventarioRepository.save(itemInventario);

        ItemEquipamento itemEquipamento = itemEquipamentoRepository.findByItemInventarioId(actualizado.getId())
                .orElseGet(ItemEquipamento::new);

        itemEquipamento.setItemInventario(actualizado);
        itemEquipamento.setItemBase(itemBase);
        itemEquipamento.setPersonaje(actualizado.getPersonaje());
        itemEquipamento.setVillano(actualizado.getVillano());
        itemEquipamento.setSlot(slot);
        itemEquipamento.setEquipado(true);
        copiarBonos(itemBase, itemEquipamento);
        itemEquipamentoRepository.save(itemEquipamento);

        recalcularEstadisticas(actualizado);
        return actualizado;
    }

    @Transactional
    public ItemInventario desequipar(ItemInventario itemInventario) {
        itemInventario.desequipar();
        ItemInventario actualizado = itemInventarioRepository.save(itemInventario);

        itemEquipamentoRepository.findByItemInventarioId(actualizado.getId())
                .ifPresent(itemEquipamentoRepository::delete);

        recalcularEstadisticas(actualizado);
        return actualizado;
    }

    @Transactional
    public void eliminar(ItemInventario itemInventario) {
        itemEquipamentoRepository.findByItemInventarioId(itemInventario.getId())
                .ifPresent(itemEquipamentoRepository::delete);
        itemInventarioRepository.delete(itemInventario);
        recalcularEstadisticas(itemInventario);
    }

    @Transactional
    public void sincronizarEquipamentosExistentes() {
        itemInventarioRepository.findAll().forEach(itemInventario -> {
            if (itemInventario.isEquipado() && !itemInventario.isConsumido()) {
                equipar(itemInventario);
            }
        });
    }

    public void normalizarItemBase(ItemBase itemBase) {
        if (itemBase == null) {
            return;
        }

        String slot = resolveSlot(itemBase);
        if (itemBase.getSlotEquipamento() == null || itemBase.getSlotEquipamento().isBlank()) {
            itemBase.setSlotEquipamento(slot);
        }
        if (!itemBase.isApilable() && isTipoEquipable(itemBase.getTipo())) {
            itemBase.setEquipable(true);
        }
    }

    private void desequiparConflictoPersonaje(Long personajeId, String slot, Long itemInventarioIdActual) {
        if (personajeId == null || slot == null || slot.isBlank()) {
            return;
        }

        Optional<ItemEquipamento> conflicto = itemEquipamentoRepository
                .findByPersonajeIdAndSlotAndEquipadoTrue(personajeId, slot);

        conflicto.filter(item -> !item.getItemInventario().getId().equals(itemInventarioIdActual))
                .ifPresent(item -> desequipar(item.getItemInventario()));
    }

    private void desequiparConflictoVillano(Long villanoId, String slot, Long itemInventarioIdActual) {
        if (villanoId == null || slot == null || slot.isBlank()) {
            return;
        }

        Optional<ItemEquipamento> conflicto = itemEquipamentoRepository
                .findByVillanoIdAndSlotAndEquipadoTrue(villanoId, slot);

        conflicto.filter(item -> !item.getItemInventario().getId().equals(itemInventarioIdActual))
                .ifPresent(item -> desequipar(item.getItemInventario()));
    }

    private void recalcularEstadisticas(ItemInventario itemInventario) {
        if (itemInventario.getPersonaje() != null) {
            recalcularEstadisticasPersonaje(itemInventario.getPersonaje().getId());
        }
        if (itemInventario.getVillano() != null) {
            recalcularEstadisticasVillano(itemInventario.getVillano().getId());
        }
    }

    private void recalcularEstadisticasPersonaje(Long personajeId) {
        if (personajeId == null) {
            return;
        }

        Personaje personaje = personajeRepository.findById(personajeId).orElse(null);
        if (personaje == null) {
            return;
        }

        inicializarBases(personaje);
        List<ItemEquipamento> equipados = itemEquipamentoRepository.findByPersonajeIdAndEquipadoTrue(personajeId);

        personaje.setFuerza(personaje.getFuerzaBase() + sumar(equipados, ItemEquipamento::getBonusFuerza));
        personaje.setDestreza(personaje.getDestrezaBase() + sumar(equipados, ItemEquipamento::getBonusDestreza));
        personaje.setConstitucion(personaje.getConstitucionBase() + sumar(equipados, ItemEquipamento::getBonusConstitucion));
        personaje.setInteligencia(personaje.getInteligenciaBase() + sumar(equipados, ItemEquipamento::getBonusInteligencia));
        personaje.setSabiduria(personaje.getSabiduriaBase() + sumar(equipados, ItemEquipamento::getBonusSabiduria));
        personaje.setCarisma(personaje.getCarismaBase() + sumar(equipados, ItemEquipamento::getBonusCarisma));
        personaje.setPuntosVidaMax(personaje.getPuntosVidaMaxBase() + sumar(equipados, ItemEquipamento::getBonusVida));
        personaje.setPuntosEnergia(personaje.getPuntosEnergiaBase() + sumar(equipados, ItemEquipamento::getBonusEnergia));
        personaje.setAtaque(sumar(equipados, ItemEquipamento::getBonusAtaque));
        personaje.setDefensa(sumar(equipados, ItemEquipamento::getBonusDefensa));
        personaje.setIniciativaBonus(sumar(equipados, ItemEquipamento::getBonusIniciativa));
        personaje.setPuntosVida(Math.min(personaje.getPuntosVida(), personaje.getPuntosVidaMax()));

        personajeRepository.save(personaje);
    }

    private void recalcularEstadisticasVillano(Long villanoId) {
        if (villanoId == null) {
            return;
        }

        Villano villano = villanoRepository.findById(villanoId).orElse(null);
        if (villano == null) {
            return;
        }

        inicializarBases(villano);
        List<ItemEquipamento> equipados = itemEquipamentoRepository.findByVillanoIdAndEquipadoTrue(villanoId);

        villano.setFuerza(villano.getFuerzaBase() + sumar(equipados, ItemEquipamento::getBonusFuerza));
        villano.setDestreza(villano.getDestrezaBase() + sumar(equipados, ItemEquipamento::getBonusDestreza));
        villano.setConstitucion(villano.getConstitucionBase() + sumar(equipados, ItemEquipamento::getBonusConstitucion));
        villano.setInteligencia(villano.getInteligenciaBase() + sumar(equipados, ItemEquipamento::getBonusInteligencia));
        villano.setSabiduria(villano.getSabiduriaBase() + sumar(equipados, ItemEquipamento::getBonusSabiduria));
        villano.setCarisma(villano.getCarismaBase() + sumar(equipados, ItemEquipamento::getBonusCarisma));
        villano.setPuntosVidaMax(villano.getPuntosVidaMaxBase() + sumar(equipados, ItemEquipamento::getBonusVida));
        villano.setPuntosEnergia(villano.getPuntosEnergiaBase() + sumar(equipados, ItemEquipamento::getBonusEnergia));
        villano.setAtaque(sumar(equipados, ItemEquipamento::getBonusAtaque));
        villano.setDefensa(sumar(equipados, ItemEquipamento::getBonusDefensa));
        villano.setIniciativaBonus(sumar(equipados, ItemEquipamento::getBonusIniciativa));
        villano.setPuntosVida(Math.min(villano.getPuntosVida(), villano.getPuntosVidaMax()));

        villanoRepository.save(villano);
    }

    private void inicializarBases(Personaje personaje) {
        if (personaje.getFuerzaBase() == null) personaje.setFuerzaBase(personaje.getFuerza());
        if (personaje.getDestrezaBase() == null) personaje.setDestrezaBase(personaje.getDestreza());
        if (personaje.getConstitucionBase() == null) personaje.setConstitucionBase(personaje.getConstitucion());
        if (personaje.getInteligenciaBase() == null) personaje.setInteligenciaBase(personaje.getInteligencia());
        if (personaje.getSabiduriaBase() == null) personaje.setSabiduriaBase(personaje.getSabiduria());
        if (personaje.getCarismaBase() == null) personaje.setCarismaBase(personaje.getCarisma());
        if (personaje.getPuntosVidaMaxBase() == null) personaje.setPuntosVidaMaxBase(personaje.getPuntosVidaMax());
        if (personaje.getPuntosEnergiaBase() == null) personaje.setPuntosEnergiaBase(personaje.getPuntosEnergia());
    }

    private void inicializarBases(Villano villano) {
        if (villano.getFuerzaBase() == null) villano.setFuerzaBase(villano.getFuerza());
        if (villano.getDestrezaBase() == null) villano.setDestrezaBase(villano.getDestreza());
        if (villano.getConstitucionBase() == null) villano.setConstitucionBase(villano.getConstitucion());
        if (villano.getInteligenciaBase() == null) villano.setInteligenciaBase(villano.getInteligencia());
        if (villano.getSabiduriaBase() == null) villano.setSabiduriaBase(villano.getSabiduria());
        if (villano.getCarismaBase() == null) villano.setCarismaBase(villano.getCarisma());
        if (villano.getPuntosVidaMaxBase() == null) villano.setPuntosVidaMaxBase(villano.getPuntosVidaMax());
        if (villano.getPuntosEnergiaBase() == null) villano.setPuntosEnergiaBase(villano.getPuntosEnergia());
    }

    private void copiarBonos(ItemBase itemBase, ItemEquipamento itemEquipamento) {
        itemEquipamento.setBonusFuerza(itemBase.getBonusFuerza());
        itemEquipamento.setBonusDestreza(itemBase.getBonusDestreza());
        itemEquipamento.setBonusConstitucion(itemBase.getBonusConstitucion());
        itemEquipamento.setBonusInteligencia(itemBase.getBonusInteligencia());
        itemEquipamento.setBonusSabiduria(itemBase.getBonusSabiduria());
        itemEquipamento.setBonusCarisma(itemBase.getBonusCarisma());
        itemEquipamento.setBonusVida(itemBase.getBonusVida());
        itemEquipamento.setBonusEnergia(itemBase.getBonusEnergia());
        itemEquipamento.setBonusAtaque(itemBase.getBonusAtaque());
        itemEquipamento.setBonusDefensa(itemBase.getBonusDefensa());
        itemEquipamento.setBonusIniciativa(itemBase.getBonusIniciativa());
    }

    private String resolveSlot(ItemBase itemBase) {
        if (itemBase == null) {
            return "miscelaneo";
        }

        String slot = itemBase.getSlotEquipamento();
        if (slot != null && !slot.isBlank()) {
            return slot.trim().toLowerCase(Locale.ROOT);
        }

        String tipo = itemBase.getTipo() == null ? "" : itemBase.getTipo().trim().toLowerCase(Locale.ROOT);
        return switch (tipo) {
            case "arma" -> "arma";
            case "armadura" -> "armadura";
            case "escudo" -> "escudo";
            case "casco" -> "casco";
            case "botas" -> "botas";
            case "anillo" -> "anillo";
            case "amuleto", "accesorio" -> "accesorio";
            default -> tipo.isBlank() ? "miscelaneo" : tipo;
        };
    }

    private boolean isTipoEquipable(String tipo) {
        String valor = tipo == null ? "" : tipo.trim().toLowerCase(Locale.ROOT);
        return switch (valor) {
            case "arma", "armadura", "escudo", "casco", "botas", "anillo", "amuleto", "accesorio" -> true;
            default -> false;
        };
    }

    private int sumar(List<ItemEquipamento> items, BonusExtractor extractor) {
        return items.stream().mapToInt(extractor::extract).sum();
    }

    @FunctionalInterface
    private interface BonusExtractor {
        int extract(ItemEquipamento itemEquipamento);
    }
}
