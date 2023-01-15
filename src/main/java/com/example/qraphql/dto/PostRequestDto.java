package com.example.qraphql.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostRequestDto {
    @NotBlank
    @Size(min=2, max = 25)
    private String title;

    @NotBlank
    @Size(min=2, max = 255)
    private String body;
}
