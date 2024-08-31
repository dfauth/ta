package com.github.dfauth.util;

import lombok.*;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static io.github.dfauth.trycatch.ExceptionalRunnable.tryCatch;
import static java.time.temporal.ChronoField.*;

@Builder
@ToString
@EqualsAndHashCode
public class TxnEntry {

    private static final String PREAMBLE = "PAYMENT BY AUTHORITY TO WESTPAC SECURITI ";
    private static final int PREAMBLE_LENGTH = PREAMBLE.length();
    //Bank Account,Date,Narrative,Debit Amount,Credit Amount,Balance,Categories,Serial
    // d,i,dt,db,c,b
    private final String accountId;
    private final LocalDate date;
    private final String detail;
    private final Optional<Number> debit;
    private final Optional<Number> credit;
    private final Optional<Number> balance;
//    private final String id;
    private final TxnType txnType;
    private final Optional<String> serial;

    private static DateTimeFormatter DTF = new DateTimeFormatterBuilder()
            .appendValue(DAY_OF_MONTH, 1, 2, SignStyle.NEVER)
            .appendLiteral('/')
            .appendValue(MONTH_OF_YEAR, 1, 2, SignStyle.NEVER)
            .appendLiteral('/')
            .appendValue(YEAR, 4)
            .toFormatter();

    public enum FieldHandler implements BiConsumer<Accumulator, String> {

        ACCOUNT_ID(v -> v, (b,o) -> b.accountId((String)o)),
        DATE(DTF::parse, (b,o) -> b.date(LocalDate.from((TemporalAccessor)o))),
//        ID(v -> v, (b,o) -> b.id((String)o)),
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
                .orElseGet(() -> tryCatch(() -> Optional.of(NumberFormat.getInstance().parse(s)))), (b,o) -> b.balance((Optional<Number>) o)),
        TYPE(TxnEntry.TxnType::valueOf, (b, o) -> b.txnType((TxnEntry.TxnType) o)),
        SERIAL(v -> v, (b,o) -> b.serial(Optional.<String>ofNullable((String)o))),
        ;

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

        private TxnEntry txn;
        private TxnEntryBuilder builder = new TxnEntryBuilder();

        public void accept(FieldHandler fieldHandler, String s) {
            fieldHandler.handle(builder, s);
            if(fieldHandler.ordinal() == FieldHandler.values().length-1) {
                txn = builder.build();
                builder = new TxnEntryBuilder();
            }
        }

        public TxnEntry finish() {
            return txn;
        }

        public Payment instead() {
            return txn.txnType.toJson(txn);
        }
    }

    public enum TxnType {
        PAYMENT(TxnEntry::parsePaymentString), // payment
        DEP,  // deposit
        CREDIT,  // credit
        INT, // interest
        DIV((TxnEntry::parseDividendString)), // dividend
        OTHER;

        private Function<TxnEntry, Payment> f;

        TxnType(Function<TxnEntry, Payment> f) {
            this.f = f;
        }

        TxnType() {
            this(e -> new Payment(null, null, null));
        }

        public Payment toJson(TxnEntry e) {
            return f.apply(e);
        }
    }

    private static Payment parseDividendString(TxnEntry e) {
        // DEPOSIT DIVIDEND MQG ITM DIV 001269915175
        String tmp = e.detail.substring("DEPOSIT DIVIDEND ".length());
        String[] strings = tmp.split(" ");
        String code = strings[0];
        String contractNo = strings[strings.length-1];
        return new DividendPayment(e.date, code, contractNo, e.credit.get(), e.balance.get());
    }

    public static Payment parsePaymentString(TxnEntry e) {
        // PAYMENT BY AUTHORITY TO WESTPAC SECURITI B DUR 42855945-0
        String tmp = e.detail.substring(PREAMBLE_LENGTH);
        Payment result;
        if(tmp.startsWith("Westpac Securitie")) {
            // some other payment type
            result = new Payment(e.date,e.debit.get(), e.balance.get());
        } else {
            String[] strings = tmp.split(" ");
            Side side = Side.valueOf(strings[0]);
            String code = strings[1];
            String contractNo = strings[2].split("\\-")[0];
            result = new SecuritiesPurchase(e.date, side, code, contractNo, e.debit.get(), e.balance.get());
        }
        return result;
    }

    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    @Getter
    public static class Payment {
        private final LocalDate date;
        private final Number value;
        private final Number balance;

        public boolean isDividendPayment() {
            return false;
        }
    }

    @ToString
    @EqualsAndHashCode
    @Getter
    public static class SecuritiesPurchase extends Payment {
        private final Side side;
        private final String code;
        private final String contractNo;

        public SecuritiesPurchase(LocalDate date, Side side, String code, String contractNo, Number value, Number balance) {
            super(date, value, balance);
            this.side = side;
            this.code = code;
            this.contractNo = contractNo;
        }
    }

    @ToString
    @EqualsAndHashCode
    @Getter
    public static class DividendPayment extends Payment {
        private final String code;
        private final String contractNo;

        public DividendPayment(LocalDate date, String code, String contractNo, Number value, Number balance) {
            super(date, value, balance);
            this.code = code;
            this.contractNo = contractNo;
        }

        @Override
        public boolean isDividendPayment() {
            return true;
        }
    }

    enum Side {
        B, S;
    }
}
