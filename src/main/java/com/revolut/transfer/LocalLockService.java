package com.revolut.transfer;

import com.revolut.lock.Exception.*;
import com.revolut.lock.Lock;
import com.revolut.lock.LockService;

import java.time.Clock;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Lock service to manage locks
 */
public class LocalLockService implements LockService {
    /**
     * Holds the lock state
     */
    private final class LockStatus {
        /**
         * Timestamp when was locked, milliseconds
         */
        long lockedAt;

        /**
         * Timestamp before what should be unlocked, milliseconds
         */
        long unlockAt;

        /**
         * Lock owner
         */
        UUID ownerId;

        /**
         * @param lockedAt timestamp when was locked, milliseconds
         * @param unlockAt timestamp before what should be unlocked, milliseconds
         * @param ownerId lock owner
         */
        LockStatus(long lockedAt, long unlockAt, UUID ownerId) {
            reset(lockedAt, unlockAt, ownerId);
        }
        /**
         *
         * @param lockedAt timestamp when was locked, milliseconds
         * @param unlockAt timestamp before what should be unlocked, milliseconds
         * @param ownerId lock owner
         */
        void reset(long lockedAt, long unlockAt, UUID ownerId) {
            this.lockedAt = lockedAt;
            this.unlockAt = unlockAt;
            this.ownerId = ownerId;
        }

        /**
         * Reset to the initial state
         */
        void reset() {
            reset(0, 0, null);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if ((null == obj) || (obj.getClass() != this.getClass())) return false;

            LockStatus other = (LockStatus) obj;

            return (
                (this.lockedAt == other.lockedAt) &&
                (this.unlockAt == other.unlockAt) &&
                (this.ownerId == other.ownerId)
            );
        }

        @Override
        public int hashCode() {
            return ownerId.hashCode();
        }
    }

    /**
     * Lock storage
     */
    private static HashMap<UUID, LockStatus> locks = new HashMap<>();

    /**
     * Clock
     */
    private Clock clock;

    /**
     * Reentrant lock for the locks hashtable
     */
    private ReentrantLock locksLock = new ReentrantLock();

    public LocalLockService(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Lock create(UUID id) {
        return new Lock(this, id);
    }

    /**
     * @inheritDoc
     *
     * A simple implementation of a spinlock
     */
    @Override
    public boolean tryLock(Lock lock, long waitingTime, long acquiringTime) throws LockException {
        long started = clock.millis();

        // Check if it is not already locked
        if (lock.getIsLocked()) throw new AlreadyLockedException(lock.getId());

        LockStatus lockStatus;

        try {
            long now;

            while (true) {
                now = clock.millis();
                if (now <= started + waitingTime) {
                    if (!locksLock.tryLock(waitingTime - (now - started), TimeUnit.MILLISECONDS)) {
                        return false;
                    }

                    lockStatus = locks.getOrDefault(lock.getId(), null);
                    now = clock.millis();

                    // need to double-check if we still have time
                    if (now <= started + waitingTime) {
                        if (lockStatus == null) {
                            // no such lock
                            locks.put(lock.getId(), new LockStatus(now, now + acquiringTime, lock.getOwnerId()));
                            lock.setIsLocked(true);
                            locksLock.unlock();
                            return true;
                        } else if (now >= lockStatus.unlockAt) {
                            // expired lock
                            lockStatus.reset(now, now + acquiringTime, lock.getOwnerId());
                            lock.setIsLocked(true);
                            locksLock.unlock();
                            return true;
                        }

                        // spinning
                        locksLock.unlock();
                        Thread.sleep(10);
                    }
                } else {
                    return false;
                }
            }
        } catch (InterruptedException ignored) {
            return false;
        }
    }

    @Override
    public void tryUnlock(Lock lock, boolean allowLost) throws LockException {
        // Check if it is not already unlocked
        if (!lock.getIsLocked()) {
            if (allowLost) return;
            throw new AlreadyUnlockedException(lock.getId());
        }

        synchronized (this) {
            LockStatus lockStatus = locks.getOrDefault(lock.getId(), null);
            if (lockStatus == null) {
                throw new LockDoesNotExistException(lock.getId());
            }

            if (!lock.getOwnerId().equals(lockStatus.ownerId)) {
                // Lock has been acquired by someone else
                if (allowLost) return;
                throw new LostLockException(lock.getId(), lock.getOwnerId(), lockStatus.ownerId);
            }

            long now = clock.millis();
            if (now > lockStatus.unlockAt) {
                // lock has expired
                if (allowLost) return;
                throw new ExpiredLockException(lock.getId());
            }

            lockStatus.reset();
            lock.setIsLocked(false);
        }
    }
}
