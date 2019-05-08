package ru.sberbank.school.task02.calculator;

import ru.sberbank.school.task02.ExtendedFxConversionService;
import ru.sberbank.school.task02.ExternalQuotesService;
import ru.sberbank.school.task02.util.Beneficiary;
import ru.sberbank.school.task02.util.ClientOperation;
import ru.sberbank.school.task02.util.Quote;
import ru.sberbank.school.task02.util.Symbol;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;

public class CurrencyCalculator implements ExtendedFxConversionService {
    private final ExternalQuotesService externalQuotesService;
    private List<Quote> quotes = new ArrayList<>();
    private BigDecimal amountOfRequest;
    private Symbol symbolOfRequest;
    private int rounding_mode;

    public CurrencyCalculator(ExternalQuotesService externalQuotesService) {
        this.externalQuotesService = externalQuotesService;
    }

    @Override
    public BigDecimal convert(@NonNull ClientOperation operation,
                              @NonNull Symbol symbol,
                              @NonNull BigDecimal amount) {
        quotes = externalQuotesService.getQuotes(Symbol.USD_RUB);
        this.amountOfRequest = amount.setScale(2, BigDecimal.ROUND_FLOOR);
        this.symbolOfRequest = symbol;
        System.out.println("Get request volume: " +  amountOfRequest );
        if (check()) {
            return operation(operation);
        }
        return new BigDecimal(0);
    }

    private BigDecimal operation(ClientOperation operation) {
        Optional<Quote> quote = findQuote(new CompareQutes());

        if (quote.isPresent() && operation == ClientOperation.SELL) {
            return quote.get().getBid();
        }
        if (quote.isPresent() && operation == ClientOperation.BUY) {
            return quote.get().getOffer();
        }
        return new BigDecimal(0);
    }

    private Optional<BigDecimal> extendedOperation(ClientOperation operation, Beneficiary beneficiary) {
        Optional<Quote> quote = findQuote(new CompareQuotesBenificiary(operation, beneficiary));
        if (!quote.isPresent()) {
            return Optional.empty();
        }
        if (operation == ClientOperation.SELL) {
            return Optional.of(BigDecimal.valueOf(1).divide(quote.get().getBid(),10,rounding_mode));
        }
        if (operation == ClientOperation.BUY) {
            return Optional.of(BigDecimal.valueOf(1).divide(quote.get().getOffer(), 10,rounding_mode));
        }
        return Optional.empty();
    }

    private Optional<Quote> findQuote(Comparator<Quote> comparator) {
        BigDecimal amountQuote;
        List<Quote> sortedQuoteList = filterQutesList(comparator);
        for (Quote quote : sortedQuoteList) {
            amountQuote = quote.getVolumeSize();
            if (amountQuote.compareTo(amountOfRequest) > 0) {
                return Optional.of(quote);
            }
            if (quote.getVolume().isInfinity()) {
                return Optional.of(quote);
            }
        }
        return Optional.empty();
    }

    private List<Quote> filterQutesList(Comparator<Quote> comparator) {
        showQuotes();
        List<Quote> filterBySymbolList = quotes.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
        return filterBySymbolList;
    }

    private boolean check() {
        if (quotes.size() == 0) {
            return false;
        }
        if (amountOfRequest.compareTo(new BigDecimal(0)) <= 0) {
            return false;
        }
        if (symbolOfRequest == null)  {
            return false;
        }
        return true;
    }

    @Override
    public Optional<BigDecimal> convertReversed(ClientOperation operation, Symbol symbol, BigDecimal amount, Beneficiary beneficiary) {
        quotes = externalQuotesService.getQuotes(Symbol.USD_RUB);
        this.symbolOfRequest = symbol;
        this.amountOfRequest = amount;

        if ((beneficiary == Beneficiary.BANK && operation == ClientOperation.BUY) ||
                (beneficiary == Beneficiary.CLIENT && operation == ClientOperation.SELL)) {
            rounding_mode = BigDecimal.ROUND_CEILING;
        }
        if ((beneficiary == Beneficiary.BANK && operation == ClientOperation.SELL) ||
                (beneficiary == Beneficiary.CLIENT && operation == ClientOperation.BUY)) {
            rounding_mode = BigDecimal.ROUND_FLOOR;
        }
        this.amountOfRequest = amount.setScale(10, BigDecimal.ROUND_FLOOR);

        if (check()) {
            return extendedOperation(operation, beneficiary);
        }
        return Optional.empty();
}

    @Override
    public Optional<BigDecimal> convertReversed(ClientOperation operation, Symbol symbol, BigDecimal amount, double delta, Beneficiary beneficiary) {
        return Optional.empty();
    }

    private void showQuotes() {
        for (Quote quote : quotes) {
            System.out.println("Get Quote symbol: " + quote.getSymbol() +
                    " volume: " + quote.getVolume() +
                    " bid: " + quote.getBid() +
                    " offer: " + quote.getOffer());
        }
    }

}
