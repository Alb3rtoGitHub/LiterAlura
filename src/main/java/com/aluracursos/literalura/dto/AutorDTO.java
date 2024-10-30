package com.aluracursos.literalura.dto;

import java.util.List;

public record AutorDTO(
        Long id,
        String nombre,
        String fechaNacimiento,
        String fechaFallecimiento
) {
}
