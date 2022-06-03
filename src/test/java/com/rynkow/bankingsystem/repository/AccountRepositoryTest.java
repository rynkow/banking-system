package com.rynkow.bankingsystem.repository;

import com.rynkow.bankingsystem.model.Account;
import com.rynkow.bankingsystem.model.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class AccountRepositoryTest {

    @BeforeEach
    void ClearRepository() {
        AccountRepository.getInstance().clear();
    }

    @Test
    void ShouldBeSingleton() {
        // when getInstance method is called multiple times
        AccountRepository instance1 = AccountRepository.getInstance();
        AccountRepository instance2 = AccountRepository.getInstance();
        // then the same instance is returned
        assertSame(instance1, instance2);
    }

    @Test
    void newAccountsShouldBeSavedCorrectly() {
        // given account repository and a few accounts
        AccountRepository repository = AccountRepository.getInstance();
        Account account1 = new Account("user1", Currency.PLN);
        Account account2 = new Account("user2", Currency.USD);
        Account account3 = new Account("user3", Currency.EUR);

        // when saving accounts
        repository.save(account1);
        repository.save(account2);
        repository.save(account3);

        // then accounts are saved correctly
        List<Account> accounts = repository.getAccounts();
        assertTrue(accounts.contains(account1));
        assertTrue(accounts.contains(account2));
        assertTrue(accounts.contains(account3));
    }

    @Test
    void ShouldNotSaveDuplicatedAccount() {
        // given account repository already containing an account
        AccountRepository repository = AccountRepository.getInstance();
        Account account = new Account("user1", Currency.PLN);
        repository.save(account);

        // when saving duplicate account
        // then exception is thrown
        assertThrows(IllegalArgumentException.class, () -> repository.save(account));
    }

    @Test
    void ShouldFilterAccountsCorrectly() {
        // given repository with different accounts
        AccountRepository repository = AccountRepository.getInstance();
        repository.save(new Account("user1", Currency.PLN));
        repository.save(new Account("user1", Currency.USD));
        repository.save(new Account("user1", Currency.EUR));
        repository.save(new Account("user2", Currency.PLN));
        repository.save(new Account("user2", Currency.USD));
        repository.save(new Account("user2", Currency.EUR));

        // when accounts are filtered
        List<Account> user1Accounts = repository.getAccountsByUserId("user1");
        List<Account> PLNAccounts = repository.getAccountsByCurrency(Currency.PLN);
        List<Account> nonexistentUserAccounts = repository.getAccountsByUserId("aRs12$3os");
        Optional<Account> user1PLNAccount = repository.getAccountByUserIdAndCurrency("user1", Currency.PLN);
        Optional<Account> nonexistentAccount = repository.getAccountByUserIdAndCurrency("p12fgA%", Currency.USD);

        // then only relevant accounts are returned
        assertEquals(3, user1Accounts.size());
        for (Account user1Account : user1Accounts)
            assertEquals("user1", user1Account.getUserId());
        assertEquals(2, PLNAccounts.size());
        for (Account PLNAccount : PLNAccounts)
            assertEquals(Currency.PLN, PLNAccount.getCurrency());
        assertEquals(0, nonexistentUserAccounts.size());
        assertTrue(user1PLNAccount.isPresent());
        assertTrue(user1PLNAccount.get().getUserId().equals("user1") && user1PLNAccount.get().getCurrency().equals(Currency.PLN));
        assertTrue(nonexistentAccount.isEmpty());
    }
}
