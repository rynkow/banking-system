package com.rynkow.bankingsystem.repository;

import com.rynkow.bankingsystem.Account;
import com.rynkow.bankingsystem.Currency;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class AccountRepository {
    private static AccountRepository instance;
    private final List<Account> accounts;
    private AccountRepository(){
        accounts = new LinkedList<>();
    }

    public static AccountRepository getInstance() {
        if (instance == null)
            instance = new AccountRepository();
        return instance;
    }

    public List<Account> getAccounts(){
        return accounts;
    }

    public List<Account> getAccountsByUserId(String userId){
        return accounts.stream().filter(account -> account.getUserId().equals(userId)).toList();
    }

    public List<Account> getAccountsByCurrency(Currency currency){
        return accounts.stream().filter(account -> account.getCurrency().equals(currency)).toList();
    }

    public Optional<Account> getAccountByUserIdAndCurrency(String userId, Currency currency) {
        return accounts.stream().filter(account -> account.getUserId().equals(userId) && account.getCurrency().equals(currency)).findAny();
    }

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
