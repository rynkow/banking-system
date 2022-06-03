package com.rynkow.bankingsystem.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;


public class AccountTest {

    @Test
    void ShouldCreateAnAccountWithEmptyBalance() {
        // when new account is created
        Account newAccount = new Account("userID", Currency.PLN);

        // then balance is 0
        assertEquals(0, newAccount.getBalance().compareTo(BigDecimal.ZERO));
    }

    @Test
    void ShouldAddFundsToBalanceOnDeposit() {
        // given an account
        Account account = new Account("userID", Currency.PLN);

        // when depositing funds
        account.deposit(BigDecimal.valueOf(12345));
        account.deposit(BigDecimal.valueOf(654));
        account.deposit(BigDecimal.valueOf(789));

        // then balance is updated
        assertEquals(0, account.getBalance().compareTo(BigDecimal.valueOf(12345 + 654 + 789)));
    }

    @Test
    void ShouldNotDepositNonPositiveAmount() {
        // given an account
        Account account = new Account("userID", Currency.PLN);

        // when depositing non-positive values
        // then exception is thrown and balance is not updated
        assertThrows(IllegalArgumentException.class, () -> account.deposit(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> account.deposit(BigDecimal.valueOf(-156)));
        assertEquals(0, account.getBalance().compareTo(BigDecimal.ZERO));
    }

    @Test
    void ShouldRemoveFundsFromBalanceOnWithdraw() {
        // given an account with positive balance
        Account account = new Account("userID", Currency.PLN);
        account.deposit(BigDecimal.valueOf(12345));

        // when funds are withdrawn
        account.withdraw(BigDecimal.valueOf(2300));
        account.withdraw(BigDecimal.valueOf(45));

        // then balance is updated
        assertEquals(0, account.getBalance().compareTo(BigDecimal.valueOf(12345 - 2300 - 45)));
    }

    @Test
    void ShouldNotWithdrawMoreThanCurrentBalance() {
        // given an account with positive balance
        Account account = new Account("userID", Currency.PLN);
        account.deposit(BigDecimal.valueOf(123));

        // when trying to withdraw more than current balance
        // then exception is thrown and balance is unchanged
        assertThrows(IllegalStateException.class, () -> account.withdraw(BigDecimal.valueOf(500)));
        assertEquals(0, account.getBalance().compareTo(BigDecimal.valueOf(123)));
    }

    @Test
    void ShouldNotWithdrawNonPositiveAmount() {
        // given an account
        Account account = new Account("userID", Currency.PLN);
        account.deposit(BigDecimal.valueOf(12345));

        // when withdrawing non-positive values
        // then exception is thrown and balance is not updated
        assertThrows(IllegalArgumentException.class, () -> account.withdraw(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> account.withdraw(BigDecimal.valueOf(-156)));
        assertEquals(0, account.getBalance().compareTo(BigDecimal.valueOf(12345)));
    }

    @Test
    void ShouldAddTransactionsToHistory() {
        // given an account and transactions
        Account account = new Account("userID", Currency.PLN);
        Transaction transaction1 = new Transaction(TransactionType.DEPOSIT, Currency.PLN, BigDecimal.valueOf(100), BigDecimal.valueOf(10));
        Transaction transaction2 = new Transaction(TransactionType.RECEIVE, Currency.PLN, BigDecimal.valueOf(1234), BigDecimal.valueOf(1));

        // when transactions are added to history
        account.addTransactionToHistory(transaction1);
        account.addTransactionToHistory(transaction2);

        // then those transactions are saved
        assertTrue(account.getTransactionHistory().contains(transaction1));
        assertTrue(account.getTransactionHistory().contains(transaction2));
    }

    @Test
    void ShouldCreateAccountsWithDifferentCurrencies() {
        // when creating accounts with different currencies
        Account accountPLN = new Account("userId", Currency.PLN);
        Account accountEUR = new Account("userId", Currency.EUR);
        Account accountUSD = new Account("userId", Currency.USD);

        // then currencies are set correctly
        assertEquals(Currency.PLN, accountPLN.getCurrency());
        assertEquals(Currency.EUR, accountEUR.getCurrency());
        assertEquals(Currency.USD, accountUSD.getCurrency());
    }
}
