package com.revolut.ledger.Exception;

import com.revolut.exception.RevolutException;

/**
 * Base exception for ledger service
 */
public abstract class LedgerException extends RevolutException {
    /**
     * @param message exception message
     */
    public LedgerException(String message) {
        super(message);
    }
}
