package com.dungeons_and_dragons.rol.dnd.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DndSpellListDTO {
    private int count;
    private List<DndResultItemDTO> results;
}
