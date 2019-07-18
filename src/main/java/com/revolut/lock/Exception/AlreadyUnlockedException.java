package com.revolut.lock.Exception;

import java.util.UUID;

/**
 * When a lock is already unlocked
 */
public class AlreadyUnlockedException extends LockException {
    /**
     * @param id lock id
     */
    public AlreadyUnlockedException(UUID id) {
        super("Lock " + id + " is already unlocked");
    }
}
