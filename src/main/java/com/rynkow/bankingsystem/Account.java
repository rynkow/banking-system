package com.rynkow.bankingsystem;

import java.math.BigDecimal;

public class Account {
    private final String userId;
    private BigDecimal balance;
    private final Currency currency;

    public Account(String userId, Currency currency) {
        this.userId = userId;
        this.currency = currency;
        this.balance = BigDecimal.ZERO;
    }

    public void deposit(BigDecimal amount) throws IllegalArgumentException{
        if (amount.signum()< 1)
            throw new IllegalArgumentException("deposit amount not positive");

        balance = balance.add(amount);
    }

    public void withdraw(BigDecimal amount) throws IllegalArgumentException, IllegalStateException{
        if (amount.signum() < 1)
            throw new IllegalArgumentException("withdraw amount not positive");
        if (balance.compareTo(amount) < 0)
            throw new IllegalStateException("insufficient Balance");

        balance = balance.subtract(amount);
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public Currency getCurrency() {
        return currency;
    }

    public String getUserId() {
        return userId;
    }
}
