package com.revolut.ledger;

import com.revolut.ledger.Transaction.CreditTransaction;
import com.revolut.ledger.Transaction.DebitTransaction;
import com.revolut.ledger.Transaction.Exception.TransactionException;
import com.revolut.ledger.Transaction.Exception.UnknownSubtypeException;
import com.revolut.ledger.Transaction.Transaction;

import java.util.List;
import java.util.UUID;

/**
 * The Ledger
 */
public class Ledger {
    /**
     * Possible types of a ledger
     */
    public enum Type {
        INTERNAL // Holds internal accounts' balances
    }

    /**
     * Ledger service.
     * Ledger object will apply operations in the Ledger immediately
     */
    private LedgerService ledgerService;

    /**
     * Unique id
     */
    private UUID id;

    /**
     * Ledger owner
     */
    private UUID accountId;

    /**
     * Type
     */
    private Type type;

    /**
     * Information about the ledger balance
     */
    private Balance balance;

    /**
     * @param ledgerService ledger service. Ledger object will apply operations in the Ledger immediately
     * @param id unique id
     * @param accountId ledger owner
     * @param type type
     * @param balance information about the ledger balance
     */
    public Ledger(LedgerService ledgerService, UUID id, UUID accountId, Type type, Balance balance) {
        this.ledgerService = ledgerService;
        this.id = id;
        this.accountId = accountId;
        this.type = type;
        this.balance = balance;
    }

    /**
     * @return unique id
     */
    public UUID getId() {
        return id;
    }

    /**
     * Recalculates ledger balance, for example, when just loading ledger entries
     *
     * @param ledgerEntries ledger entries for this ledger
     *
     * @throws UnknownSubtypeException
     */
    public void updateBalance(List<LedgerEntry> ledgerEntries) throws UnknownSubtypeException {
        balance.actual = balance.obligating = balance.receiving = 0;

        // go thru all ledger entries and update the balance accordingly
        for (LedgerEntry ledgerEntry : ledgerEntries) {
            switch (ledgerEntry.getType()) {
                case DEBIT:
                    balance.updateDebit(ledgerEntry.getSubtype(), ledgerEntry.getAmount());
                    break;

                case CREDIT:
                    balance.updateCredit(ledgerEntry.getSubtype(), ledgerEntry.getAmount());
                    break;
            }
        }
    }

    /**
     * @return actual amount of available funds
     */
    public long getActualBalance() {
        return balance.getActual();
    }

    /**
     * Creates new debit transaction (DEBIT ledger entry with the OBLIGATING subtype)
     *
     * @param createdBy who requested this transaction
     * @param to ledger of the target
     * @param amount transaction amount
     *
     * @return new debit transaction
     */
    public DebitTransaction createDebitTransaction(UUID createdBy, Ledger to, long amount) throws UnknownSubtypeException {
        DebitTransaction debitTransaction = new DebitTransaction(
            ledgerService.initiateTransaction(
                    id, to.id, id, UUID.randomUUID(), amount, LedgerEntry.Type.DEBIT, LedgerEntry.Subtype.OBLIGATION, createdBy
            )
        );

        balance.updateDebit(LedgerEntry.Subtype.OBLIGATION, amount);
        return debitTransaction;
    }

    /**
     * Creates new credit transaction (CREDIT ledger entry with the RECEIVING subtype)
     *
     * @param createdBy who requested this transaction
     * @param from ledger of the source
     * @param amount transaction amount
     * @param globalId id of the transaction. It is the same across the relevant ledger entries
     *
     * @return new credit transaction
     */
    public CreditTransaction createCreditTransaction(UUID createdBy, Ledger from, long amount, UUID globalId) throws UnknownSubtypeException {
        CreditTransaction creditTransaction = new CreditTransaction(
            ledgerService.initiateTransaction(
                from.getId(), id, id, globalId, amount, LedgerEntry.Type.CREDIT, LedgerEntry.Subtype.RECEIVING, createdBy
            )
        );

        balance.updateCredit(LedgerEntry.Subtype.RECEIVING, amount);
        return creditTransaction;
    }

    /**
     * Finishes a successful transaction (creates a ledger entry with the ACTUAL subtype)
     *
     * @param transaction transaction
     */
    public void completeTransaction(Transaction transaction) throws TransactionException {
        LedgerEntry ledgerEntry = ledgerService.completeTransaction(transaction);
        transaction.updateBalance(balance, ledgerEntry.getSubtype(), ledgerEntry.getAmount());
    }

    /**
     * Cancels transaction
     *
     * @param transaction transaction to cancel
     */
    public void cancelTransaction(Transaction transaction) throws TransactionException {
        LedgerEntry ledgerEntry = ledgerService.cancelTransaction(transaction);
        transaction.updateBalance(balance, ledgerEntry.getSubtype(), ledgerEntry.getAmount());
    }
}
