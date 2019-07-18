package com.revolut.transfer;

import com.revolut.account.Account;
import com.revolut.account.AccountService;
import com.revolut.command.executor.CommandExecutor;
import com.revolut.ledger.LedgerService;
import com.revolut.router.Response;
import com.revolut.transfer.Exception.TransferException;
import com.revolut.utils.UUIDUtil;
import org.slf4j.Logger;

import java.util.UUID;

/**
 * Handle and execute transfer requests
 */
public class TransferService {
    /**
     * Ledger service
     */
    private LedgerService ledgerService;

    /**
     * Command executor
     */
    private CommandExecutor commandExecutor;

    /**
     * Account service
     */
    private AccountService accountService;

    /**
     * Logger
     */
    private Logger logger;

    /**
     * Waiting timeout when acquiring lock
     */
    private int lockWaitingTimeout;

    /**
     * @param ledgerService ledger service
     * @param commandExecutor command executor
     * @param accountService account service
     * @param logger logger
     * @param lockWaitingTimeout waiting timeout when acquiring lock
     */
    public TransferService(
        LedgerService ledgerService,
        CommandExecutor commandExecutor,
        AccountService accountService,
        Logger logger,
        int lockWaitingTimeout
    ) {
        this.ledgerService = ledgerService;
        this.commandExecutor = commandExecutor;
        this.accountService = accountService;
        this.logger = logger;
        this.lockWaitingTimeout = lockWaitingTimeout;
    }

    /**
     * Handle and execute transfer requests
     *
     * @param request incoming request
     * @param sparkResponse spark response
     * @return response object
     */
    public Object transferAction(spark.Request request, spark.Response sparkResponse) throws Exception {
        String fromAccountParameter = request.queryParams("from");
        String toAccountParameter = request.queryParams("to");
        String amountParameter = request.queryParams("amount");


        // ALL THE VALIDATIONS

        if (fromAccountParameter == null)
            return new Response(Response.ResponseCode.MISSING_PARAMETER, "Missing \"from\" parameter");

        if (toAccountParameter == null)
            return new Response(Response.ResponseCode.MISSING_PARAMETER, "Missing \"to\" parameter");

        if (amountParameter == null)
            return new Response(Response.ResponseCode.MISSING_PARAMETER, "Missing \"amount\" parameter");

        UUID fromAccountUUID = UUIDUtil.parse(fromAccountParameter);
        UUID toAccountUUID = UUIDUtil.parse(toAccountParameter);

        if (fromAccountUUID == null)
            return new Response(Response.ResponseCode.WRONG_PARAMETER, "Wrong \"from\" format");

        if (toAccountUUID == null)
            return new Response(Response.ResponseCode.WRONG_PARAMETER, "Wrong \"to\" format");

        int amount;

        try {
            amount = Integer.parseInt(amountParameter);
        } catch (NumberFormatException numberFormatException) {
            return new Response(Response.ResponseCode.WRONG_PARAMETER, "Wrong \"amount\" format");
        }

        if (amount <= 0)
            return new Response(Response.ResponseCode.INVALID_VALUE, "\"amount\" must be greater than zero");

        Account fromAccount = accountService.get(fromAccountUUID);
        Account toAccount = accountService.get(toAccountUUID);

        if (fromAccount == null)
            return new Response(Response.ResponseCode.INVALID_VALUE, "\"from\" account does not exist");

        if (toAccount == null)
            return new Response(Response.ResponseCode.INVALID_VALUE, "\"to\" account does not exist");

        if (fromAccount.getId().equals(toAccount.getId()))
            return new Response(Response.ResponseCode.INVALID_VALUE, "can not transfer to the same account");

        // Create transfer command

        TransferCommand transferCommand = new TransferCommand(
            ledgerService,
            fromAccount.getId(),
            toAccount.getId(),
            amount,
            logger,
            lockWaitingTimeout
        );

        // Execute command
        try {
            commandExecutor.execute(transferCommand);
        } catch (TransferException transferException) {
            if (transferException.isDisplayable()) {
                logger.error("Exception during transfer", transferException);
                return new Response(Response.ResponseCode.INTERNAL_ERROR, transferException.toDisplayable());
            }

            // This is ugly but the reason of doing this is to put the traceId in the response
            throw transferException;
        }

        return new Response(Response.ResponseCode.NO_ERROR, "");
    }
}
