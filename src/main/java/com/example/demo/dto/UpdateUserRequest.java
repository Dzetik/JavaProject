package com.example.demo.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Size(min = 2, max = 50)
    private String name;

    @Email
    private String email;
}