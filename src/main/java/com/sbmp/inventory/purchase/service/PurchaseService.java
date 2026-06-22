package com.sbmp.inventory.purchase.service;

import com.sbmp.inventory.purchase.dto.*;
import com.sbmp.inventory.purchase.entity.Purchase.PurchaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;

public interface PurchaseService {

    PurchaseResponseDTO createPurchase(PurchaseRequestDTO requestDTO);

    PurchaseResponseDTO updatePurchase(Long id, PurchaseRequestDTO requestDTO);

    PurchaseResponseDTO getPurchaseById(Long id);

    PurchaseResponseDTO getPurchaseByNumber(String purchaseNumber);

    Page<PurchaseSummaryDTO> searchPurchases(
            Long businessId,
            PurchaseStatus status,
            Long supplierId,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    );

    PurchaseResponseDTO completePurchase(Long id);

    PurchaseResponseDTO saveDraft(PurchaseRequestDTO requestDTO);

    void cancelPurchase(Long id);

    void deletePurchase(Long id);

    List<PurchaseSummaryDTO> getAllByBusiness(Long businessId);
}
