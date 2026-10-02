package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "decks")
@Getter
@Setter
@NoArgsConstructor
public class Deck {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "game_session_id",
            nullable = false,
            unique = true
    )
    private GameSession gameSession;

    @OneToMany(
            mappedBy = "deck",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Card> cards = new ArrayList<>();
}