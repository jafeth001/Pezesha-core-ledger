package com.fintech.pezesha_core_ledger.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class LedgerException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;

    public LedgerException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }
}



