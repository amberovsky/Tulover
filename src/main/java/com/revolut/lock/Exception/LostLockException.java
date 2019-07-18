package com.revolut.lock.Exception;

import java.util.UUID;

/**
 * When doing operations on a lock which belongs to someone else
 */
public class LostLockException extends LockException {
    /**
     *
     * @param id lock id
     * @param expected expected owner of the lock
     * @param actual actual owner of the lock
     */
    public LostLockException(UUID id, UUID expected, UUID actual) {
        super("lock " + id + " supposed to belong to " + expected + " but belongs to " + actual);
    }
}
