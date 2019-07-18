package com.revolut.transfer.Exception;

import com.revolut.exception.RevolutException;

/**
 * Base exception for transfer service
 */
public abstract class TransferException extends RevolutException {
    /**
     * @param message exception message
     */
    public TransferException(String message) {
        super(message);
    }
}
