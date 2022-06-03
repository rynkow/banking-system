package com.rynkow.bankingsystem.repository;

import com.rynkow.bankingsystem.model.Account;
import com.rynkow.bankingsystem.model.Currency;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class ListAccountRepository implements AccountRepository {
    private static ListAccountRepository instance;
    private final List<Account> accounts;

    private ListAccountRepository() {
        accounts = new LinkedList<>();
    }

    public static ListAccountRepository getInstance() {
        if (instance == null)
            instance = new ListAccountRepository();
        return instance;
    }

    @Override
    public List<Account> getAccounts() {
        return accounts;
    }

    @Override
    public List<Account> getAccountsByUserId(String userId) {
        return accounts.stream().filter(account -> account.getUserId().equals(userId)).toList();
    }

    @Override
    public List<Account> getAccountsByCurrency(Currency currency) {
        return accounts.stream().filter(account -> account.getCurrency().equals(currency)).toList();
    }

    @Override
    public Optional<Account> getAccountByUserIdAndCurrency(String userId, Currency currency) {
        return accounts.stream().filter(account -> account.getUserId().equals(userId) && account.getCurrency().equals(currency)).findAny();
    }

    @Override
    public void save(Account account) throws IllegalArgumentException {
        if (getAccountByUserIdAndCurrency(account.getUserId(), account.getCurrency()).isPresent())
            throw new IllegalArgumentException("Duplicated account");

        accounts.add(account);
    }

    // for testing
    public void clear() {
        accounts.clear();
    }
}
