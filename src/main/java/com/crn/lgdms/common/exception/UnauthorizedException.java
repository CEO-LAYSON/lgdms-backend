package com.crn.lgdms.common.exception;

public class UnauthorizedException extends ApiException {
    public UnauthorizedException(String message) {
        super(message, "UNAUTHORIZED");
    }

    public UnauthorizedException() {
        super("Authentication required", "UNAUTHORIZED");
    }
}
