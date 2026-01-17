package com.fintech.pezesha_core_ledger.exception;


import org.springframework.http.HttpStatus;

public class ConcurrencyException extends LedgerException {
    public ConcurrencyException(String message) {
        super(message, HttpStatus.CONFLICT, "CONCURRENCY_ERROR");
    }
}
