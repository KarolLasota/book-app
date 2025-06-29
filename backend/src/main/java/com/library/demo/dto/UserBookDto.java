package com.library.demo.dto;

public record UserBookDto(
        Long id,
        String googleBookId,
        String title,
        String authors,
        String description,
        String thumbnail
) {}