package ru.sberbank.school.task02.calculator;

import ru.sberbank.school.task02.ExtendedFxConversionService;
import ru.sberbank.school.task02.ExternalQuotesService;

public class ExtendedCurrencyCalculator extends CurrencyCalculator implements ExtendedFxConversionService {
    public ExtendedCurrencyCalculator(ExternalQuotesService externalQuotesService) {
        super(externalQuotesService);
    }
}
