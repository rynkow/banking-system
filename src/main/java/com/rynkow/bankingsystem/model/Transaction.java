package com.rynkow.bankingsystem.model;

import java.math.BigDecimal;
import java.util.Date;

public class Transaction {
    public final TransactionType type;
    public final Currency currency;
    public final BigDecimal initialBalance;
    public final BigDecimal balanceChange;
    public final Date transactionDate;


    public Transaction(TransactionType type, Currency currency, BigDecimal initialBalance, BigDecimal balanceChange) {
        this.type = type;
        this.initialBalance = initialBalance;
        this.balanceChange = balanceChange;
        this.transactionDate = new Date();
        this.currency = currency;
    }

    @Override
    public String toString() {
        return String.format("%-16s%-16s%-25s%-25s%s", "type=" + type, "currency=" + currency, "initialBalance=" + initialBalance, "balanceChange=" + balanceChange, "transactionDate=" + transactionDate);
    }
}
