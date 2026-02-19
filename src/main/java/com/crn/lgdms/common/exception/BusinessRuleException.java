package com.crn.lgdms.common.exception;

import lombok.Getter;

@Getter
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
