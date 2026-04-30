package com.example.backend.service;

import com.example.backend.dto.AddressDTO;
import com.example.backend.dto.CustomerCreateRequest;
import com.example.backend.dto.CustomerDTO;
import com.example.backend.dto.CustomerUpdateRequest;
import com.example.backend.exception.DuplicateResourceException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.model.*;
import com.example.backend.repository.CityRepository;
import com.example.backend.repository.CountryRepository;
import com.example.backend.repository.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CityRepository cityRepository;
    private final CountryRepository countryRepository;

    public CustomerService(CustomerRepository customerRepository,
                           CityRepository cityRepository,
                           CountryRepository countryRepository) {
        this.customerRepository = customerRepository;
        this.cityRepository = cityRepository;
        this.countryRepository = countryRepository;
    }

    @Transactional
    public CustomerDTO createCustomer(CustomerCreateRequest request) {
        if (customerRepository.existsByNicNumber(request.getNicNumber())) {
            throw new DuplicateResourceException("Customer with NIC " + request.getNicNumber() + " already exists");
        }

        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setNicNumber(request.getNicNumber());

        // Add mobile numbers
        if (request.getMobileNumbers() != null) {
            for (String mobile : request.getMobileNumbers()) {
                if (mobile != null && !mobile.trim().isEmpty()) {
                    customer.addMobile(new CustomerMobile(mobile.trim()));
                }
            }
        }

        // Add addresses
        if (request.getAddresses() != null) {
            for (AddressDTO addrDto : request.getAddresses()) {
                CustomerAddress address = mapToAddress(addrDto);
                customer.addAddress(address);
            }
        }

        Customer saved = customerRepository.save(customer);

        // Add family members
        if (request.getFamilyMemberIds() != null && !request.getFamilyMemberIds().isEmpty()) {
            for (Long memberId : request.getFamilyMemberIds()) {
                Customer member = customerRepository.findById(memberId)
                        .orElseThrow(() -> new ResourceNotFoundException("Family member not found: " + memberId));
                saved.addFamilyMember(member);
            }
            saved = customerRepository.save(saved);
        }

        return mapToDTO(customerRepository.findByIdWithDetails(saved.getId()).orElse(saved));
    }

    @Transactional
    public CustomerDTO updateCustomer(Long id, CustomerUpdateRequest request) {
        Customer customer = customerRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        // Check NIC uniqueness if changed
        if (!customer.getNicNumber().equals(request.getNicNumber())
                && customerRepository.existsByNicNumber(request.getNicNumber())) {
            throw new DuplicateResourceException("Customer with NIC " + request.getNicNumber() + " already exists");
        }

        customer.setName(request.getName());
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setNicNumber(request.getNicNumber());

        // Replace mobile numbers
        customer.getMobileNumbers().clear();
        if (request.getMobileNumbers() != null) {
            for (String mobile : request.getMobileNumbers()) {
                if (mobile != null && !mobile.trim().isEmpty()) {
                    customer.addMobile(new CustomerMobile(mobile.trim()));
                }
            }
        }

        // Replace addresses
        customer.getAddresses().clear();
        if (request.getAddresses() != null) {
            for (AddressDTO addrDto : request.getAddresses()) {
                CustomerAddress address = mapToAddress(addrDto);
                customer.addAddress(address);
            }
        }

        // Replace family members
        customer.getFamilyMembers().clear();
        if (request.getFamilyMemberIds() != null) {
            for (Long memberId : request.getFamilyMemberIds()) {
                if (!memberId.equals(id)) {
                    Customer member = customerRepository.findById(memberId)
                            .orElseThrow(() -> new ResourceNotFoundException("Family member not found: " + memberId));
                    customer.getFamilyMembers().add(member);
                }
            }
        }

        Customer saved = customerRepository.save(customer);
        return mapToDTO(customerRepository.findByIdWithDetails(saved.getId()).orElse(saved));
    }

    @Transactional(readOnly = true)
    public CustomerDTO getCustomer(Long id) {
        Customer customer = customerRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        return mapToDTO(customer);
    }

    @Transactional(readOnly = true)
    public Page<CustomerDTO> getAllCustomers(String search, Pageable pageable) {
        Page<Customer> page;
        if (search != null && !search.trim().isEmpty()) {
            page = customerRepository.searchCustomers(search.trim(), pageable);
        } else {
            page = customerRepository.findAll(pageable);
        }
        return page.map(this::mapToSimpleDTO);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Customer not found with id: " + id);
        }
        customerRepository.deleteById(id);
    }

    private CustomerAddress mapToAddress(AddressDTO dto) {
        CustomerAddress address = new CustomerAddress();
        address.setAddressLine1(dto.getAddressLine1());
        address.setAddressLine2(dto.getAddressLine2());

        City city = cityRepository.findById(dto.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("City not found: " + dto.getCityId()));
        address.setCity(city);

        Country country = countryRepository.findById(dto.getCountryId())
                .orElseThrow(() -> new ResourceNotFoundException("Country not found: " + dto.getCountryId()));
        address.setCountry(country);

        return address;
    }

    public CustomerDTO mapToDTO(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setDateOfBirth(customer.getDateOfBirth());
        dto.setNicNumber(customer.getNicNumber());
        dto.setCreatedAt(customer.getCreatedAt() != null ? customer.getCreatedAt().toString() : null);
        dto.setUpdatedAt(customer.getUpdatedAt() != null ? customer.getUpdatedAt().toString() : null);

        // Mobile numbers
        if (customer.getMobileNumbers() != null) {
            dto.setMobileNumbers(customer.getMobileNumbers().stream()
                    .map(CustomerMobile::getMobileNumber)
                    .collect(Collectors.toList()));
        }

        // Addresses
        if (customer.getAddresses() != null) {
            dto.setAddresses(customer.getAddresses().stream().map(addr -> {
                AddressDTO addrDto = new AddressDTO();
                addrDto.setId(addr.getId());
                addrDto.setAddressLine1(addr.getAddressLine1());
                addrDto.setAddressLine2(addr.getAddressLine2());
                addrDto.setCityId(addr.getCity().getId());
                addrDto.setCityName(addr.getCity().getName());
                addrDto.setCountryId(addr.getCountry().getId());
                addrDto.setCountryName(addr.getCountry().getName());
                return addrDto;
            }).collect(Collectors.toList()));
        }

        // Family members
        if (customer.getFamilyMembers() != null) {
            dto.setFamilyMembers(customer.getFamilyMembers().stream().map(fm -> {
                CustomerDTO.FamilyMemberDTO fmDto = new CustomerDTO.FamilyMemberDTO();
                fmDto.setId(fm.getId());
                fmDto.setName(fm.getName());
                fmDto.setNicNumber(fm.getNicNumber());
                return fmDto;
            }).collect(Collectors.toList()));
        }

        return dto;
    }

    private CustomerDTO mapToSimpleDTO(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setDateOfBirth(customer.getDateOfBirth());
        dto.setNicNumber(customer.getNicNumber());
        dto.setCreatedAt(customer.getCreatedAt() != null ? customer.getCreatedAt().toString() : null);
        dto.setUpdatedAt(customer.getUpdatedAt() != null ? customer.getUpdatedAt().toString() : null);
        dto.setMobileNumbers(new ArrayList<>());
        dto.setAddresses(new ArrayList<>());
        dto.setFamilyMembers(new ArrayList<>());
        return dto;
    }
}
