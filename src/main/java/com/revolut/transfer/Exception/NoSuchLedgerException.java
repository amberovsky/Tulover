package com.revolut.transfer.Exception;

import java.util.UUID;

/**
 * When failed to find a ledger by given id
 */
public class NoSuchLedgerException extends TransferException {
    public NoSuchLedgerException(UUID ledgerId) {
        super("Ledger " + ledgerId.toString() + " does not exist");
    }
}
