package com.dungeons_and_dragons.rol.dnd.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DndResultItemDTO {
    private String index;
    private String name;
    private String url;
}
