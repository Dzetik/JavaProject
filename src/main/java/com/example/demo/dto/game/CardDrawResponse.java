package com.example.demo.dto.game;

import com.example.demo.entity.CardLocation;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CardDrawResponse {
    private Long id;
    private Long recipeId;
    private CardLocation location;
}
