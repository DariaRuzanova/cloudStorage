package com.example.cloudstorage.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class AuthentificationRequest {
    @NotNull
    private String login;
    @NotNull
    private String password;

}
