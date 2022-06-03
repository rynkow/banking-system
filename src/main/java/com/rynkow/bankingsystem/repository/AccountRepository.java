package com.rynkow.bankingsystem.repository;

import com.rynkow.bankingsystem.model.Account;
import com.rynkow.bankingsystem.model.Currency;

import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    List<Account> getAccounts();

    List<Account> getAccountsByUserId(String userId);

    List<Account> getAccountsByCurrency(Currency currency);

    Optional<Account> getAccountByUserIdAndCurrency(String userId, Currency currency);

    void save(Account account) throws IllegalArgumentException;
}
