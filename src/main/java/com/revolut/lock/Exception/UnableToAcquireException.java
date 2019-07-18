package com.revolut.lock.Exception;

/**
 * When fail to acquire lock
 */
public class UnableToAcquireException extends LockException {
    /**
     * @param message exception message
     */
    public UnableToAcquireException(String message) {
        super(message);
    }
}
