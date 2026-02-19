package com.crn.lgdms.integration.accounting;

import com.crn.lgdms.modules.sales.domain.entity.Sale;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class AccountingMapper {

    public Map<String, Object> mapSaleToAccountingFormat(Sale sale) {
        Map<String, Object> result = new HashMap<>();

        result.put("documentType", "INVOICE");
        result.put("documentNumber", sale.getInvoiceNumber());
        result.put("documentDate", sale.getSaleDate().toString());
        result.put("customer", sale.getCustomer() != null ? sale.getCustomer().getName() : "Walk-in");
        result.put("totalAmount", sale.getTotalAmount());
        result.put("tax", sale.getTax());
        result.put("items", sale.getItems().stream().map(item -> {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("product", item.getCylinderSize().getName());
            itemMap.put("type", item.getProductType().toString());
            itemMap.put("quantity", item.getQuantity());
            itemMap.put("unitPrice", item.getUnitPrice());
            itemMap.put("total", item.getTotalPrice());
            return itemMap;
        }).toList());

        return result;
    }
}
