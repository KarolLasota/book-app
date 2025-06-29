package com.library.demo.dto;


import lombok.*;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookSearchResponse {
    private int totalItems;

    private List<SimplifiedBook> books;


}
