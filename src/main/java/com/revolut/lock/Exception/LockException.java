package com.revolut.lock.Exception;

import com.revolut.exception.RevolutException;

/**
 * Base lock exception
 */
public abstract class LockException extends RevolutException {
    /**
     * @param message exception message
     */
    public LockException(String message) {
        super(message);
    }
}
