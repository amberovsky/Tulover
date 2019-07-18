package com.revolut.lock;

import com.revolut.lock.Exception.LockException;

import java.util.UUID;

/**
 * Represents a distributed lock.
 * It is not implementing java Lock interface because semantic of distributed locks is a bit different. IMHO
 */
public class Lock {
    /**
     * Lock service
     */
    private LockService lockService;

    /**
     * id
     */
    private UUID id;

    /**
     * lock owner
     */
    private UUID ownerId;

    /**
     * Locked flag
     */
    private boolean isLocked;

    /**
     * @param lockService lock service
     * @param id id
     */
    public Lock(LockService lockService, UUID id) {
        this.lockService = lockService;
        this.id = id;
        this.ownerId = UUID.randomUUID();
    }

    /**
     * @return id
     */
    public UUID getId() {
        return id;
    }

    /**
     * @return lock owner
     */
    public UUID getOwnerId() {
        return ownerId;
    }

    /**
     * @return locked flag
     */
    public boolean getIsLocked() {
        return isLocked;
    }

    /**
     * @param isLocked set locked flag
     */
    public void setIsLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }

    /**
     * Try to acquire the lock
     *
     * @param waitingTime maximum time to wait, if lock is already acquired, milliseconds
     * @param acquiringTime for how long to acquire lock, milliseconds
     *
     * @return true if managed to acquire lock within the given waiting time, false otherwise
     *
     * @throws LockException
     */
    public boolean tryLock(long waitingTime, long acquiringTime) throws LockException {
        return lockService.tryLock(this, waitingTime, acquiringTime);
    }

    /**
     * Try to unlock the lock
     *
     * @param allowLost in some cases it is fine when we are trying to unlock a lock which already belongs
     *                  to someone else or expired
     *
     * @throws LockException
     */
    public void tryUnlock(boolean allowLost) throws LockException {
        lockService.tryUnlock(this, allowLost);
    }
}
