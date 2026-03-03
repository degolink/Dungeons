package com.dungeons_and_dragons.rol.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dungeons_and_dragons.rol.model.ItemBase;

@Repository
public interface ItemBaseRepository extends JpaRepository<ItemBase, Long> {
    // Opcional: agregar búsquedas personalizadas
    // List<ItemBase> findByTipo(String tipo);
    // List<ItemBase> findByRareza(String rareza);
}