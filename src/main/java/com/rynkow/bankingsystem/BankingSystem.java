package com.rynkow.bankingsystem;

import com.rynkow.bankingsystem.repository.AccountRepository;
import com.rynkow.bankingsystem.service.CurrencyExchangeService;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BankingSystem {
    private final AccountRepository accountRepository;
    private final CurrencyExchangeService exchangeService;

    public BankingSystem() throws IOException, ParseException {
        this.accountRepository = AccountRepository.getInstance();
        this.exchangeService = CurrencyExchangeService.getInstance();
    }

    public void sendFunds(Currency currency, BigDecimal amount, String senderId, String receiverId) throws RuntimeException {
        Account senderAccount = accountRepository.getAccountByUserIdAndCurrency(senderId, currency)
                .orElseThrow(() -> new RuntimeException("sender account not found"));
        Account receiverAccount = accountRepository.getAccountByUserIdAndCurrency(receiverId, currency)
                .orElseThrow(() -> new RuntimeException("receiver account not found"));

        Transaction sendTransaction = new Transaction(TransactionType.SEND, senderAccount.getBalance(), amount.negate());
        Transaction receiveTransaction = new Transaction(TransactionType.RECEIVE, receiverAccount.getBalance(), amount);

        senderAccount.withdraw(amount);
        receiverAccount.deposit(amount);

        senderAccount.addTransactionToHistory(sendTransaction);
        receiverAccount.addTransactionToHistory(receiveTransaction);
    }

    public void depositFunds(Currency currency, BigDecimal amount, String userId) throws RuntimeException {
        Account account = accountRepository.getAccountByUserIdAndCurrency(userId, currency)
                .orElseThrow(() -> new RuntimeException("account not found"));

        Transaction depositTransaction = new Transaction(TransactionType.DEPOSIT, account.getBalance(), amount);
        account.deposit(amount);
        account.addTransactionToHistory(depositTransaction);
    }

    public void withdrawFunds(Currency currency, BigDecimal amount, String userId) throws RuntimeException {
        Account account = accountRepository.getAccountByUserIdAndCurrency(userId, currency)
                .orElseThrow(() -> new RuntimeException("account not found"));

        Transaction withdrawTransaction = new Transaction(TransactionType.WITHDRAW, account.getBalance(), amount.negate());
        account.withdraw(amount);
        account.addTransactionToHistory(withdrawTransaction);
    }

    public void exchangeCurrency(Currency baseCurrency, Currency targetCurrency, BigDecimal amount, String userId) throws RuntimeException {
        Account baseCurrencyAccount = accountRepository.getAccountByUserIdAndCurrency(userId, baseCurrency)
                .orElseThrow(() -> new RuntimeException("base currency account not found"));
        Account targetCurrencyAccount = accountRepository.getAccountByUserIdAndCurrency(userId, targetCurrency)
                .orElseThrow(() -> new RuntimeException("target currency account not found"));

        BigDecimal receivedAmount = exchangeService.exchange(baseCurrency, targetCurrency, amount);

        Transaction baseCurrencyExchangeTransaction = new Transaction(TransactionType.EXCHANGE, baseCurrencyAccount.getBalance(), amount.negate());
        Transaction targetCurrencyExchangeTransaction = new Transaction(TransactionType.EXCHANGE, targetCurrencyAccount.getBalance(), receivedAmount);

        baseCurrencyAccount.withdraw(amount);
        targetCurrencyAccount.deposit(receivedAmount);

        baseCurrencyAccount.addTransactionToHistory(baseCurrencyExchangeTransaction);
        targetCurrencyAccount.addTransactionToHistory(targetCurrencyExchangeTransaction);
    }

    public List<Transaction> getAccountHistory(String userId, Currency currency, Date startDate, Date endDate, TransactionType transactionType) throws RuntimeException {
        List<Transaction> transactions = new ArrayList<>();

        // get transaction history
        // for specific currency
        if (currency != null)
            transactions.addAll(
                    accountRepository.getAccountByUserIdAndCurrency(userId, currency)
                            .orElseThrow(() -> new RuntimeException("account not found"))
                            .getTransactionHistory()
            );
        // for all currencies
        else {
            List<Account> userAccounts = accountRepository.getAccountsByUserId(userId);
            if (userAccounts.size() == 0) throw new RuntimeException("account not found");
            for (Account account : userAccounts)
                transactions.addAll(account.getTransactionHistory());
        }

        // filter results
        transactions = transactions.stream().filter(transaction -> {
            // filter by date
            if (startDate != null && transaction.transactionDate.before(startDate))
                return false;
            if (endDate != null && transaction.transactionDate.after(endDate))
                return false;

            // filter by type
            if (transactionType != null && !transaction.type.equals(transactionType))
                return false;

            return true;

        }).toList();

        return transactions;
    }

    public void newUser(String userId) throws IllegalArgumentException {
        if (accountRepository.getAccountsByUserId(userId).size() > 0)
            throw new IllegalArgumentException("duplicated userId");

        for (Currency currency : Currency.values()) {
            Account newAccount = new Account(userId, currency);
            accountRepository.save(newAccount);
        }
    }
}
