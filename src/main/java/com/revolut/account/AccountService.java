package com.revolut.account;

import java.util.UUID;

/**
 * Account service to manage accounts
 */
public interface AccountService {
    /**
     * @param uuid account id
     *
     * @return Account if such exists, null otherwise
     */
    Account get(UUID uuid);
}
