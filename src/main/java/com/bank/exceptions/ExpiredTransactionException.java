package com.bank.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NO_CONTENT)
public class ExpiredTransactionException extends RuntimeException {
    public ExpiredTransactionException(String message){
        super(message);
    }
}
