package com.rynkow.bankingsystem.service;

import com.rynkow.bankingsystem.model.Currency;

import java.math.BigDecimal;

public interface CurrencyExchangeService {
    BigDecimal exchange(Currency baseCurrency, Currency targetCurrency, BigDecimal amount);
}
