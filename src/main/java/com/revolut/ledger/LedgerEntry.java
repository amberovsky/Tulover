package com.revolut.ledger;

import java.util.UUID;

/**
 * A ledger entry
 */
public class LedgerEntry {
    /**
     * Possible types of an entry
     */
    public enum Type {
        CREDIT, // Crediting
        DEBIT // Debiting
    }

    /**
     * Possible subtypes of an entry
     */
    public enum Subtype {
        OBLIGATION, // Obliged to send money
        RECEIVING, // Expecting to receive money
        CANCEL, // Cancelled due to some reasons
        ACTUAL // Successfully finished obligation/receiving
    }

    /**
     * Unique id
     */
    private UUID id;

    /**
     * Global transaction id, unique across the relevant entries
     */
    private UUID globalId;

    /**
     * Ledger id of the source
     */
    private UUID fromLedgerId;

    /**
     * ledger id of the target
     */
    private UUID toLedgerId;

    /**
     * Transaction amount
     */
    private long amount;

    /**
     * Type
     */
    private Type type;

    /**
     * Subtype
     */
    private Subtype subtype;

    /**
     * Timestamp of creation, milliseconds
     */
    private long createdAt;

    /**
     * Who requested this transaction
     */
    private UUID createdBy;

    /**
     * @param id unique id
     * @param globalId global transaction id, unique across the relevant entries
     * @param fromLedgerId ledger id of the source
     * @param toLedgerId ledger id of the target
     * @param amount transaction amount
     * @param type type
     * @param subtype subtype
     * @param createdAt timestamp of creation, milliseconds
     * @param createdBy who requested this transaction
     */
    public LedgerEntry(
        UUID id,
        UUID globalId,
        UUID fromLedgerId,
        UUID toLedgerId,
        long amount,
        Type type,
        Subtype subtype,
        long createdAt,
        UUID createdBy
    ) {
        this.id = id;
        this.globalId = globalId;
        this.fromLedgerId = fromLedgerId;
        this.toLedgerId = toLedgerId;
        this.amount = amount;
        this.type = type;
        this.subtype = subtype;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
    }

    /**
     * @return global transaction id, unique across the relevant entries
     */
    public UUID getGlobalId() {
        return globalId;
    }

    /**
     * @return type
     */
    public Type getType() {
        return type;
    }

    /**
     * @return subtype
     */
    public Subtype getSubtype() {
        return subtype;
    }

    /**
     * @return transaction amount
     */
    public long getAmount() {
        return amount;
    }

    /**
     * @return ledger id of the source
     */
    public UUID getFromLedgerId() {
        return fromLedgerId;
    }

    /**
     * @return ledger id of the target
     */
    public UUID getToLedgerId() {
        return toLedgerId;
    }

    /**
     * @return Timestamp of creation, milliseconds
     */
    public UUID getCreatedBy() {
        return this.createdBy;
    }
}
