package com.example.cloudstorage.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NewFileName {
    @NotNull
    String fileName;
}
