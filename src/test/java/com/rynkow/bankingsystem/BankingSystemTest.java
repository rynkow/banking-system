package com.rynkow.bankingsystem;

import com.rynkow.bankingsystem.model.Currency;
import com.rynkow.bankingsystem.model.Transaction;
import com.rynkow.bankingsystem.model.TransactionType;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BankingSystemTest {
    private static BankingSystem bankingSystem;

    @BeforeAll
    static void CreateBankingSystem() throws IOException, ParseException {
        bankingSystem = new BankingSystem();
    }

    @Test
    void ShouldAddNewUser() {
        // when creating new user
        bankingSystem.newUser("newUserTest");
        // then accounts for all currencies are created with balance 0
        Map<Currency, BigDecimal> accountsBalance = bankingSystem.getAccountBalance("newUserTest");
        for (Currency currency : Currency.values())
            assertEquals(0, accountsBalance.get(currency).compareTo(BigDecimal.ZERO));
    }

    @Test
    void ShouldNotAddDuplicatedUser() {
        // given user already existing in the system
        bankingSystem.newUser("duplicateTest");

        // when trying to add user with duplicated id
        // then exception is thrown
        assertThrows(IllegalArgumentException.class, () -> bankingSystem.newUser("duplicateTest"));
    }

    @Test
    void ShouldDepositFunds() {
        // given a user
        bankingSystem.newUser("depositTest");

        // when funds are deposited
        bankingSystem.depositFunds(Currency.PLN, BigDecimal.valueOf(1234), "depositTest");
        bankingSystem.depositFunds(Currency.PLN, BigDecimal.valueOf(10), "depositTest");
        bankingSystem.depositFunds(Currency.PLN, BigDecimal.valueOf(324), "depositTest");

        // then balance is updated
        assertEquals(0, bankingSystem.getAccountBalance("depositTest").get(Currency.PLN).compareTo(BigDecimal.valueOf(1234 + 10 + 324)));
    }

    @Test
    void ShouldNotDepositNonPositiveAmount() {
        // given a user
        bankingSystem.newUser("nonPositiveDepositTest");

        // when trying to deposit non-positive amount of funds
        // then exceptions are thrown
        assertThrows(IllegalArgumentException.class, () -> bankingSystem.depositFunds(Currency.PLN, BigDecimal.valueOf(0), "nonPositiveDepositTest"));
        assertThrows(IllegalArgumentException.class, () -> bankingSystem.depositFunds(Currency.PLN, BigDecimal.valueOf(-132), "nonPositiveDepositTest"));
    }

    @Test
    void ShouldWithdrawFunds() {
        // given a user with a positive balance
        bankingSystem.newUser("withdrawTest");
        bankingSystem.depositFunds(Currency.USD, BigDecimal.valueOf(123), "withdrawTest");

        // when withdrawing funds
        bankingSystem.withdrawFunds(Currency.USD, BigDecimal.valueOf(100), "withdrawTest");
        bankingSystem.withdrawFunds(Currency.USD, BigDecimal.valueOf(1), "withdrawTest");

        // then balance is reduced
        assertEquals(0, bankingSystem.getAccountBalance("withdrawTest").get(Currency.USD).compareTo(BigDecimal.valueOf(123 - 100 - 1)));
    }

    @Test
    void ShouldNotWithdrawFromAccountWithInsufficientBalance() {
        // given a user with a positive balance
        bankingSystem.newUser("insufficientBalanceWithdrawTest");
        bankingSystem.depositFunds(Currency.USD, BigDecimal.valueOf(100), "insufficientBalanceWithdrawTest");

        // when funds are withdrawn form account with insufficient balance
        // then exception is thrown
        assertThrows(IllegalStateException.class, () -> bankingSystem.withdrawFunds(Currency.USD, BigDecimal.valueOf(123), "insufficientBalanceWithdrawTest"));
        assertEquals(0, bankingSystem.getAccountBalance("insufficientBalanceWithdrawTest").get(Currency.USD).compareTo(BigDecimal.valueOf(100)));
    }

    @Test
    void ShouldNotWithdrawNonPositiveAmount() {
        // given a user
        bankingSystem.newUser("nonPositiveWithdrawTest");

        // when trying to withdraw non-positive amount of funds
        // then exceptions are thrown
        assertThrows(IllegalArgumentException.class, () -> bankingSystem.withdrawFunds(Currency.PLN, BigDecimal.valueOf(0), "nonPositiveWithdrawTest"));
        assertThrows(IllegalArgumentException.class, () -> bankingSystem.withdrawFunds(Currency.PLN, BigDecimal.valueOf(-132), "nonPositiveWithdrawTest"));
    }

    @Test
    void ShouldSendFunds() {
        // given two users
        bankingSystem.newUser("sender1");
        bankingSystem.newUser("receiver1");
        bankingSystem.depositFunds(Currency.PLN, BigDecimal.valueOf(123), "sender1");

        // when sending funds to other user
        bankingSystem.sendFunds(Currency.PLN, BigDecimal.valueOf(100), "sender1", "receiver1");

        // then balances are updated
        assertEquals(0, bankingSystem.getAccountBalance("sender1").get(Currency.PLN).compareTo(BigDecimal.valueOf(123 - 100)));
        assertEquals(0, bankingSystem.getAccountBalance("receiver1").get(Currency.PLN).compareTo(BigDecimal.valueOf(100)));
    }

    @Test
    void ShouldNotSendFromAccountWithInsufficientBalance() {
        // given two users
        bankingSystem.newUser("sender2");
        bankingSystem.newUser("receiver2");
        bankingSystem.depositFunds(Currency.PLN, BigDecimal.valueOf(100), "sender2");

        //  when funds are sent form account with insufficient balance
        // then exception is thrown and balances are unchanged
        assertThrows(IllegalStateException.class, () -> bankingSystem.sendFunds(Currency.PLN, BigDecimal.valueOf(123), "sender2", "receiver2"));
        assertEquals(0, bankingSystem.getAccountBalance("sender2").get(Currency.PLN).compareTo(BigDecimal.valueOf(100)));
        assertEquals(0, bankingSystem.getAccountBalance("receiver2").get(Currency.PLN).compareTo(BigDecimal.valueOf(0)));
    }

    @Test
    void ShouldNotSendNonPositiveAmount() {
        // given two users
        bankingSystem.newUser("sender3");
        bankingSystem.newUser("receiver3");

        // when trying to send non-positive amount of funds
        // then exceptions are thrown
        assertThrows(IllegalArgumentException.class, () -> bankingSystem.sendFunds(Currency.EUR, BigDecimal.valueOf(0), "sender3", "receiver3"));
        assertThrows(IllegalArgumentException.class, () -> bankingSystem.sendFunds(Currency.EUR, BigDecimal.valueOf(-123), "sender3", "receiver3"));
        assertEquals(0, bankingSystem.getAccountBalance("sender3").get(Currency.PLN).compareTo(BigDecimal.valueOf(0)));
        assertEquals(0, bankingSystem.getAccountBalance("receiver3").get(Currency.PLN).compareTo(BigDecimal.valueOf(0)));
    }

    @Test
    void ShouldExchangeCurrencies() {
        // given a user with positive balance
        bankingSystem.newUser("exchange1");
        bankingSystem.depositFunds(Currency.PLN, BigDecimal.valueOf(100), "exchange1");

        // when currencies are exchanged
        bankingSystem.exchangeCurrency(Currency.PLN, Currency.USD, BigDecimal.valueOf(99), "exchange1");

        // then balances are updated
        assertEquals(0, bankingSystem.getAccountBalance("exchange1").get(Currency.PLN).compareTo(BigDecimal.valueOf(100 - 99)));
        assertEquals(0, bankingSystem.getAccountBalance("exchange1").get(Currency.USD).compareTo(BigDecimal.valueOf(99).multiply(BigDecimal.valueOf(0.23))));
    }

    @Test
    void ShouldNotExchangeNonPositiveAmount() {
        // given a user
        bankingSystem.newUser("exchange2");
        bankingSystem.depositFunds(Currency.PLN, BigDecimal.valueOf(100), "exchange2");

        // when trying to exchange non-positive amount of funds
        // then exceptions are thrown
        assertThrows(IllegalArgumentException.class, () -> bankingSystem.exchangeCurrency(Currency.PLN, Currency.USD, BigDecimal.valueOf(0), "exchange2"));
        assertThrows(IllegalArgumentException.class, () -> bankingSystem.exchangeCurrency(Currency.PLN, Currency.USD, BigDecimal.valueOf(-55), "exchange2"));
        assertEquals(0, bankingSystem.getAccountBalance("exchange2").get(Currency.PLN).compareTo(BigDecimal.valueOf(100)));
        assertEquals(0, bankingSystem.getAccountBalance("exchange2").get(Currency.USD).compareTo(BigDecimal.valueOf(0)));
    }

    @Test
    void ShouldNotExchangeFromAccountWithInsufficientBalance() {
        // given a user
        bankingSystem.newUser("exchange3");
        bankingSystem.depositFunds(Currency.PLN, BigDecimal.valueOf(88), "exchange3");

        //  when funds are exchanged in an account with insufficient balance
        // then exception is thrown and balances are unchanged
        assertThrows(IllegalStateException.class, () -> bankingSystem.exchangeCurrency(Currency.PLN, Currency.USD, BigDecimal.valueOf(101), "exchange3"));
        assertEquals(0, bankingSystem.getAccountBalance("exchange3").get(Currency.PLN).compareTo(BigDecimal.valueOf(88)));
        assertEquals(0, bankingSystem.getAccountBalance("exchange3").get(Currency.USD).compareTo(BigDecimal.valueOf(0)));
    }

    @Test
    void ShouldGetAccountBalance() {
        // given a user with some activity
        bankingSystem.newUser("balance");
        bankingSystem.depositFunds(Currency.PLN, BigDecimal.valueOf(55), "balance");
        bankingSystem.depositFunds(Currency.USD, BigDecimal.valueOf(10), "balance");
        bankingSystem.depositFunds(Currency.EUR, BigDecimal.valueOf(161), "balance");

        bankingSystem.withdrawFunds(Currency.PLN, BigDecimal.valueOf(12), "balance");
        bankingSystem.withdrawFunds(Currency.USD, BigDecimal.valueOf(3), "balance");
        bankingSystem.withdrawFunds(Currency.EUR, BigDecimal.valueOf(16), "balance");

        // when checking account balance
        Map<Currency, BigDecimal> balance = bankingSystem.getAccountBalance("balance");

        // then values are correct
        assertEquals(0, balance.get(Currency.PLN).compareTo(BigDecimal.valueOf(55 - 12)));
        assertEquals(0, balance.get(Currency.USD).compareTo(BigDecimal.valueOf(10 - 3)));
        assertEquals(0, balance.get(Currency.EUR).compareTo(BigDecimal.valueOf(161 - 16)));
    }

    @Test
    void ShouldNotGetNonExistentUserBalance() {
        // when checking account balance of a non-existent user
        // then exception is thrown
        assertThrows(RuntimeException.class, () -> bankingSystem.getAccountBalance("a#ds4Ds3^!"));
    }

    @Test
    void ShouldGetCorrectTransactionHistory() {
        // given user with some activity
        bankingSystem.newUser("history");
        bankingSystem.depositFunds(Currency.PLN, BigDecimal.valueOf(55), "history");
        bankingSystem.depositFunds(Currency.USD, BigDecimal.valueOf(10), "history");
        bankingSystem.depositFunds(Currency.EUR, BigDecimal.valueOf(161), "history");

        bankingSystem.withdrawFunds(Currency.PLN, BigDecimal.valueOf(12), "history");
        bankingSystem.withdrawFunds(Currency.USD, BigDecimal.valueOf(3), "history");

        bankingSystem.exchangeCurrency(Currency.PLN, Currency.USD, BigDecimal.valueOf(10), "history");
        bankingSystem.exchangeCurrency(Currency.EUR, Currency.USD, BigDecimal.valueOf(100), "history");


        // when looking up transaction history
        List<Transaction> plnTransactions = bankingSystem.getAccountHistory("history", Currency.PLN, null, null, null);
        List<Transaction> depositTransactions = bankingSystem.getAccountHistory("history", null, null, null, TransactionType.DEPOSIT);
        List<Transaction> usdWithdrawTransactions = bankingSystem.getAccountHistory("history", Currency.USD, null, null, TransactionType.WITHDRAW);

        // then transactions are found
        assertEquals(3, plnTransactions.size());
        for (Transaction transaction : plnTransactions)
            assertEquals(Currency.PLN, transaction.currency);
        assertEquals(3, depositTransactions.size());
        for (Transaction transaction : depositTransactions)
            assertEquals(TransactionType.DEPOSIT, transaction.type);
        assertEquals(1, usdWithdrawTransactions.size());
        for (Transaction transaction : usdWithdrawTransactions) {
            assertEquals(Currency.USD, transaction.currency);
            assertEquals(TransactionType.WITHDRAW, transaction.type);
        }
    }
}
