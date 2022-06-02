package com.rynkow.bankingsystem;

import java.math.BigDecimal;
import java.util.Date;

public class Transaction {
    public final TransactionType type;
    public final BigDecimal initialBalance;
    public final BigDecimal balanceChange;
    public final Date transactionDate;

    public Transaction(TransactionType type, BigDecimal initialBalance, BigDecimal balanceChange) {
        this.type = type;
        this.initialBalance = initialBalance;
        this.balanceChange = balanceChange;
        this.transactionDate = new Date();
    }
}
