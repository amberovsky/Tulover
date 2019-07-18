package com.revolut.ledger;

import com.revolut.ledger.Transaction.Exception.TransactionException;
import com.revolut.ledger.Transaction.Transaction;

import java.util.UUID;

/**
 * Ledger service to manage ledgers
 */
public interface LedgerService {
    /**
     * @param accountId account id
     *
     * @return a provisioned ledger (with ledger entries) for the given accountId, null if such does not exist
     *
     * @throws TransactionException
     */
    Ledger getByAccountId(UUID accountId) throws TransactionException;

    /**
     * @param fromLedgerId ledger id of the source
     * @param toLedgerId ledger id of the target
     * @param ownerLedgerId owner ledger id - either fromLedgerId or toLedgerId
     * @param globalId id of the transaction. It is the same across the relevant ledger entries
     * @param amount transaction amount
     * @param type type of the transaction. @see LedgerEntry.Type
     * @param subtype subtype of the ledger entry. @see LedgerEntry.Subtype
     * @param createdBy who requested this transaction
     *
     * @return initial transaction
     */
    LedgerEntry initiateTransaction(
        UUID fromLedgerId,
        UUID toLedgerId,
        UUID ownerLedgerId,
        UUID globalId,
        long amount,
        LedgerEntry.Type type,
        LedgerEntry.Subtype subtype,
        UUID createdBy
    );

    /**
     * Complete transaction
     *
     * @param transaction original transaction
     *
     * @return the second stage of a successful transaction
     */
    LedgerEntry completeTransaction(Transaction transaction);

    /**
     * Cancel transaction
     *
     * @param transaction original transaction
     *
     * @return cancellation ledger entry
     */
    LedgerEntry cancelTransaction(Transaction transaction);
}
