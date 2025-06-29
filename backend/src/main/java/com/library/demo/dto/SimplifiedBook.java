package com.library.demo.dto;

import jakarta.validation.constraints.NotBlank;

public record SimplifiedBook(
        @NotBlank String googleBookId,
        @NotBlank String title,
        String authors,
        String description,
        String thumbnail

) {
}
