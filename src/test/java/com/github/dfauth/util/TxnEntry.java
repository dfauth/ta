package com.github.dfauth.util;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static io.github.dfauth.trycatch.ExceptionalRunnable.tryCatch;
import static java.time.temporal.ChronoField.*;

@Builder
@ToString
@EqualsAndHashCode
public class TxnEntry {

    // d,i,dt,db,c,b
    private final LocalDate date;
    private final String id;
    private final TxnType txnType;
    private final String detail;
    private final Optional<Number> debit;
    private final Optional<Number> credit;
    private final Optional<Number> balance;

    private static DateTimeFormatter DTF = new DateTimeFormatterBuilder()
            .appendValue(DAY_OF_MONTH, 1, 2, SignStyle.NEVER)
            .appendLiteral('/')
            .appendValue(MONTH_OF_YEAR, 1, 2, SignStyle.NEVER)
            .appendLiteral('/')
            .appendValue(YEAR, 4)
            .toFormatter();

    public enum FieldHandler implements BiConsumer<Accumulator, String> {

        DATE(DTF::parse, (b,o) -> b.date(LocalDate.from((TemporalAccessor)o))),
        ID(v -> v, (b,o) -> b.id((String)o)),
        TYPE(TxnEntry.TxnType::valueOf, (b, o) -> b.txnType((TxnEntry.TxnType) o)),
        DETAIL(v -> v, (b,o) -> b.detail((String)o)),
        DEBIT(s -> Optional.ofNullable(s)
                .filter(String::isEmpty)
                .map(_s -> Optional.empty())
                .orElseGet(() -> tryCatch(() -> Optional.of(NumberFormat.getInstance().parse(s)))), (b, o) -> b.debit((Optional<Number>) o)),
        CREDIT(s -> Optional.ofNullable(s)
                .filter(String::isEmpty)
                .map(_s -> Optional.empty())
                .orElseGet(() -> tryCatch(() -> Optional.of(NumberFormat.getInstance().parse(s)))), (b,o) -> b.credit((Optional<Number>) o)),
        BALANCE(s -> Optional.ofNullable(s)
                .filter(String::isEmpty)
                .map(_s -> Optional.empty())
                .orElseGet(() -> tryCatch(() -> Optional.of(NumberFormat.getInstance().parse(s)))), (b,o) -> b.balance((Optional<Number>) o));
        private Function<String, ?> converter;
        private BiConsumer<TxnEntry.TxnEntryBuilder, Object> consumer;

        FieldHandler(Function<String, Object> converter, BiConsumer<TxnEntry.TxnEntryBuilder, Object> consumer) {
            this.converter = converter;
            this.consumer = consumer;
        }

        public void accept(TxnEntry.Accumulator accumulator, String s) {
            accumulator.accept(this, s);
        }

        public void handle(TxnEntry.TxnEntryBuilder builder, String s) {
            consumer.accept(builder, converter.apply(s));
        }
    }

    public static class Accumulator {

        private List<TxnEntry> txns = new ArrayList<>();
        private TxnEntryBuilder builder = new TxnEntryBuilder();

        public void accept(FieldHandler fieldHandler, String s) {
            fieldHandler.handle(builder, s);
            if(fieldHandler.ordinal() == FieldHandler.values().length-1) {
                txns.add(builder.build());
                builder = new TxnEntryBuilder();
            }
        }

        public List<TxnEntry> finish() {
            return txns;
        }
    }

    public enum TxnType {
        Contract,
        Receipt,
        Payment,
        Journal;
    }

}
