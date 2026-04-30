package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {
    private Long id;

    @NotBlank(message = "Address line 1 is mandatory")
    private String addressLine1;

    private String addressLine2;

    @NotNull(message = "City is mandatory")
    private Long cityId;

    @NotNull(message = "Country is mandatory")
    private Long countryId;

    // Read-only fields for responses
    private String cityName;
    private String countryName;
}
