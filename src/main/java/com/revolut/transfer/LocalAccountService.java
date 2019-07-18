package com.revolut.transfer;

import com.revolut.account.Account;
import com.revolut.account.AccountService;

import java.util.HashMap;
import java.util.UUID;

/**
 * Account service with local static storage and a few predefined accounts
 */
public class LocalAccountService implements AccountService {
    /**
     * Account storage
     */
    static private HashMap<UUID, Account> accounts = new HashMap<>();
    static {
        UUID uuid;

        uuid = UUID.fromString("326608e5-5fbf-4505-871d-d0ec830e1994");
        accounts.put(uuid, new Account(uuid, "John"));

        uuid = UUID.fromString("5ab59fdf-997f-4a20-ab33-67272b840a19");
        accounts.put(uuid, new Account(uuid, "Smith"));

        uuid = UUID.fromString("d2febbaf-0edb-4f19-824e-588b712c8c29");
        accounts.put(uuid, new Account(uuid, "Angelina"));
    }

    @Override
    public Account get(UUID uuid) {
        return accounts.getOrDefault(uuid, null);
    }
}
