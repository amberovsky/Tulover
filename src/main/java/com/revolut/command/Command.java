package com.revolut.command;

import com.revolut.exception.RevolutException;
import com.revolut.lock.LockService;

/**
 * Financial command to execute
 */
public interface Command {
    /**
     * Initial checks, locks, etc.
     * Idea is that we pass here all the required parameters for all commands, like the lock service.
     *
     * @param lockService lock service
     *
     * @throws Exception we don't catch any exception - the onFailure() method will be
     *                   called for that. @see CommandExecutor
     */
    void pre(LockService lockService) throws Exception;

    /**
     * Execute the command. It is a proper place to release acquired resources in the pre() method
     */
    void execute() throws Exception;

    /**
     * Called upon the successful execution
     */
    void onSuccess();

    /**
     * Called in case of any exception being thrown during execute()
     *
     * @param exception exception thrown by the execute()
     */
    void onFailure(Exception exception) throws Exception;

    /**
     * Always called to release acquired resources
     */
    void release() throws RevolutException;
}
