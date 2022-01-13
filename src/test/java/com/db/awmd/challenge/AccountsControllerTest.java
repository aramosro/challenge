package com.db.awmd.challenge;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.service.AccountsService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class AccountsControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private AccountsService accountsService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void prepareMockMvc() {
        this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

        // Reset the existing accounts before each test.
        accountsService.getAccountsRepository().clearAccounts();
    }

    @Test
    public void createAccount() throws Exception {
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

        Account account = accountsService.getAccount("Id-123");
        assertThat(account.getAccountId()).isEqualTo("Id-123");
        assertThat(account.getBalance()).isEqualByComparingTo("1000");
    }

    @Test
    public void createDuplicateAccount() throws Exception {
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isBadRequest());
    }

    @Test
    public void createAccountNoAccountId() throws Exception {
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"balance\":1000}")).andExpect(status().isBadRequest());
    }

    @Test
    public void createAccountNoBalance() throws Exception {
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-123\"}")).andExpect(status().isBadRequest());
    }

    @Test
    public void createAccountNoBody() throws Exception {
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createAccountNegativeBalance() throws Exception {
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-123\",\"balance\":-1000}")).andExpect(status().isBadRequest());
    }

    @Test
    public void createAccountEmptyAccountId() throws Exception {
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().isBadRequest());
    }

    @Test
    public void getAccount() throws Exception {
        String uniqueAccountId = "Id-" + System.currentTimeMillis();
        Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
        this.accountsService.createAccount(account);
        this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId))
                .andExpect(status().isOk())
                .andExpect(
                        content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
    }

    /**
     * TEST TRANSFER WITHOUT BODY
     *
     * @throws Exception
     */
    @Test
    public void transferNoBody() throws Exception {
        this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    /**
     * TEST TRANSFER WITHOUT "FROM" ACCOUNT ID
     *
     * @throws Exception
     */
    @Test
    public void transferNoFromAccountId() throws Exception {
        this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountToId\":\"Id-1234\",\"amount\":1000}")).andExpect(status().isBadRequest());
    }

    /**
     * TEST TRANSFER WITHOUT "TO" ACCOUNT ID
     *
     * @throws Exception
     */
    @Test
    public void transferNoToAccountId() throws Exception {
        this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountFromId\":\"Id-123\",\"amount\":1000}")).andExpect(status().isBadRequest());
    }

    /**
     * TEST TRANSFER WITHOUT AMOUNT
     *
     * @throws Exception
     */
    @Test
    public void transferNoAmount() throws Exception {
      this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
              .content("{\"accountFromId\":\"\",\"accountToId\":\"Id-1234\"")).andExpect(status().isBadRequest());
    }

    /**
     * TEST EMPTY "FROM" ACCOUNT IN TRANSFERS
     *
     * @throws Exception
     */
    @Test
    public void transferEmptyFromAccount() throws Exception {
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-1234\",\"balance\":2000}")).andExpect(status().isCreated());
        this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountFromId\":\"\",\"accountToId\":\"Id-1234\",\"amount\":1000}")).andExpect(status().isBadRequest());
    }

    /**
     * TEST EMPTY "TO" ACCOUNT IN TRANSFERS
     *
     * @throws Exception
     */
    @Test
    public void transferEmptyToAccount() throws Exception {
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-1234\",\"balance\":2000}")).andExpect(status().isCreated());
        this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountFromId\":\"Id-123\",\"accountToId\":\"\",\"amount\":1000}")).andExpect(status().isBadRequest());
    }

    /**
     * TEST EMPTY "FROM AND TO" ACCOUNTS IN TRANSFERS
     *
     * @throws Exception
     */
    @Test
    public void transferEmptyToAndFromAccounts() throws Exception {
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-1234\",\"balance\":2000}")).andExpect(status().isCreated());
        this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountFromId\":\"\",\"accountToId\":\"\",\"amount\":1000}")).andExpect(status().isBadRequest());
    }

    /**
     * TEST TRANSFER WITH NO POSITIVE AMOUNT VALUE
     *
     * @throws Exception
     */
    @Test
    public void transferNoPositiveAmount() throws Exception {
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-1234\",\"balance\":2000}")).andExpect(status().isCreated());
        this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountFromId\":\"Id-123\",\"accountToId\":\"Id-1234\",\"amount\":0}")).andExpect(status().isBadRequest());
    }

    /**
     * TEST A VALID TRANSFER
     *
     * @throws Exception
     */
    @Test
    public void transfer() throws Exception {
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-1234\",\"balance\":2000}")).andExpect(status().isCreated());
        this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountFromId\":\"Id-123\",\"accountToId\":\"Id-1234\",\"amount\":1}")).andExpect(status().isOk());
        Account account1 = accountsService.getAccount("Id-123");
        Account account2 = accountsService.getAccount("Id-1234");
        assertThat(account1.getBalance()).isEqualByComparingTo("999");
        assertThat(account2.getBalance()).isEqualByComparingTo("2001");
    }
}
