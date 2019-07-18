package com.revolut.ledger.Transaction;

import com.revolut.ledger.Balance;
import com.revolut.ledger.LedgerEntry;
import com.revolut.ledger.Transaction.Exception.UnknownSubtypeException;

import java.util.UUID;

/**
 * Represents a debit transaction
 */
public class DebitTransaction extends Transaction {
    /**
     * @param ledgerEntry initial ledger entry
     */
    public DebitTransaction(LedgerEntry ledgerEntry) {
        super(ledgerEntry);
    }

    @Override
    public LedgerEntry.Type getType() {
        return LedgerEntry.Type.DEBIT;
    }

    @Override
    public UUID getOwnerLedgerId() {
        return ledgerEntry.getFromLedgerId();
    }

    @Override
    public void updateBalance(Balance balance, LedgerEntry.Subtype subtype, long amount) throws UnknownSubtypeException {
        balance.updateDebit(subtype, amount);
    }
}
