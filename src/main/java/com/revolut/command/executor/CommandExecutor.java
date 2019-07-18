package com.revolut.command.executor;

import com.revolut.command.Command;
import com.revolut.lock.LockService;

/**
 * Base executor class. Provides a trivial sequence of executing a command
 */
public class CommandExecutor {
    /**
     * Lock service
     */
    private LockService lockService;

    /**
     * @param lockService lock service
     */
    public CommandExecutor(LockService lockService) {
        this.lockService = lockService;
    }

    /**
     * Tries to execute a command. If fails on any stage - calls onFailure() and rethrows the exception
     *
     * @param command command to execute
     *
     * @throws Exception any exception during any stage of execution
     */
    public void execute(Command command) throws Exception {
        try {
            command.pre(lockService);
            command.execute();
            command.onSuccess();
        } catch (Exception exception) {
            command.onFailure(exception);

            throw exception;
        } finally {
            command.release();
        }
    }
}
