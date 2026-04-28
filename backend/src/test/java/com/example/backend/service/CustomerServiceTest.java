package com.example.backend.service;

import com.example.backend.dto.CustomerCreateRequest;
import com.example.backend.dto.CustomerDTO;
import com.example.backend.dto.CustomerUpdateRequest;
import com.example.backend.exception.DuplicateResourceException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.model.Customer;
import com.example.backend.repository.CityRepository;
import com.example.backend.repository.CountryRepository;
import com.example.backend.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private CountryRepository countryRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer sampleCustomer;
    private CustomerCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        sampleCustomer = new Customer();
        sampleCustomer.setId(1L);
        sampleCustomer.setName("John Doe");
        sampleCustomer.setDateOfBirth(LocalDate.of(1990, 1, 15));
        sampleCustomer.setNicNumber("199012345678");
        sampleCustomer.setCreatedAt(LocalDateTime.now());
        sampleCustomer.setUpdatedAt(LocalDateTime.now());

        createRequest = new CustomerCreateRequest();
        createRequest.setName("John Doe");
        createRequest.setDateOfBirth(LocalDate.of(1990, 1, 15));
        createRequest.setNicNumber("199012345678");
        createRequest.setMobileNumbers(Arrays.asList("0771234567"));
        createRequest.setAddresses(new ArrayList<>());
        createRequest.setFamilyMemberIds(new ArrayList<>());
    }

    @Test
    void createCustomer_Success() {
        when(customerRepository.existsByNicNumber("199012345678")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(sampleCustomer);
        when(customerRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(sampleCustomer));

        CustomerDTO result = customerService.createCustomer(createRequest);

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("199012345678", result.getNicNumber());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void createCustomer_DuplicateNIC_ThrowsException() {
        when(customerRepository.existsByNicNumber("199012345678")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                customerService.createCustomer(createRequest));

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void getCustomer_Found() {
        when(customerRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(sampleCustomer));

        CustomerDTO result = customerService.getCustomer(1L);

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
    }

    @Test
    void getCustomer_NotFound() {
        when(customerRepository.findByIdWithDetails(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                customerService.getCustomer(99L));
    }

    @Test
    void getAllCustomers_WithSearch() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Customer> page = new PageImpl<>(Arrays.asList(sampleCustomer));

        when(customerRepository.searchCustomers("John", pageable)).thenReturn(page);

        Page<CustomerDTO> result = customerService.getAllCustomers("John", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getAllCustomers_NoSearch() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Customer> page = new PageImpl<>(Arrays.asList(sampleCustomer));

        when(customerRepository.findAll(pageable)).thenReturn(page);

        Page<CustomerDTO> result = customerService.getAllCustomers(null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void updateCustomer_Success() {
        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest();
        updateRequest.setName("John Updated");
        updateRequest.setDateOfBirth(LocalDate.of(1990, 1, 15));
        updateRequest.setNicNumber("199012345678");
        updateRequest.setMobileNumbers(new ArrayList<>());
        updateRequest.setAddresses(new ArrayList<>());
        updateRequest.setFamilyMemberIds(new ArrayList<>());

        when(customerRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(sampleCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(sampleCustomer);

        CustomerDTO result = customerService.updateCustomer(1L, updateRequest);

        assertNotNull(result);
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void updateCustomer_NotFound() {
        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest();
        updateRequest.setName("Test");
        updateRequest.setDateOfBirth(LocalDate.now());
        updateRequest.setNicNumber("123");

        when(customerRepository.findByIdWithDetails(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                customerService.updateCustomer(99L, updateRequest));
    }

    @Test
    void deleteCustomer_Success() {
        when(customerRepository.existsById(1L)).thenReturn(true);
        doNothing().when(customerRepository).deleteById(1L);

        customerService.deleteCustomer(1L);

        verify(customerRepository).deleteById(1L);
    }

    @Test
    void deleteCustomer_NotFound() {
        when(customerRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () ->
                customerService.deleteCustomer(99L));
    }
}
