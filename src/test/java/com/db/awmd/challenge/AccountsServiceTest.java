package com.db.awmd.challenge;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InvalidAccountException;
import com.db.awmd.challenge.exception.InvalidBalanceException;
import com.db.awmd.challenge.service.AccountsService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class AccountsServiceTest {

    @Autowired
    private AccountsService accountsService;

    private Timestamp thread1Start;
    private Timestamp thread2Start;

    @Before
    public void setupMock() {
        accountsService.getAccountsRepository().clearAccounts();
    }

    @Test
    public void addAccount() throws Exception {
        Account account = new Account("Id-123");
        account.setBalance(new BigDecimal(1000));
        this.accountsService.createAccount(account);

        assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
    }

    @Test
    public void addAccount_failsOnDuplicateId() throws Exception {
        String uniqueId = "Id-" + System.currentTimeMillis();
        Account account = new Account(uniqueId);
        this.accountsService.createAccount(account);

        try {
            this.accountsService.createAccount(account);
            fail("Should have failed when adding duplicate account");
        } catch (DuplicateAccountIdException ex) {
            assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
        }
    }

    /**
     * TEST IF A LIST OF ACCOUNTS EXISTS
     *
     * @throws Exception
     */
    @Test
    public void accountNotExist() throws Exception {
        Account account = new Account("Id-123");
        account.setBalance(new BigDecimal(1000));
        this.accountsService.createAccount(account);
        Account account2 = new Account("Id-1234");
        account2.setBalance(new BigDecimal(2000));
        this.accountsService.createAccount(account2);
        List<String> list = new ArrayList<>();
        list.add(account.getAccountId());
        list.add("Id-9999");
        String message = this.accountsService.existAccount(list);
        assertThat(message).isEqualTo("account: Id-9999 not exist");
    }

    /**
     * TEST TRANSFER ACCOUNT NOT EXIST
     *
     * @throws Exception
     */
    @Test
    public void transferInvalidAccount() throws Exception {
        Account account = new Account("Id-123");
        account.setBalance(new BigDecimal(1000));
        this.accountsService.createAccount(account);
        Account account2 = new Account("Id-1234");
        account2.setBalance(new BigDecimal(2000));
        this.accountsService.createAccount(account2);
        Transfer transfer = new Transfer("Id-12345", "Id-123", new BigDecimal(10));
        try {
            accountsService.transfer(transfer);
            fail("Should have failed when account not exist");
        } catch (InvalidAccountException ex) {
            assertThat(ex.getMessage()).isEqualTo("account: Id-12345 not exist");
        }
    }

    /**
     * TEST TRANSFER ACCOUNTS("FROM" AND "TO")  NOT EXIST
     *
     * @throws Exception
     */
    @Test
    public void transferInvalidAccounts() throws Exception {
        Account account = new Account("Id-123");
        account.setBalance(new BigDecimal(1000));
        this.accountsService.createAccount(account);
        Account account2 = new Account("Id-1234");
        account2.setBalance(new BigDecimal(2000));
        this.accountsService.createAccount(account2);
        Transfer transfer = new Transfer("Id-12345", "Id-1239", new BigDecimal(10));
        try {
            accountsService.transfer(transfer);
            fail("Should have failed when account not exist");
        } catch (InvalidAccountException ex) {
            assertThat(ex.getMessage()).isEqualTo("account: Id-12345 and account: Id-1239 not exist");
        }
    }

    /**
     * TEST ACCOUNT WITH NULL ID
     *
     * @throws Exception
     */
    @Test
    public void transferNullAccountIds() throws Exception {
        Account account = new Account("Id-123");
        account.setBalance(new BigDecimal(1000));
        this.accountsService.createAccount(account);
        Account account2 = new Account("Id-1234");
        account2.setBalance(new BigDecimal(2000));
        Transfer transfer = new Transfer(null, "Id-123", new BigDecimal(10));
        try {
            accountsService.transfer(transfer);
            fail("Should have failed when when account is null");
        } catch (InvalidAccountException ex) {
            assertThat(ex.getMessage()).isEqualTo("Account can not be null");
        }
    }

    /**
     * TEST TRANSFER SERVICE WITH NEGATIVE BALANCE
     */
    @Test
    public void transferInvalidBalance() {
        Account account = new Account("Id-123");
        account.setBalance(new BigDecimal(1000));
        this.accountsService.createAccount(account);
        Account account2 = new Account("Id-1234");
        account2.setBalance(new BigDecimal(2000));
        Transfer transfer = new Transfer(account.getAccountId(), account2.getAccountId(), new BigDecimal(500000));
        this.accountsService.createAccount(account2);
        try {
            accountsService.transfer(transfer);
            fail("Should have failed when transfer balance is negative");
        } catch (InvalidBalanceException ex) {
            assertThat(ex.getMessage()).isEqualTo("account balance can not be negative");
        }
    }

    /**
     * TEST THREAD-SAFE VALID TRANSFERS
     */
    @Test
    public void transfer() throws Exception {
        Account account = new Account("Id-123");
        account.setBalance(new BigDecimal(1000));
        this.accountsService.createAccount(account);
        Account account2 = new Account("Id-1234");
        account2.setBalance(new BigDecimal(2000));
        this.accountsService.createAccount(account2);
        Transfer transfer = new Transfer(account.getAccountId(), account2.getAccountId(), new BigDecimal(1));
        Transfer transfer2 = new Transfer(account2.getAccountId(), account.getAccountId(), new BigDecimal(2001));
        thread1Start = null;
        //START TWO THREAD (FROM A TO B, FROM B TO A) TO TEST THREAD-SAFE AND DEADLOCKS
        new Thread(() -> {
            try {
                Thread.sleep(1 * 1000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            thread1Start = new Timestamp(System.currentTimeMillis());
            log.info("thread 1 start : " + thread1Start);
            accountsService.transfer(transfer);
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(1 * 1003);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            thread2Start = new Timestamp(System.currentTimeMillis());
            log.info("thread 2 start : " + thread2Start);
            try {
                accountsService.transfer(transfer2);
            } catch (InvalidBalanceException ex) {
                //IF THREAD 2 TRANSFER START BEFORE THREAD 1 TRANSFER, THE BALANCES ARE INVALID, THREAD 2 TRANSFER CAN NOT BE PROCESSED
                log.info("thread 2 start before thread 1");
                assertThat(thread1Start == null || thread1Start.compareTo(thread2Start) > 0);
                assertThat(ex.getMessage()).isEqualTo("account balance can not be negative");
            }
        }).start();
        //WAIT UNTIL THREADS FINISH TO CHECK THE BALANCES
        try {
            Thread.sleep(2 * 1000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        //IF THREAD 1 TRANSFER START BEFORE THREAD 2 TRANSFER THE BALANCES ARE CORRECT
        if (thread1Start != null && thread1Start.compareTo(thread2Start) < 0) {
            Account acc = this.accountsService.getAccount("Id-123");
            Account acc2 = this.accountsService.getAccount("Id-1234");
            log.info("balance after thread-safe :" + acc.getBalance());
            log.info("balance after thread-safe :" + acc2.getBalance());
            assertThat(acc.getBalance()).isEqualByComparingTo("3000");
            assertThat(acc2.getBalance()).isEqualByComparingTo("0");
        }

    }
}
