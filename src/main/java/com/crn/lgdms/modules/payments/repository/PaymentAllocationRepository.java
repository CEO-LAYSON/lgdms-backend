package com.crn.lgdms.modules.payments.repository;

import com.crn.lgdms.modules.payments.domain.entity.PaymentAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PaymentAllocationRepository extends JpaRepository<PaymentAllocation, String> {

    List<PaymentAllocation> findByPaymentId(String paymentId);

    List<PaymentAllocation> findBySaleId(String saleId);

    @Query("SELECT SUM(pa.allocatedAmount) FROM PaymentAllocation pa WHERE pa.sale.id = :saleId")
    BigDecimal getTotalAllocatedToSale(@Param("saleId") String saleId);
}
