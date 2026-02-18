package com.crn.lgdms.modules.sales.service;

import com.crn.lgdms.common.enums.AuditAction;
import com.crn.lgdms.common.exception.ConflictException;
import com.crn.lgdms.common.exception.NotFoundException;
import com.crn.lgdms.modules.sales.domain.entity.Customer;
import com.crn.lgdms.modules.sales.dto.request.CreateCustomerRequest;
import com.crn.lgdms.modules.sales.dto.request.UpdateCustomerRequest;
import com.crn.lgdms.modules.sales.dto.response.CustomerResponse;
import com.crn.lgdms.modules.sales.dto.mapper.CustomerMapper;
import com.crn.lgdms.modules.sales.repository.CustomerRepository;
import com.crn.lgdms.modules.users.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final AuditLogService auditLogService;

    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        log.info("Creating new customer: {}", request.getName());

        if (request.getPhone() != null && customerRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new ConflictException("Customer with phone " + request.getPhone() + " already exists");
        }

        if (request.getEmail() != null && customerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("Customer with email " + request.getEmail() + " already exists");
        }

        Customer customer = customerMapper.toEntity(request);
        customer.setCustomerNumber(generateCustomerNumber());

        Customer saved = customerRepository.save(customer);

        auditLogService.log(AuditAction.CREATE, "Customer", saved.getId(),
            null, saved.getName(), getCurrentUsername());

        return customerMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "customers", key = "#id")
    public CustomerResponse getCustomerById(String id) {
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Customer not found with id: " + id));
        return customerMapper.toResponse(customer);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerByPhone(String phone) {
        Customer customer = customerRepository.findByPhone(phone)
            .orElseThrow(() -> new NotFoundException("Customer not found with phone: " + phone));
        return customerMapper.toResponse(customer);
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> searchCustomers(String searchTerm, Pageable pageable) {
        return customerRepository.searchCustomers(searchTerm, pageable)
            .map(customerMapper::toResponse);
    }

    @Transactional
    @CacheEvict(value = "customers", key = "#id")
    public CustomerResponse updateCustomer(String id, UpdateCustomerRequest request) {
        log.info("Updating customer with ID: {}", id);

        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Customer not found with id: " + id));

        if (request.getPhone() != null && !request.getPhone().equals(customer.getPhone())) {
            if (customerRepository.findByPhone(request.getPhone()).isPresent()) {
                throw new ConflictException("Customer with phone " + request.getPhone() + " already exists");
            }
        }

        if (request.getEmail() != null && !request.getEmail().equals(customer.getEmail())) {
            if (customerRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new ConflictException("Customer with email " + request.getEmail() + " already exists");
            }
        }

        customerMapper.updateEntity(request, customer);
        Customer updated = customerRepository.save(customer);

        auditLogService.log(AuditAction.UPDATE, "Customer", id,
            null, updated.getName(), getCurrentUsername());

        return customerMapper.toResponse(updated);
    }

    @Transactional
    @CacheEvict(value = "customers", key = "#id")
    public void deleteCustomer(String id) {
        log.info("Deleting customer with ID: {}", id);

        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Customer not found with id: " + id));

        customer.setActive(false);
        customerRepository.save(customer);

        auditLogService.log(AuditAction.DELETE, "Customer", id,
            customer.getName(), "deactivated", getCurrentUsername());
    }

    private String generateCustomerNumber() {
        String prefix = "CUST";
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sequence = String.format("%04d", getNextSequenceNumber());
        return prefix + "-" + date + "-" + sequence;
    }

    private synchronized int getNextSequenceNumber() {
        return (int) (customerRepository.count() + 1);
    }

    private String getCurrentUsername() {
        return "SYSTEM";
    }
}
