package com.crn.lgdms.common.pagination;

import com.crn.lgdms.common.constants.SystemDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class PageRequestFactory {

    public Pageable create(int page, int size) {
        return create(page, size, null, null);
    }

    public Pageable create(int page, int size, String sortBy, String sortDirection) {
        int pageNumber = Math.max(page, 0);
        int pageSize = size > 0 ? Math.min(size, SystemDefaults.MAX_PAGE_SIZE) : SystemDefaults.DEFAULT_PAGE_SIZE;

        if (sortBy != null && !sortBy.isEmpty()) {
            Sort.Direction direction = Sort.Direction.fromOptionalString(sortDirection)
                .orElse(Sort.Direction.ASC);
            Sort sort = Sort.by(direction, sortBy);
            return PageRequest.of(pageNumber, pageSize, sort);
        }

        return PageRequest.of(pageNumber, pageSize);
    }
}
