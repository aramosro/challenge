package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.InvalidAccountException;
import com.db.awmd.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;


@Service
public class AccountsService {

    @Getter
    private final AccountsRepository accountsRepository;

    @Autowired
    public AccountsService(AccountsRepository accountsRepository) {
        this.accountsRepository = accountsRepository;
    }

    public void createAccount(Account account) {
        this.accountsRepository.createAccount(account);
    }

    public Account getAccount(String accountId) {
        return this.accountsRepository.getAccount(accountId);
    }

    /**
     * THE TRANSFER OBJECT METHOD
     *
     * @param transfer The transfer object
     */
    public void transfer(Transfer transfer) {
        //VALIDATE ACCOUNTS
        if (transfer.getAccountFromId() != null && transfer.getAccountToId() != null) {
            accountsTransferValidations(transfer.getAccountFromId(), transfer.getAccountToId());
        } else {
            throw new InvalidAccountException("Account can not be null");
        }
        this.accountsRepository.transfer(transfer);
    }

    /**
     * THIS METHOD VALIDATE THE TRANSFER ACCOUNTS
     *
     * @param fromAccountId Account from where the money is transferred
     * @param toAccountId Account to which the money is transferred
     * @return message
     */
    private void accountsTransferValidations(String fromAccountId, String toAccountId) {
        String message = null;
        //CHECK IF "FROM "ACCOUNT AND "TO" ACCOUNT ARE THE SAME
        if (fromAccountId.equals(toAccountId)) {
            message = "From and To Accounts are the same";
        }
        //CHECK IF ACCOUNTS EXIST
        if (message == null) {
            message = this.existAccount(Arrays.asList(fromAccountId, toAccountId));
        }
        if (message != null) {
            throw new InvalidAccountException(message);
        }
    }

    /**
     * THIS METHOD CHECK IF ACCOUNTS EXIST, RETURN NULL IF ALL ACCOUNTS EXIST AND A ERROR MESSAGE IF NOT
     *
     * @param accountIds List of accounts to check if exist
     * @return message
     */
    public String existAccount(List<String> accountIds) {
        StringBuilder message = new StringBuilder();
        for (String accountId : accountIds) {
            if (this.accountsRepository.getAccount(accountId) == null) {
                message.append(message.length() == 0 ? "account: " + accountId : " and account: " + accountId);
            }
        }
        return message.length() == 0 ? null : message.append(" not exist").toString();
    }

}
