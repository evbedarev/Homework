package ru.sberbank.school.task02;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.sberbank.school.task02.calculator.CustomExternalQutesService;
import ru.sberbank.school.task02.calculator.ExtendedCurrencyCalculator;
import ru.sberbank.school.task02.util.Beneficiary;
import ru.sberbank.school.task02.util.ClientOperation;
import ru.sberbank.school.task02.util.Symbol;

import java.math.BigDecimal;
import java.util.Optional;

public class TestExtendedCalculator {
    ExtendedFxConversionService conversionService;
    @Before
    public void beforeAll() {
        ExternalQuotesService qutesService = new CustomExternalQutesService();
        conversionService = new ExtendedCurrencyCalculator(qutesService);
    }

    @Test
    public void testBenificiary() {
        Optional<BigDecimal> sum = conversionService.convertReversed(ClientOperation.BUY, Symbol.USD_RUB, BigDecimal.valueOf(99), Beneficiary.BANK);
        System.out.println(sum.get());
        Assert.assertTrue(sum.get().compareTo(BigDecimal.valueOf(0.0114942529)) == 0);
        sum = conversionService.convertReversed(ClientOperation.BUY, Symbol.USD_RUB, BigDecimal.valueOf(99), Beneficiary.CLIENT);
        System.out.println(sum.get());
        Assert.assertTrue(sum.get().compareTo(BigDecimal.valueOf(0.0121951219)) == 0);



    }

}
