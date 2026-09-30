package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class CardCraftSource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "result_card_id", unique = true)
    private Card resultCard;

    @OneToMany
    @JoinTable(
            name = "card_craft_source_cards",
            joinColumns = @JoinColumn(name = "craft_source_id"),
            inverseJoinColumns = @JoinColumn(name = "card_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_card_craft_source_card",
                    columnNames = "card_id"
            )
    )
    private List<Card> ingredientCards = new ArrayList<>();
}
