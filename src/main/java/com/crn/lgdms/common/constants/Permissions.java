package com.crn.lgdms.common.constants;

public final class Permissions {
    // User permissions
    public static final String USER_CREATE = "user:create";
    public static final String USER_READ = "user:read";
    public static final String USER_UPDATE = "user:update";
    public static final String USER_DELETE = "user:delete";

    // Inventory permissions
    public static final String INVENTORY_VIEW = "inventory:view";
    public static final String INVENTORY_ADJUST = "inventory:adjust";
    public static final String INVENTORY_TRANSFER = "inventory:transfer";

    // Sales permissions
    public static final String SALE_CREATE = "sale:create";
    public static final String SALE_VIEW = "sale:view";
    public static final String SALE_VOID = "sale:void";

    // Credit permissions
    public static final String CREDIT_VIEW = "credit:view";
    public static final String CREDIT_APPROVE = "credit:approve";
    public static final String CREDIT_LIMIT_SET = "credit:limit:set";

    // Report permissions
    public static final String REPORT_VIEW = "report:view";
    public static final String REPORT_EXPORT = "report:export";

    private Permissions() {}
}
