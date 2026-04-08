package com.dungeons_and_dragons.rol.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dungeons_and_dragons.rol.model.ItemInventario;

@Repository
public interface ItemInventarioRepository extends JpaRepository<ItemInventario, Long> {
}
