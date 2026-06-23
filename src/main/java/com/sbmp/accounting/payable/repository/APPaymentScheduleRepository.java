package com.sbmp.accounting.payable.repository;

import com.sbmp.accounting.payable.entity.APPaymentSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface APPaymentScheduleRepository extends JpaRepository<APPaymentSchedule, Long> {

    List<APPaymentSchedule> findByAccountsPayableId(Long apId);

    @Query("""
        SELECT aps FROM APPaymentSchedule aps
        WHERE aps.accountsPayable.id = :apId
        ORDER BY aps.scheduledDate ASC
    """)
    List<APPaymentSchedule> findSchedulesByAPId(
            @Param("apId") Long apId
    );

    @Query("""
        SELECT COALESCE(SUM(aps.paidAmount), 0)
        FROM APPaymentSchedule aps
        WHERE aps.accountsPayable.id = :apId
    """)
    BigDecimal getTotalPaidByAP(
            @Param("apId") Long apId
    );
}