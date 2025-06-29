package com.library.demo.dto;

import java.util.List;

public record PagedUserBooksResponse(
        List<UserBookDto> content,
        long totalElements
) {}
