package com.db.awmd.challenge.repository;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InvalidBalanceException;
import com.db.awmd.challenge.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    @Autowired
    private NotificationService notificationService;

    @Override
    public void createAccount(Account account) throws DuplicateAccountIdException {
        Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
        if (previousAccount != null) {
            throw new DuplicateAccountIdException(
                    "Account id " + account.getAccountId() + " already exists!");
        }
    }

    @Override
    public Account getAccount(String accountId) {
        return accounts.get(accountId);
    }

    /**
     * THIS METHOD TRANSFER METHOD BETWEEN TWO ACCOUNTS
     *
     * @param transfer
     * @throws InvalidBalanceException
     */
    @Override
    public void transfer(Transfer transfer) throws InvalidBalanceException {

        //READ, VALIDATE BALANCE AND UPDATE "FROM" ACCOUNT IN THREAD-SAFE MODE, USING COMPUTE METHOD FROM CONCURRENTHASMAP COLLECTION
        Account fromAccount = accounts.compute(transfer.getAccountFromId(), (key, val)
                -> {
            if (val.getBalance().subtract(transfer.getAmount()).compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidBalanceException("account balance can not be negative");
            }
            val.setBalance(val.getBalance().subtract(transfer.getAmount()));
            return val;
        });
        //READ AND UPDATE "TO" ACCOUNT IN THREAD-SAFE MODE, USING COMPUTE METHOD FROM CONCURRENTHASMAP COLLECTION
        Account toAccount = accounts.compute(transfer.getAccountToId(), (key, val)
                -> {
            val.setBalance(val.getBalance().add(transfer.getAmount()));
            return val;
        });
        //NOTIFY TRANSFER WAS SUCCESSFUL
        this.notificationService.notifyAboutTransfer(fromAccount, "tranfers to account " + transfer.getAccountFromId() + " finish succesfully");
        this.notificationService.notifyAboutTransfer(toAccount, "tranfers from account " + transfer.getAccountToId() + " finish succesfully");

    }

    @Override
    public void clearAccounts() {
        accounts.clear();
    }

}
