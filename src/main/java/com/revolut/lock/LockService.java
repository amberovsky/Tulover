package com.revolut.lock;

import com.revolut.lock.Exception.LockException;

import java.util.UUID;

/**
 * Lock service to manage locks
 */
public interface LockService {
    /**
     * @param id lock id
     *
     * @return new lock
     */
    Lock create(UUID id);

    /**
     * Try to acquire a lock
     *
     * @param lock lock to acquire
     * @param waitingTime maximum time to wait, if lock is already acquired, milliseconds
     * @param acquiringTime for how long to acquire lock, milliseconds
     *
     * @return true if managed to acquire lock within the given waiting time, false otherwise
     *
     * @throws LockException
     */
    boolean tryLock(Lock lock, long waitingTime, long acquiringTime) throws LockException;

    /**
     * Try to unlock a lock.
     *
     * @param lock lock to unlock
     * @param allowLost in some cases it is fine when we are trying to unlock a lock which already belongs
     *                  to someone else or expired
     *
     * @throws LockException
     */
    void tryUnlock(Lock lock, boolean allowLost) throws LockException;
}
