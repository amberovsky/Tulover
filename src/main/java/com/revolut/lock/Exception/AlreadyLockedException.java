package com.revolut.lock.Exception;

import java.util.UUID;

/**
 * When a lock is already locked
 */
public class AlreadyLockedException extends LockException {
    /**
     * @param id lock id
     */
    public AlreadyLockedException(UUID id) {
        super("Lock " + id + " is already locked");
    }
}
