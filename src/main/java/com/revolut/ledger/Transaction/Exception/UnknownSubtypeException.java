package com.revolut.ledger.Transaction.Exception;

import com.revolut.ledger.LedgerEntry;

/**
 * Unknown subtype for a given typeof transaction
 */
public class UnknownSubtypeException extends TransactionException {
    /**
     *
     * @param type type of the transaction
     * @param subtype unknown subtype
     */
    public UnknownSubtypeException(LedgerEntry.Type type, LedgerEntry.Subtype subtype) {
        super("Unknown subtype " + subtype + " for a " + type + " transaction");
    }
}
