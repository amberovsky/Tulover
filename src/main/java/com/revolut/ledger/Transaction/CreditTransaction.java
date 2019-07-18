package com.revolut.ledger.Transaction;

import com.revolut.ledger.Balance;
import com.revolut.ledger.LedgerEntry;
import com.revolut.ledger.Transaction.Exception.UnknownSubtypeException;

import java.util.UUID;

/**
 * Represents a credit transaction
 */
public class CreditTransaction extends Transaction {
    /**
     * @param ledgerEntry initial ledger entry
     */
    public CreditTransaction(LedgerEntry ledgerEntry) {
        super(ledgerEntry);
    }

    @Override
    public LedgerEntry.Type getType() {
        return LedgerEntry.Type.CREDIT;
    }

    @Override
    public UUID getOwnerLedgerId() {
        return ledgerEntry.getToLedgerId();
    }

    @Override
    public void updateBalance(Balance balance, LedgerEntry.Subtype subtype, long amount) throws UnknownSubtypeException {
        balance.updateCredit(subtype, amount);
    }
}
