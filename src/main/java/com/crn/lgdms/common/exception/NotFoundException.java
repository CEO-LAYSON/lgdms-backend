package com.crn.lgdms.common.exception;

public class NotFoundException extends ApiException {
    public NotFoundException(String message) {
        super(message, "NOT_FOUND");
    }

    public NotFoundException(String entity, Object id) {
        super(String.format("%s not found with id: %s", entity, id), "NOT_FOUND");
    }
}
