package com.revolut.lock.Exception;

import java.util.UUID;

/**
 * When doing operations on a lock which does not exist
 */
public class LockDoesNotExistException extends LockException {
    /**
     * @param id lock id
     */
    public LockDoesNotExistException(UUID id) {
        super("lock " + id + " does not exist");
    }
}
