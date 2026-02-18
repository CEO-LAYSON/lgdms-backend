package com.crn.lgdms.modules.sales.repository;

import com.crn.lgdms.modules.sales.domain.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {

    Optional<Customer> findByCustomerNumber(String customerNumber);

    Optional<Customer> findByPhone(String phone);

    Optional<Customer> findByEmail(String email);

    boolean existsByCustomerNumber(String customerNumber);

    @Query("SELECT c FROM Customer c WHERE " +
        "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
        "c.phone LIKE CONCAT('%', :searchTerm, '%') OR " +
        "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Customer> searchCustomers(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE c.currentBalance > c.creditLimit")
    Page<Customer> findCustomersOverCreditLimit(Pageable pageable);
}
