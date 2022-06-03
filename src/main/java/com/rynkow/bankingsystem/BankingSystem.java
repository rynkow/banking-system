package com.rynkow.bankingsystem;

import com.rynkow.bankingsystem.model.Account;
import com.rynkow.bankingsystem.model.Currency;
import com.rynkow.bankingsystem.model.Transaction;
import com.rynkow.bankingsystem.model.TransactionType;
import com.rynkow.bankingsystem.repository.AccountRepository;
import com.rynkow.bankingsystem.service.CurrencyExchangeService;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class BankingSystem {
    private final AccountRepository accountRepository;
    private final CurrencyExchangeService exchangeService;

    public BankingSystem() throws IOException, ParseException {
        this.accountRepository = AccountRepository.getInstance();
        this.exchangeService = CurrencyExchangeService.getInstance();
    }

    public void sendFunds(com.rynkow.bankingsystem.model.Currency currency, BigDecimal amount, String senderId, String receiverId) throws RuntimeException {
        Account senderAccount = accountRepository.getAccountByUserIdAndCurrency(senderId, currency)
                .orElseThrow(() -> new RuntimeException("sender account not found"));
        Account receiverAccount = accountRepository.getAccountByUserIdAndCurrency(receiverId, currency)
                .orElseThrow(() -> new RuntimeException("receiver account not found"));

        Transaction sendTransaction = new Transaction(TransactionType.SEND, currency, senderAccount.getBalance(), amount.negate());
        Transaction receiveTransaction = new Transaction(TransactionType.RECEIVE, currency, receiverAccount.getBalance(), amount);

        senderAccount.withdraw(amount);
        receiverAccount.deposit(amount);

        senderAccount.addTransactionToHistory(sendTransaction);
        receiverAccount.addTransactionToHistory(receiveTransaction);
    }

    public void depositFunds(com.rynkow.bankingsystem.model.Currency currency, BigDecimal amount, String userId) throws RuntimeException {
        Account account = accountRepository.getAccountByUserIdAndCurrency(userId, currency)
                .orElseThrow(() -> new RuntimeException("account not found"));

        Transaction depositTransaction = new Transaction(TransactionType.DEPOSIT, currency, account.getBalance(), amount);
        account.deposit(amount);
        account.addTransactionToHistory(depositTransaction);
    }

    public void withdrawFunds(com.rynkow.bankingsystem.model.Currency currency, BigDecimal amount, String userId) throws RuntimeException {
        Account account = accountRepository.getAccountByUserIdAndCurrency(userId, currency)
                .orElseThrow(() -> new RuntimeException("account not found"));

        Transaction withdrawTransaction = new Transaction(TransactionType.WITHDRAW, currency, account.getBalance(), amount.negate());
        account.withdraw(amount);
        account.addTransactionToHistory(withdrawTransaction);
    }

    public void exchangeCurrency(com.rynkow.bankingsystem.model.Currency baseCurrency, com.rynkow.bankingsystem.model.Currency targetCurrency, BigDecimal amount, String userId) throws RuntimeException {
        Account baseCurrencyAccount = accountRepository.getAccountByUserIdAndCurrency(userId, baseCurrency)
                .orElseThrow(() -> new RuntimeException("base currency account not found"));
        Account targetCurrencyAccount = accountRepository.getAccountByUserIdAndCurrency(userId, targetCurrency)
                .orElseThrow(() -> new RuntimeException("target currency account not found"));

        BigDecimal receivedAmount = exchangeService.exchange(baseCurrency, targetCurrency, amount);

        Transaction baseCurrencyExchangeTransaction = new Transaction(TransactionType.EXCHANGE, baseCurrency, baseCurrencyAccount.getBalance(), amount.negate());
        Transaction targetCurrencyExchangeTransaction = new Transaction(TransactionType.EXCHANGE, targetCurrency, targetCurrencyAccount.getBalance(), receivedAmount);

        baseCurrencyAccount.withdraw(amount);
        targetCurrencyAccount.deposit(receivedAmount);

        baseCurrencyAccount.addTransactionToHistory(baseCurrencyExchangeTransaction);
        targetCurrencyAccount.addTransactionToHistory(targetCurrencyExchangeTransaction);
    }

    public List<Transaction> getAccountHistory(String userId, com.rynkow.bankingsystem.model.Currency currency, Date startDate, Date endDate, TransactionType transactionType) throws RuntimeException {
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

        }).sorted(Comparator.comparing((Transaction t) -> t.transactionDate)).toList();

        return transactions;
    }

    public void newUser(String userId) throws IllegalArgumentException {
        if (accountRepository.getAccountsByUserId(userId).size() > 0)
            throw new IllegalArgumentException("duplicated userId");

        for (com.rynkow.bankingsystem.model.Currency currency : com.rynkow.bankingsystem.model.Currency.values()) {
            Account newAccount = new Account(userId, currency);
            accountRepository.save(newAccount);
        }
    }

    public Map<com.rynkow.bankingsystem.model.Currency, BigDecimal> getAccountBalance(String userId) {
        List<Account> accounts = accountRepository.getAccountsByUserId(userId);
        if (accounts.size() == 0)
            throw new RuntimeException("user account not found");

        Map<Currency, BigDecimal> balances = new HashMap<>();
        for (Account account : accounts)
            balances.put(account.getCurrency(), account.getBalance());

        return balances;
    }
}
