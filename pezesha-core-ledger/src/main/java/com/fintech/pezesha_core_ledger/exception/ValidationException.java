package com.fintech.pezesha_core_ledger.exception;

import org.springframework.http.HttpStatus;

public class ValidationException extends LedgerException {
    public ValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
    }
}

