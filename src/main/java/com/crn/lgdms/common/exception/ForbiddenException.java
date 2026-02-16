package com.crn.lgdms.common.exception;

public class ForbiddenException extends ApiException {
    public ForbiddenException(String message) {
        super(message, "FORBIDDEN");
    }

    public ForbiddenException() {
        super("Access denied", "FORBIDDEN");
    }
}
