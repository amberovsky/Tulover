package com.revolut.ledger.Transaction;

import com.revolut.ledger.Balance;
import com.revolut.ledger.LedgerEntry;
import com.revolut.ledger.Transaction.Exception.TransactionException;

import java.util.UUID;

/**
 * Represents a possible transaction in the ledger. @see LedgerEntity.Type for list of possible types.
 *
 * Current logic supports only two-staged transactions, that means each transaction consists of 2 entries - first
 * is when we initiate the transaction and the second is when we confirm it. It is pretty similar to the 2 phase commit
 */
public abstract class Transaction {
    /**
     * Ledger entry for the initial part
     */
    protected LedgerEntry ledgerEntry;

    /**
     * @param ledgerEntry initial ledger entry
     */
    public Transaction(LedgerEntry ledgerEntry) {
        this.ledgerEntry = ledgerEntry;
    }

    /**
     * @return id of the transaction. It is the same across the relevant ledger entries
     */
    public UUID getId() {
        return ledgerEntry.getGlobalId();
    }

    /**
     * @return ledger id of the source
     */
    public UUID getFromLedgerId() {
        return ledgerEntry.getFromLedgerId();
    }

    /**
     * @return ledger id of the target
     */
    public UUID getToLedgerId() {
        return ledgerEntry.getToLedgerId();
    }

    /**
     * @return transaction amount
     */
    public long getAmount() {
        return ledgerEntry.getAmount();
    }

    /**
     * @return who requested this transaction
     */
    public UUID getCreatedBy() {
        return ledgerEntry.getCreatedBy();
    }

    /**
     * @return type of the transaction. @see LedgerEntry.Type
     */
    public abstract LedgerEntry.Type getType();

    /**
     * @return owner ledger id - either fromLedgerId or toLedgerId
     */
    public abstract UUID getOwnerLedgerId();

    /**
     * Update the balance accordingly to the Type and Subtype
     *
     * @param balance current ledger's balance
     * @param subtype ledger entry subtype
     * @param amount ledger entry amount
     */
    public abstract void updateBalance(Balance balance, LedgerEntry.Subtype subtype, long amount) throws TransactionException;
}
