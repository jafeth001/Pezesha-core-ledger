package com.fintech.pezesha_core_ledger.exception;

import org.springframework.http.HttpStatus;

public class AccountingException extends LedgerException {
    public AccountingException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY, "ACCOUNTING_ERROR");
    }
}