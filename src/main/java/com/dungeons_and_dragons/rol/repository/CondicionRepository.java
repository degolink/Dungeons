package com.dungeons_and_dragons.rol.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dungeons_and_dragons.rol.model.Condicion;

@Repository
public interface CondicionRepository extends JpaRepository<Condicion, Long> {
    // Opcional: búsquedas personalizadas
    // List<Condicion> findByNombre(String nombre);
}