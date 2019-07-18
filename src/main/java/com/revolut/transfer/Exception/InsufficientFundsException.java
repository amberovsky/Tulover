package com.revolut.transfer.Exception;

import java.util.UUID;

/**
 * When account's balance is below requested amount to transfer
 */
public class InsufficientFundsException extends TransferException {
    public InsufficientFundsException(UUID accountId) {
        super("Account " + accountId.toString() + " does not have enough balance");
    }

    @Override
    public boolean isDisplayable() {
        return true;
    }

    @Override
    public String toDisplayable() {
        return getMessage();
    }
}
