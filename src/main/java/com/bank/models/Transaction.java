package com.bank.models;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Date;

public class Transaction {

    @NotNull
    private BigDecimal amount;

    @NotNull
    private Date timestamp;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "amount=" + amount +
                ", timestamp=" + timestamp +
                '}';
    }
}
