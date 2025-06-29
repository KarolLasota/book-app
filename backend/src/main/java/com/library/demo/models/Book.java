package com.library.demo.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String googleBookId;
    private String title;
    private String authors;
    private String thumbnail;
    private String description;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
