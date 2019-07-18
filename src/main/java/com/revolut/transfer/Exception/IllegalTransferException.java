package com.revolut.transfer.Exception;

/**
 * When transfer is not allowed between given accounts
 * For example, when accounts are the same
 */
public class IllegalTransferException extends TransferException {
    public IllegalTransferException() {
        super("Transfer between given accounts is prohibited");
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
