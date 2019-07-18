package com.revolut.transfer;

import com.revolut.command.executor.CommandExecutor;
import org.junit.jupiter.api.Test;
import org.slf4j.helpers.NOPLogger;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransferCommandTest {
    /**
     * TransferCommand with random delays during execution
     */
    private static class TransferCommandWithRandomDelay extends TransferCommand {
        TransferCommandWithRandomDelay(LocalLedgerService localLedgerService, UUID from, UUID to) {
            super(localLedgerService, from, to, 1, NOPLogger.NOP_LOGGER, 5000);
        }

        @Override
        public void execute() throws Exception {
            Thread.sleep(ThreadLocalRandom.current().nextInt(0, 20));
            super.execute();
        }
    }

    /**
     * Run 50 concurrent transfer commands with random sleep() per each thread.
     *
     * Check there were no exceptions
     * Check the funds have been moved between accounts (by checking balances)
     */
    @Test
    public void testConcurrency() throws Exception {
        int threads = 50;
        List<Callable<Void>> tasks = new ArrayList<>(threads);
        AtomicInteger noErrors = new AtomicInteger(0);
        AtomicInteger errors = new AtomicInteger(0);

        CommandExecutor commandExecutor = new CommandExecutor(new LocalLockService(Clock.systemUTC()));

        LocalLedgerService localLedgerService = new LocalLedgerService(Clock.systemUTC());
        UUID from = UUID.fromString("d2febbaf-0edb-4f19-824e-588b712c8c29");
        UUID to = UUID.fromString("5ab59fdf-997f-4a20-ab33-67272b840a19");
        long currentBalance = localLedgerService.getByAccountId(to).getActualBalance();

        for (int i = 0; i < threads; i++) {
            // Account d2febbaf-0edb-4f19-824e-588b712c8c29 has 50 funds

            tasks.add(() -> {
                try {
                    commandExecutor.execute(new TransferCommandWithRandomDelay(localLedgerService, from, to));
                    noErrors.incrementAndGet();
                } catch (Exception exception) {
                    errors.incrementAndGet();
                }

                return null;
            });
        }

        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        executorService.invokeAll(tasks);
        executorService.shutdown();

        assertEquals(threads, noErrors.get());
        assertEquals(0, errors.get());

        // Checking balances
        assertEquals(0, localLedgerService.getByAccountId(from).getActualBalance());
        assertEquals(currentBalance + threads, localLedgerService.getByAccountId(to).getActualBalance());
    }
}
