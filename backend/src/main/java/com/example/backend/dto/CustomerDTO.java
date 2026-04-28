package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {
    private Long id;
    private String name;
    private LocalDate dateOfBirth;
    private String nicNumber;
    private List<String> mobileNumbers = new ArrayList<>();
    private List<AddressDTO> addresses = new ArrayList<>();
    private List<FamilyMemberDTO> familyMembers = new ArrayList<>();
    private String createdAt;
    private String updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FamilyMemberDTO {
        private Long id;
        private String name;
        private String nicNumber;
    }
}
