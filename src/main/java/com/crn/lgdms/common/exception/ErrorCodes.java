package com.crn.lgdms.common.exception;

public final class ErrorCodes {
    public static final String NOT_FOUND = "ERR_001";
    public static final String VALIDATION_FAILED = "ERR_002";
    public static final String UNAUTHORIZED = "ERR_003";
    public static final String FORBIDDEN = "ERR_004";
    public static final String CONFLICT = "ERR_005";
    public static final String INTERNAL_ERROR = "ERR_006";
    public static final String DUPLICATE_ENTITY = "ERR_007";
    public static final String INSUFFICIENT_STOCK = "ERR_008";
    public static final String CREDIT_LIMIT_EXCEEDED = "ERR_009";
    public static final String EMPTY_REQUIRED = "ERR_010";
    public static final String NEGATIVE_STOCK = "ERR_011";

    private ErrorCodes() {}
}
