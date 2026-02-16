package com.crn.lgdms.common.pagination;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class SortFactory {

    public Sort create(String... properties) {
        List<Sort.Order> orders = new ArrayList<>();
        for (String property : properties) {
            if (property.startsWith("-")) {
                orders.add(Sort.Order.desc(property.substring(1)));
            } else {
                orders.add(Sort.Order.asc(property));
            }
        }
        return Sort.by(orders);
    }

    public Sort create(Sort.Direction direction, String... properties) {
        return Sort.by(direction, properties);
    }
}
