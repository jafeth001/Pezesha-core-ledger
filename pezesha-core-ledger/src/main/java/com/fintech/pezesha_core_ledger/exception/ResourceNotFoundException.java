package com.fintech.pezesha_core_ledger.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends LedgerException {
    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }
}
