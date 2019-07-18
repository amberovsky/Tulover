package com.revolut.ledger;

import com.revolut.ledger.Transaction.Exception.UnknownSubtypeException;

/**
 * Balance information
 */
public class Balance {
    /**
     * Actual amount of available funds
     */
    long actual;

    /**
     * Amount obliged to be sent (hold)
     */
    long obligating;

    /**
     * Amount to receive
     */
    long receiving;

    /**
     * @param actual actual amount of available funds
     * @param obligating amount obliged to be sent (hold)
     * @param receiving amount to receive
     */
    public Balance(long actual, long obligating, long receiving) {
        this.actual = actual;
        this.obligating = obligating;
        this.receiving = receiving;
    }

    /**
     * Update balance on DEBIT ledger entry
     *
     * @param subtype subtype of the DEBIT ledger entry. @see LedgerEntry.Subtype
     * @param amount amount of the ledger entry
     *
     * @throws UnknownSubtypeException when subtype is unknown
     */
    public void updateDebit(LedgerEntry.Subtype subtype, long amount) throws UnknownSubtypeException {
        switch (subtype) {
            case OBLIGATION:
                actual -= amount;
                obligating += amount;
                break;

            case ACTUAL:
                obligating -= amount;
                break;

            case CANCEL:
                obligating -= amount;
                actual += amount;
                break;

            default:
                throw new UnknownSubtypeException(LedgerEntry.Type.DEBIT, subtype);
        }
    }

    /**
     * Update balance on CREDIT ledger entry
     *
     * @param subtype subtype of the CREDIT ledger entry. @see LedgerEntry.Subtype
     * @param amount amount of the ledger entry
     *
     * @throws UnknownSubtypeException when subtype is unknown
     */
    public void updateCredit(LedgerEntry.Subtype subtype, long amount) throws UnknownSubtypeException {
        switch (subtype) {
            case RECEIVING:
                receiving += amount;
                break;

            case ACTUAL:
                actual += amount;
                receiving -= amount;
                break;

            case CANCEL:
                receiving -= amount;
                break;

            default:
                throw new UnknownSubtypeException(LedgerEntry.Type.CREDIT, subtype);
        }
    }

    /**
     * @return actual amount of available funds
     */
    public long getActual() {
        return actual;
    }

    /**
     * @return amount obliged to be sent (hold)
     */
    public long getObligating() {
        return obligating;
    }

    /**
     * @return amount to receive
     */
    public long getReceiving() {
        return receiving;
    }
}
