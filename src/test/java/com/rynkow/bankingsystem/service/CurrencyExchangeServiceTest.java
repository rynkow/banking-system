package com.rynkow.bankingsystem.service;

import com.rynkow.bankingsystem.model.Currency;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class CurrencyExchangeServiceTest {

    @Test
    void ShouldParseFileWithoutErrors() {
        // when getting service instance for the first time
        // then no exceptions are thrown
        assertDoesNotThrow(CurrencyExchangeService::getInstance);
    }

    @Test
    void ShouldBeSingleton() throws IOException, ParseException {
        // when getting an instance multiple times
        CurrencyExchangeService instance1 = CurrencyExchangeService.getInstance();
        CurrencyExchangeService instance2 = CurrencyExchangeService.getInstance();

        // then same instance is returned
        assertSame(instance1, instance2);
    }

    @Test
    void ShouldNotExchangeCurrencyForItself() throws IOException, ParseException {
        // given currency exchange service
        CurrencyExchangeService exchangeService = CurrencyExchangeService.getInstance();

        // when trying to exchange currency for itself
        // then exception is thrown
        assertThrows(IllegalArgumentException.class, () -> exchangeService.exchange(Currency.PLN, Currency.PLN, BigDecimal.valueOf(123)));
    }

    @Test
    void ShouldExchangeCurrenciesCorrectly() throws IOException, ParseException {
        // given currency exchange service
        CurrencyExchangeService exchangeService = CurrencyExchangeService.getInstance();

        // when exchanging currencies
        BigDecimal plnToEur = exchangeService.exchange(Currency.PLN, Currency.EUR, BigDecimal.valueOf(100));
        BigDecimal plnToUsd = exchangeService.exchange(Currency.PLN, Currency.USD, BigDecimal.valueOf(362));

        BigDecimal eurToPln = exchangeService.exchange(Currency.EUR, Currency.PLN, BigDecimal.valueOf(124));
        BigDecimal eurToUsd = exchangeService.exchange(Currency.EUR, Currency.USD, BigDecimal.valueOf(567));

        BigDecimal usdToPln = exchangeService.exchange(Currency.USD, Currency.PLN, BigDecimal.valueOf(795));
        BigDecimal usdToEur = exchangeService.exchange(Currency.USD, Currency.EUR, BigDecimal.valueOf(611));

        // then received values are calculated correctly
        assertEquals(0, plnToEur.compareTo(BigDecimal.valueOf(100).multiply(BigDecimal.valueOf(0.22))));
        assertEquals(0, plnToUsd.compareTo(BigDecimal.valueOf(362).multiply(BigDecimal.valueOf(0.23))));

        assertEquals(0, eurToPln.compareTo(BigDecimal.valueOf(124).multiply(BigDecimal.valueOf(4.58))));
        assertEquals(0, eurToUsd.compareTo(BigDecimal.valueOf(567).multiply(BigDecimal.valueOf(1.07))));

        assertEquals(0, usdToPln.compareTo(BigDecimal.valueOf(795).multiply(BigDecimal.valueOf(4.26))));
        assertEquals(0, usdToEur.compareTo(BigDecimal.valueOf(611).multiply(BigDecimal.valueOf(0.93))));
    }
}
