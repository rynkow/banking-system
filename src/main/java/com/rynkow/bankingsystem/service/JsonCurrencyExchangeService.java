package com.rynkow.bankingsystem.service;

import com.rynkow.bankingsystem.model.Currency;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class JsonCurrencyExchangeService implements CurrencyExchangeService {
    private final static String EXCHANGE_RATE_FILE_PATH = "src/main/resources/exchangeRates.json";
    private static JsonCurrencyExchangeService instance;
    private final Map<Currency, Map<Currency, BigDecimal>> exchangeRates;

    private JsonCurrencyExchangeService() throws IOException, ParseException {
        exchangeRates = new HashMap<>();
        exchangeRates.put(Currency.PLN, new HashMap<>());
        exchangeRates.put(Currency.EUR, new HashMap<>());
        exchangeRates.put(Currency.USD, new HashMap<>());

        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader(EXCHANGE_RATE_FILE_PATH)) {
            JSONObject jsonExchangeRates = (JSONObject) jsonParser.parse(reader);
            for (Currency currency : Currency.values())
                readCurrencyExchangeRates(currency, jsonExchangeRates);
        }
    }

    public static JsonCurrencyExchangeService getInstance() throws IOException, ParseException {
        if (instance == null)
            instance = new JsonCurrencyExchangeService();

        return instance;
    }

    private void readCurrencyExchangeRates(Currency baseCurrency, JSONObject jsonExchangeRates) {
        JSONObject currencyExchangeRates = (JSONObject) jsonExchangeRates.get(baseCurrency.name());
        for (Currency targetCurrency : Currency.values()) {
            if (baseCurrency.equals(targetCurrency)) continue;
            BigDecimal exchangeRate = BigDecimal.valueOf((double) currencyExchangeRates.get(targetCurrency.name()));
            exchangeRates.get(baseCurrency).put(targetCurrency, exchangeRate);
        }
    }

    @Override
    public BigDecimal exchange(Currency baseCurrency, Currency targetCurrency, BigDecimal amount) {
        if (baseCurrency.equals(targetCurrency))
            throw new IllegalArgumentException("target currency cannot be the same as base currency");

        return amount.multiply(exchangeRates.get(baseCurrency).get(targetCurrency));
    }
}
