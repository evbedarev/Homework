package ru.sberbank.school.task02;

import lombok.NonNull;
import ru.sberbank.school.task02.util.Beneficiary;
import ru.sberbank.school.task02.util.ClientOperation;
import ru.sberbank.school.task02.util.Quote;
import ru.sberbank.school.task02.util.Symbol;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

public class ExConverter extends Converter implements ExtendedFxConversionService {
    private ExternalQuotesService service;

    public ExConverter(ExternalQuotesService service) {
        super(service);
        this.service = service;
    }

    @Override
    public Optional<BigDecimal> convertReversed(@NonNull ClientOperation operation,
                                                @NonNull Symbol symbol,
                                                @NonNull BigDecimal amount,
                                                @NonNull Beneficiary beneficiary) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException();
        }

        List<Quote> quotes = service.getQuotes(symbol);
        if (quotes.isEmpty()) {
            return Optional.empty();
        }
        BigDecimal tempAmount;
        Quote quote = null;

        for (Quote q : quotes) {
            if (operation == ClientOperation.BUY) {
                tempAmount = amount.divide(q.getOffer(), RoundingMode.HALF_UP);
            } else {
                tempAmount = amount.divide(q.getBid(), RoundingMode.HALF_UP);
            }

            if (!q.isInfinity() && quoteIsMatch(q, tempAmount)) {
                quote = chooseQuote(quote, q, operation, beneficiary);
            }
        }

        if (quote != null) {
            return operation == ClientOperation.BUY
                    ? Optional.of(BigDecimal.ONE.divide(quote.getOffer(),
                                                        10,
                                                        RoundingMode.HALF_UP))
                    : Optional.of(BigDecimal.ONE.divide(quote.getBid(),
                                                        10,
                                                        RoundingMode.HALF_UP));
        } else {
            return Optional.empty();
        }
    }

    private boolean quoteIsMatch(Quote quote, BigDecimal amount) {
        return (quote.getVolumeSize().subtract(amount).compareTo(BigDecimal.ZERO) > 0);
    }

    private Quote chooseQuote(Quote q1, Quote q2, ClientOperation operation, Beneficiary beneficiary) {
        if (q1 == null && q2 == null) {
            throw new IllegalArgumentException();
        }
        if (q1 == null) {
            return q2;
        }
        if (q2 == null) {
            return q1;
        }

        if (beneficiary == Beneficiary.CLIENT) {
            if (operation == ClientOperation.BUY) {
                return q1.getOffer().compareTo(q2.getOffer()) < 0 ? q1 : q2;
            } else {
                return q1.getBid().compareTo(q2.getBid()) < 0 ? q2 : q1;
            }
        } else {
            if (operation == ClientOperation.BUY) {
                return q1.getOffer().compareTo(q2.getOffer()) < 0 ? q2 : q1;
            } else {
                return q1.getBid().compareTo(q2.getBid()) < 0 ? q1 : q2;
            }
        }
    }
}
