package com.example.backend.repository;

import com.example.backend.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByNicNumber(String nicNumber);

    Optional<Customer> findByNicNumber(String nicNumber);

    @EntityGraph(attributePaths = {"mobileNumbers", "addresses", "addresses.city", "addresses.country", "familyMembers"})
    @Query("SELECT c FROM Customer c WHERE c.id = :id")
    Optional<Customer> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT c FROM Customer c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.nicNumber) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Customer> searchCustomers(@Param("search") String search, Pageable pageable);

    @Query("SELECT c.nicNumber FROM Customer c WHERE c.nicNumber IN :nicNumbers")
    List<String> findExistingNicNumbers(@Param("nicNumbers") List<String> nicNumbers);
}
