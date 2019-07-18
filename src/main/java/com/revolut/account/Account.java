package com.revolut.account;

import java.util.UUID;

/**
 * Represents a user account in the system.
 * Just a UUID represents *any* account in the system - could be a user or another service
 */
public class Account {
    /**
     * Unique id
     */
    private UUID id;

    /**
     * Name
     */
    private String name;

    /**
     * @param id unique id
     * @param name name
     */
    public Account(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * @return unique id
     */
    public UUID getId() {
        return this.id;
    }

    /**
     * @return name
     */
    public String getName() {
        return this.name;
    }
}
