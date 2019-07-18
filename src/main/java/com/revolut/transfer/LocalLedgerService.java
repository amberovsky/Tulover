package com.revolut.transfer;

import com.revolut.ledger.Balance;
import com.revolut.ledger.Ledger;
import com.revolut.ledger.LedgerEntry;
import com.revolut.ledger.LedgerService;
import com.revolut.ledger.Transaction.Exception.TransactionException;
import com.revolut.ledger.Transaction.Transaction;

import java.time.Clock;
import java.util.*;

/**
 * Ledger service with local static storage and a few predefined ledgers with entries
 */
public class LocalLedgerService implements LedgerService {
    /**
     * Ledgers storage
     */
    private static HashMap<UUID, Ledger> ledgers = new HashMap<>();

    /**
     * Ledgers entries storage
     */
    private static HashMap<UUID, List<LedgerEntry>> ledgersEntries = new HashMap<>();

    /**
     * "Owner/Author" of the predefined ledger entries
     */
    private static UUID GodUUID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    /**
     * Helper method to create a predefined ledger with some predefined entries
     *
     * @param ledger ledger UUID to create
     * @param account account UUID of the ledger
     * @param amount balance of the ledger
     */
    private void createLedgerAndDebitTransaction(String ledger, String account, int amount) {
        UUID ledgerId = UUID.fromString(ledger);
        UUID accountId = UUID.fromString(account);
        ledgers.put(
            accountId,
            new Ledger(this, ledgerId, accountId, Ledger.Type.INTERNAL, new Balance(amount, 0, 0))
        );

        UUID globalId = UUID.randomUUID();
        ledgersEntries.put(ledgerId, new ArrayList<>(Arrays.asList(
            new LedgerEntry(UUID.randomUUID(), globalId, GodUUID, ledgerId, amount, LedgerEntry.Type.CREDIT, LedgerEntry.Subtype.RECEIVING, clock.millis(), GodUUID),
            new LedgerEntry(UUID.randomUUID(), globalId, GodUUID, ledgerId, amount, LedgerEntry.Type.CREDIT, LedgerEntry.Subtype.ACTUAL, clock.millis(), GodUUID)
        )));
    }

    private Clock clock;

    /**
     * Create ledger service
     */
    public LocalLedgerService(Clock clock) {
        this.clock = clock;

        // Create some predefined ledgers
        createLedgerAndDebitTransaction("326608e5-5fbf-4505-871d-d0ec830e1994", "326608e5-5fbf-4505-871d-d0ec830e1994", 1000);
        createLedgerAndDebitTransaction("ef43bea7-8723-4f14-bab1-6b48ef8cb4fb", "5ab59fdf-997f-4a20-ab33-67272b840a19", 500);
        createLedgerAndDebitTransaction("530d0897-36dd-4045-bc1c-89f9dc41c0f2", "d2febbaf-0edb-4f19-824e-588b712c8c29", 50);
    }

    @Override
    public Ledger getByAccountId(UUID accountId) throws TransactionException {
        Ledger ledger = ledgers.getOrDefault(accountId, null);
        if (ledger != null) ledger.updateBalance(ledgersEntries.get(ledger.getId()));

        return ledger;
    }

    @Override
    public LedgerEntry initiateTransaction(
        UUID fromLedgerId,
        UUID toLedgerId,
        UUID ownerLedgerId,
        UUID globalId,
        long amount,
        LedgerEntry.Type type,
        LedgerEntry.Subtype subtype,
        UUID createdBy
    ) {
        LedgerEntry ledgerEntry = new LedgerEntry(
            UUID.randomUUID(), globalId, fromLedgerId, toLedgerId, amount, type, subtype, clock.millis(), createdBy
        );

        ledgersEntries.get(ownerLedgerId).add(ledgerEntry);

        return ledgerEntry;
    }

    @Override
    public LedgerEntry completeTransaction(Transaction transaction) {
        LedgerEntry ledgerEntry = new LedgerEntry(
            UUID.randomUUID(),
            transaction.getId(),
            transaction.getFromLedgerId(),
            transaction.getToLedgerId(),
            transaction.getAmount(),
            transaction.getType(),
            LedgerEntry.Subtype.ACTUAL,
            clock.millis(),
            transaction.getCreatedBy()
        );

        ledgersEntries
            .get(transaction.getOwnerLedgerId())
            .add(ledgerEntry);

        return ledgerEntry;
    }

    @Override
    public LedgerEntry cancelTransaction(Transaction transaction) {
        LedgerEntry ledgerEntry = new LedgerEntry(
                UUID.randomUUID(),
                transaction.getId(),
                transaction.getFromLedgerId(),
                transaction.getToLedgerId(),
                transaction.getAmount(),
                transaction.getType(),
                LedgerEntry.Subtype.CANCEL,
                clock.millis(),
                transaction.getCreatedBy()
        );

        ledgersEntries
                .get(transaction.getOwnerLedgerId())
                .add(ledgerEntry);

        return ledgerEntry;
    }
}
