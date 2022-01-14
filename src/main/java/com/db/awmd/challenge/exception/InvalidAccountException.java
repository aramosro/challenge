package com.db.awmd.challenge.exception;

/**
 * EXCEPTION THROWN WHEN INVALID ACCOUNTS
 *
 */
public class InvalidAccountException extends RuntimeException {

    public InvalidAccountException(String message) {
        super(message);
    }
}
