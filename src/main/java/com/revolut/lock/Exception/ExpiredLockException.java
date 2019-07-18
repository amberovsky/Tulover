package com.revolut.lock.Exception;

import java.util.UUID;

/**
 * When trying to unlock expired lock
 */
public class ExpiredLockException extends LockException {
    /**
     *
     * @param id lock id
     */
    public ExpiredLockException(UUID id) {
        super("trying to unlock already expired lock " + id);
    }
}
