package com.dungeons_and_dragons.rol.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
// se importa con jakarta.* para usar las anotaciones de JPA, como @Entity, @Id, etc. Esto es necesario para que la clase Notes pueda ser mapeada a una tabla en la base de datos y para que se puedan realizar operaciones CRUD (Crear, Leer, Actualizar, Eliminar) sobre los objetos de esta clase a través de un repositorio de Spring Data JPA.
@Entity
@Data
@NoArgsConstructor
public class Notes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    private String contenido;

    public Notes(String contenido, Long id, String titulo) {
        this.contenido = contenido;
        this.id = id;
        this.titulo = titulo;
    }
}
