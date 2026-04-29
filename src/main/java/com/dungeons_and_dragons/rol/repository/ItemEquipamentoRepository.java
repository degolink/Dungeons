package com.dungeons_and_dragons.rol.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dungeons_and_dragons.rol.model.ItemEquipamento;

@Repository
public interface ItemEquipamentoRepository extends JpaRepository<ItemEquipamento, Long> {

    Optional<ItemEquipamento> findByItemInventarioId(Long itemInventarioId);

    List<ItemEquipamento> findByPersonajeIdAndEquipadoTrue(Long personajeId);

    List<ItemEquipamento> findByVillanoIdAndEquipadoTrue(Long villanoId);

    Optional<ItemEquipamento> findByPersonajeIdAndSlotAndEquipadoTrue(Long personajeId, String slot);

    Optional<ItemEquipamento> findByVillanoIdAndSlotAndEquipadoTrue(Long villanoId, String slot);
}
