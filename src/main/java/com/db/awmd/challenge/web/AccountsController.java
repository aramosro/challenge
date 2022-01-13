package com.db.awmd.challenge.web;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InvalidAccountException;
import com.db.awmd.challenge.exception.InvalidBalanceException;
import com.db.awmd.challenge.service.AccountsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
@Validated
public class AccountsController {

  private final AccountsService accountsService;

  @Autowired
  public AccountsController(AccountsService accountsService) {
    this.accountsService = accountsService;
  }


  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
    log.info("Creating account {}", account);

    try {
    this.accountsService.createAccount(account);
    } catch (DuplicateAccountIdException daie) {
      return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping(path = "/{accountId}")
  public Account getAccount(@PathVariable String accountId) {
    log.info("Retrieving account for id {}", accountId);
    return this.accountsService.getAccount(accountId);
  }

  @PostMapping(value = "/transfer", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> transfer(@RequestBody @Valid Transfer transfer) {

    log.info("Transfer {} from {} to {}", transfer.getAmount(), transfer.getAccountFromId(), transfer.getAccountToId());
    try {
      //IF ACCOUNTS ARE VALID CALL SERVICE METHOD TRANSFER, CHECKING THE FINAL FROMACCOUNT BALANCE,
      // IF IT IS NOT CORRECT THE METHOD THROWS AN INVALIDBALANCEEXCEPTION OR INVALIDACCOUNTEXCEPTION, IF IS OK SEND NOTIFICATIONS TO BOTH ACCOUNTS
      this.accountsService.transfer(transfer);
    } catch (InvalidBalanceException ibe) {
      return new ResponseEntity<>(ibe.getMessage(), HttpStatus.BAD_REQUEST);
    } catch (InvalidAccountException iae) {
      return new ResponseEntity<>(iae.getMessage(), HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * THIS METHOD TRANSFORM THE MethodArgumentNotValidException MESSAGE INTO A MORE FRIENDLY ONE
   * @param ex
   * @return errors
   */
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public Map<String, String> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();

    ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage()));

    return errors;
  }

}
