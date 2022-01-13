package com.db.awmd.challenge.exception;

/**
 * EXCEPTION THROWN WHEN INVALID BALANCES
 */
public class InvalidBalanceException extends RuntimeException {

    public InvalidBalanceException(String message){
        super(message);
    }
}
