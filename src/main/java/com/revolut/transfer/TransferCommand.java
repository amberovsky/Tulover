package com.revolut.transfer;

import com.revolut.command.Command;
import com.revolut.exception.RevolutException;
import com.revolut.ledger.Transaction.CreditTransaction;
import com.revolut.ledger.Transaction.DebitTransaction;
import com.revolut.lock.Exception.*;
import com.revolut.lock.Lock;
import com.revolut.lock.LockService;
import com.revolut.ledger.Ledger;
import com.revolut.ledger.LedgerService;
import com.revolut.transfer.Exception.IllegalTransferException;
import com.revolut.transfer.Exception.InsufficientFundsException;
import com.revolut.transfer.Exception.NoSuchLedgerException;
import org.slf4j.Logger;

import java.util.UUID;

/**
 * THE transfer command
 */
public class TransferCommand implements Command {
    /**
     * For how long ro acquire lock, milliseconds
     */
    private final static int acquiringTime = 2000;

    /**
     * Ledger service
     */
    private LedgerService ledgerService;

    /**
     * Account id to transfer from
     */
    private UUID fromId;

    /**
     * Account id to transfer to
     */
    private UUID toId;

    /**
     * How much to transfer
     */
    private long amount;

    /**
     * Logger
     */
    private Logger logger;

    /**
     * Ledger of the "from" account
     */
    private Ledger ledgerFrom;

    /**
     * Ledger of the "to" account
     */
    private Ledger ledgerTo;

    /**
     * Lock of the "from" account
     */
    private Lock fromLock;

    /**
     * Debit transaction
     */
    private DebitTransaction debitTransaction;

    /**
     * Credit transaction
     */
    private CreditTransaction creditTransaction;

    /**
     * Waiting timeout when acquiring lock
     */
    private int lockWaitingTimeout;

    /**
     * @param ledgerService ledger service
     * @param fromId account id to transfer from
     * @param toId account id to transfer to
     * @param amount how much to transfer
     * @param logger logger
     * @param lockWaitingTimeout waiting timeout when acquiring lock
     */
    public TransferCommand(LedgerService ledgerService, UUID fromId, UUID toId, long amount, Logger logger, int lockWaitingTimeout) {
        this.ledgerService = ledgerService;
        this.fromId = fromId;
        this.toId = toId;
        this.amount = amount;
        this.logger = logger;
        this.lockWaitingTimeout = lockWaitingTimeout;
    }

    @Override
    public void pre(LockService lockService) throws RevolutException {
        // check that we are not trying to send money to the same account
        if (fromId.equals(toId)) throw new IllegalTransferException();

        // acquiring resources
        fromLock = lockService.create(fromId);
        if (fromLock.tryLock(lockWaitingTimeout, acquiringTime)) {

            // loading ledgers
            ledgerFrom = ledgerService.getByAccountId(fromId);
            if (ledgerFrom == null) throw new NoSuchLedgerException(fromId);
            ledgerTo = ledgerService.getByAccountId(toId);
            if (ledgerTo == null) throw new NoSuchLedgerException(toId);

            // checking for the balance
            if (ledgerFrom.getActualBalance() < amount) throw new InsufficientFundsException(fromId);
        } else {
            throw new UnableToAcquireException("Failed to wait " + lockWaitingTimeout + " ms to acquire " + fromLock.getId());
        }
    }

    @Override
    public void execute() throws Exception {
        // Create 2 "mirroring" transactions - DEBIT for sender and CREDIT for receiver
        debitTransaction = ledgerFrom.createDebitTransaction(fromId, ledgerTo, amount);
        creditTransaction = ledgerTo.createCreditTransaction(fromId, ledgerFrom, amount, debitTransaction.getId());

        ledgerTo.completeTransaction(creditTransaction);
        ledgerFrom.completeTransaction(debitTransaction);

        fromLock.tryUnlock(false);
        fromLock = null;
    }

    @Override
    public void onSuccess() {
        logger.info("Transferred " + amount + " from " + fromId + " to " + toId);
    }

    @Override
    public void onFailure(Exception exception) throws Exception {
        // Trying to cancel transactions in ase of failure
        if (creditTransaction != null) ledgerTo.cancelTransaction(creditTransaction);
        if (debitTransaction != null) ledgerFrom.cancelTransaction(debitTransaction);

        if (fromLock != null) fromLock.tryUnlock(true);

        logger.error("Rolling back transfer", exception);
    }

    @Override
    public void release() throws RevolutException {
        try {
            // release() is called in both happy/failed paths so we need to be careful here
            if (fromLock != null) fromLock.tryUnlock(true);
        } catch (AlreadyUnlockedException | ExpiredLockException | LostLockException exception) {
            throw new RevolutException(
                    "Issues with lock " + fromLock.getId() + " - transactions are possible in the unbalanced state",
                    exception
            );
        } catch (LockException ignore) {}
    }
}
