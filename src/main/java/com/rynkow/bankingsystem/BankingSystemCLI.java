package com.rynkow.bankingsystem;

import com.rynkow.bankingsystem.model.Currency;
import com.rynkow.bankingsystem.model.Transaction;
import com.rynkow.bankingsystem.repository.ListAccountRepository;
import com.rynkow.bankingsystem.service.JsonCurrencyExchangeService;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BankingSystemCLI {

    public static void main(String[] args) throws IOException, ParseException {
        BankingSystem bankingSystem = new BankingSystem(ListAccountRepository.getInstance(), JsonCurrencyExchangeService.getInstance());
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String activeUser;
        while (true) {

            // get active user
            try {
                System.out.print("enter command: create|login userId\n> ");
                String[] command = br.readLine().split("\\s+");
                if (command.length != 2) throw new RuntimeException("invalid Command");
                activeUser = command[1];
                if (Objects.equals(command[0], "create")) {
                    bankingSystem.newUser(activeUser);
                } else if (Objects.equals(command[0], "login")) {
                    bankingSystem.getAccountBalance(activeUser);
                } else throw new RuntimeException("invalid command");
            } catch (RuntimeException e) {
                System.out.println(e.getMessage());
                continue;
            }

            // operations on account
            accountCommands:
            while (true) {
                try {
                    System.out.println("enter command: balance|history|deposit|withdraw|send|exchange|quit");
                    System.out.print(activeUser + "> ");
                    String command = br.readLine().trim();
                    switch (command) {
                        case "quit":
                            break accountCommands;
                        case "balance":
                            Map<Currency, BigDecimal> balance = bankingSystem.getAccountBalance(activeUser);
                            for (Currency currency : Currency.values())
                                System.out.println("\t" + currency.name() + ": " + balance.get(currency).toString());
                            break;
                        case "history":
                            List<Transaction> history = bankingSystem.getAccountHistory(activeUser, null, null, null, null);
                            for (Transaction transaction : history)
                                System.out.println("\t" + transaction.toString());
                            break;
                        case "deposit":
                            System.out.println("enter command: PLN|EUR|USD amount");
                            System.out.print(activeUser + "> ");
                            String[] depositCommand = br.readLine().split("\\s+");
                            if (depositCommand.length != 2) throw new RuntimeException("invalid deposit command");
                            bankingSystem.depositFunds(Currency.valueOf(depositCommand[0]), new BigDecimal(depositCommand[1]), activeUser);
                            break;
                        case "withdraw":
                            System.out.println("enter command: PLN|EUR|USD amount");
                            System.out.print(activeUser + "> ");
                            String[] withdrawCommand = br.readLine().split("\\s+");
                            if (withdrawCommand.length != 2) throw new RuntimeException("invalid withdraw command");
                            bankingSystem.withdrawFunds(Currency.valueOf(withdrawCommand[0]), new BigDecimal(withdrawCommand[1]), activeUser);
                            break;
                        case "send":
                            System.out.println("enter command: PLN|EUR|USD amount receiverId");
                            System.out.print(activeUser + "> ");
                            String[] sendCommand = br.readLine().split("\\s+");
                            if (sendCommand.length != 3) throw new RuntimeException("invalid send command");
                            bankingSystem.sendFunds(Currency.valueOf(sendCommand[0]), new BigDecimal(sendCommand[1]), activeUser, sendCommand[2]);
                            break;
                        case "exchange":
                            System.out.println("enter command: (base currency)PLN|EUR|USD (target currency)PLN|EUR|USD amount");
                            System.out.print(activeUser + "> ");
                            String[] exchangeCommand = br.readLine().split("\\s+");
                            if (exchangeCommand.length != 3) throw new RuntimeException("invalid exchange command");
                            bankingSystem.exchangeCurrency(Currency.valueOf(exchangeCommand[0]), Currency.valueOf(exchangeCommand[1]), new BigDecimal(exchangeCommand[2]), activeUser);
                            break;
                        default:
                            throw new RuntimeException("invalid command: " + command);
                    }
                } catch (RuntimeException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }
}
