package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCreateRequest {

    @NotBlank(message = "Name is mandatory")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @NotNull(message = "Date of birth is mandatory")
    private LocalDate dateOfBirth;

    @NotBlank(message = "NIC number is mandatory")
    @Size(max = 20, message = "NIC number must not exceed 20 characters")
    private String nicNumber;

    private List<String> mobileNumbers = new ArrayList<>();

    @Valid
    private List<AddressDTO> addresses = new ArrayList<>();

    private List<Long> familyMemberIds = new ArrayList<>();
}
