package com.crn.lgdms.modules.credit.repository;

import com.crn.lgdms.modules.credit.domain.entity.CreditLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CreditLimitRepository extends JpaRepository<CreditLimit, String> {

    Optional<CreditLimit> findByCustomerIdAndIsCurrentTrue(String customerId);

    Optional<CreditLimit> findByLocationIdAndIsCurrentTrue(String locationId);

    List<CreditLimit> findByCustomerIdOrderByEffectiveFromDesc(String customerId);

    List<CreditLimit> findByLocationIdOrderByEffectiveFromDesc(String locationId);

    @Query("SELECT cl FROM CreditLimit cl WHERE cl.effectiveTo < :date AND cl.isCurrent = true")
    List<CreditLimit> findExpiredLimits(@Param("date") LocalDateTime date);
}
