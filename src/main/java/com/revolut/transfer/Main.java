package com.revolut.transfer;

import com.revolut.command.executor.CommandExecutor;
import com.revolut.router.Router;
import org.apache.logging.log4j.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.time.Clock;
import java.util.UUID;

/**
 * Entrypoint
 */
public class Main {
    public static void main(String[] args)  {
        final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getSimpleName());
        ThreadContext.put(com.revolut.logger.Logger.FIELD_TRACE_ID, UUID.randomUUID().toString());
        logger.info("Loading transfer service...");

        TransferService transferService = new TransferService(
            new LocalLedgerService(Clock.systemUTC()),
            new CommandExecutor(new LocalLockService(Clock.systemUTC())),
            new LocalAccountService(),
            logger,
            500
        );

        Router router = new Router(logger);
        router.handlePOST("/transfer", transferService::transferAction);

        logger.info("Transfer service has been loaded");
    }
}
