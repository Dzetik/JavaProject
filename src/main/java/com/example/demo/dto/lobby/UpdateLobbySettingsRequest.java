package com.example.demo.dto.lobby;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateLobbySettingsRequest {
    @NotNull(message = "Количество карт на столе обязательно")
    @Min(value = 3, message = "Минимум 3 карты на столе")
    @Max(value = 10, message = "Максимум 10 карт на столе")
    private Integer initialTableCards;

    @NotNull(message = "Количество карт в руке обязательно")
    @Min(value = 3, message = "Минимум 3 карты в руке")
    @Max(value = 10, message = "Максимум 10 карт в руке")
    private Integer initialHandCards;
}