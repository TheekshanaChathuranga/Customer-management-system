package com.example.backend.controller;

import com.example.backend.dto.CustomerCreateRequest;
import com.example.backend.dto.CustomerDTO;
import com.example.backend.dto.CustomerUpdateRequest;
import com.example.backend.exception.GlobalExceptionHandler;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;

    private CustomerDTO sampleDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(customerController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        sampleDTO = new CustomerDTO();
        sampleDTO.setId(1L);
        sampleDTO.setName("John Doe");
        sampleDTO.setDateOfBirth(LocalDate.of(1990, 1, 15));
        sampleDTO.setNicNumber("199012345678");
        sampleDTO.setMobileNumbers(Arrays.asList("0771234567"));
        sampleDTO.setAddresses(new ArrayList<>());
        sampleDTO.setFamilyMembers(new ArrayList<>());
    }

    @Test
    void createCustomer_Returns201() throws Exception {
        CustomerCreateRequest request = new CustomerCreateRequest();
        request.setName("John Doe");
        request.setDateOfBirth(LocalDate.of(1990, 1, 15));
        request.setNicNumber("199012345678");

        when(customerService.createCustomer(any(CustomerCreateRequest.class))).thenReturn(sampleDTO);

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.nicNumber").value("199012345678"));
    }

    @Test
    void createCustomer_InvalidData_Returns400() throws Exception {
        CustomerCreateRequest request = new CustomerCreateRequest();
        // Missing mandatory fields

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCustomer_Returns200() throws Exception {
        when(customerService.getCustomer(1L)).thenReturn(sampleDTO);

        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void getCustomer_NotFound_Returns404() throws Exception {
        when(customerService.getCustomer(99L))
                .thenThrow(new ResourceNotFoundException("Customer not found"));

        mockMvc.perform(get("/api/customers/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllCustomers_Returns200() throws Exception {
        Page<CustomerDTO> page = new PageImpl<>(Arrays.asList(sampleDTO));
        when(customerService.getAllCustomers(any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/customers")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("John Doe"));
    }

    @Test
    void updateCustomer_Returns200() throws Exception {
        CustomerUpdateRequest request = new CustomerUpdateRequest();
        request.setName("John Updated");
        request.setDateOfBirth(LocalDate.of(1990, 1, 15));
        request.setNicNumber("199012345678");

        when(customerService.updateCustomer(eq(1L), any(CustomerUpdateRequest.class))).thenReturn(sampleDTO);

        mockMvc.perform(put("/api/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteCustomer_Returns204() throws Exception {
        mockMvc.perform(delete("/api/customers/1"))
                .andExpect(status().isNoContent());
    }
}
