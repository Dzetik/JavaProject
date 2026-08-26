package com.example.demo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateLobbyRequest {
    @NotBlank(message = "Название лобби не может быть пустым")
    @Size(max = 100, message = "Название лобби не должно превышать 100 символов")
    private String name;

    @NotNull(message = "Максимальное количество игроков обязательно")
    @Min(value = 2, message = "Минимальное количество игроков: 2")
    @Max(value = 6, message = "Максимальное количество игроков: 6")
    private Integer maxPlayers;
}