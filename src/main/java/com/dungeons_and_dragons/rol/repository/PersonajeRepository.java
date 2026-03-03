package com.dungeons_and_dragons.rol.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dungeons_and_dragons.rol.model.Personaje;

@Repository
public interface PersonajeRepository extends JpaRepository<Personaje, Long> {
    // JpaRepository ya te da:
    // findAll(), findById(id), save(entity), deleteById(id), etc.
}