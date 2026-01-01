package com.atm.model;

import java.util.Date;

public abstract class Transaction {
    protected String transactionId;
    protected String accountId;
    protected Date date;

    public Transaction(String transactionId, String accountId) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.date = new Date();
    }

    public abstract void execute();
}
