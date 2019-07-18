package com.revolut.ledger.Transaction.Exception;

import com.revolut.ledger.Exception.LedgerException;

/**
 * Base exception for transactions
 */
public class TransactionException extends LedgerException {
    /**
     * @param message exception message
     */
    public TransactionException(String message) {
        super(message);
    }
}
