package com.github.dfauth.ta.model.txn;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.github.dfauth.ta.model.txn.TxnEntry.TxnType.INT;
import static com.github.dfauth.ta.model.txn.TxnEntry.TxnType.PAYMENT;
import static java.math.BigDecimal.ZERO;
import static java.time.temporal.ChronoField.*;

@Slf4j
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
    private final Optional<BigDecimal> debit;
    private final Optional<BigDecimal> credit;
    private final Optional<BigDecimal> balance;
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
                .orElseGet(() -> handleNumber(s)), (b, o) -> b.debit((Optional<BigDecimal>) o)),
        CREDIT(s -> Optional.ofNullable(s)
                .filter(String::isEmpty)
                .map(_s -> Optional.empty())
                .orElseGet(() -> handleNumber(s)), (b,o) -> b.credit((Optional<BigDecimal>) o)),
        BALANCE(s -> Optional.ofNullable(s)
                .filter(String::isEmpty)
                .map(_s -> Optional.empty())
                .orElseGet(() -> handleNumber(s)), (b,o) -> b.balance((Optional<BigDecimal>) o)),
        TYPE(TxnEntry.TxnType::valueOf, (b, o) -> b.txnType((TxnEntry.TxnType) o)),
        SERIAL(v -> v, (b,o) -> b.serial(Optional.<String>ofNullable((String)o))),
        ;

        private static Optional<Object> handleNumber(String s) {
            try {
                Number number = DecimalFormat.getInstance().parse(s);;
                if(number instanceof Long) {
                    return Optional.of(BigDecimal.valueOf((long)number));
                } else if(number instanceof Integer) {
                    return Optional.of(BigDecimal.valueOf((int)number));
                } else if(number instanceof Double) {
                    return Optional.of(BigDecimal.valueOf((double)number));
                } else if(number instanceof Float) {
                    return Optional.of(BigDecimal.valueOf((float)number));
                }
                throw new IllegalArgumentException("Unexpected or unsupported type "+number);
            } catch (ParseException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }

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
        PAYMENT(TxnEntry::parsePaymentString, Payment::new), // payment
        DEP(TxnEntry::parseDepositString, Deposit::new),  // deposit
        CREDIT(TxnEntry::parseCreditString, Credit::new),  // credit
        INT(TxnEntry::parseInterestString, Interest::new), // interest
        DIV(TxnEntry::parseDividendString, DividendPayment::new), // dividend
        OTHER(TxnEntry::parseOtherString, Other::new);

        private Function<TxnEntry, Payment> f;
        private Function<Payment, ? extends Payment> f2;

        TxnType(Function<TxnEntry, Payment> f, Function<Payment, ? extends Payment> f2) {
            this.f = f;
            this.f2 = f2;
        }

        public Payment toJson(TxnEntry e) {
            return f.apply(e);
        }

        public <T extends Payment> T blah(Payment pymnt) {
            return (T) f2.apply(pymnt);
        }
    }

    private static Payment parseInterestString(TxnEntry e) {
        // GROSS INT         155.74 INC BONUS           9.35 TAX 10.00%         15.00 NET INTERES
        return new Interest(e.date, e.detail, e.credit.orElse(ZERO), e.balance.get());
    }

    private static Payment parseCreditString(TxnEntry e) {
        // DEPOSIT ONLINE 2081482 TFR Westpac Ch
        String tmp = e.detail.substring("DEPOSIT ONLINE ".length());
        String[] strings = tmp.split(" ");
        String contractNo = strings[0];
        return new Credit(e.date, e.detail, contractNo, e.credit.get(), e.balance.get());
    }

    private static Payment parseDepositString(TxnEntry e) {
        // DEPOSIT WESTPAC SECURITI        S VUL 42584340-00
        String code;
        String contractNo;
        if(e.detail.startsWith("DEPOSIT WESTPAC SECURITI        ")) {
            String tmp = e.detail.substring("DEPOSIT WESTPAC SECURITI        ".length());
            String[] strings = tmp.split(" ");
            String side = strings[0];
            code = strings[1];
            contractNo = strings[strings.length-1];
            return new Deposit(e.date, e.detail, "ASX:"+code, contractNo, e.credit.get(), e.balance.get());
        } else if(e.detail.startsWith("DEPOSIT ")) {
            // DEPOSIT ALTIUM LIMITED        SOA24/0080705
            String tmp = e.detail.substring("DEPOSIT ".length());
            String[] strings = tmp.split(" ");
            code = strings[0];
            contractNo = strings[strings.length-1];
            return new Deposit(e.date, e.detail, "ASX:"+code, contractNo, e.credit.get(), e.balance.get());
        } else {
            return new Payment(0, PAYMENT, e.date, e.detail, e.credit.get(), e.balance.get(), null, null, null);
        }
    }

    private static Payment parseOtherString(TxnEntry e) {
        // DIRECT DEBIT DISHONOURED 012384
        return new Other(e.date, e.detail, e.credit.get(), e.balance.get());
    }

    private static Payment parseDividendString(TxnEntry e) {
        // DEPOSIT DIVIDEND MQG ITM DIV 001269915175
        String tmp = e.detail.substring("DEPOSIT DIVIDEND ".length());
        String[] strings = tmp.split(" ");
        String code = strings[0];
        String contractNo = strings[strings.length-1];
        return new DividendPayment(e.date, e.detail, "ASX:"+code, contractNo, e.credit.get(), e.balance.get());
    }

    public static Payment parsePaymentString(TxnEntry e) {
        Payment result;
        // WITHDRAWAL ONLINE 1934011 TFR Westpac Cho renovation fund
        if(e.detail.startsWith("WITHDRAWAL ONLINE")) {
            result = new Payment(0, TxnType.PAYMENT, e.date,e.detail, e.debit.get(), e.balance.get(), null, null, null);
        } else {
            // PAYMENT BY AUTHORITY TO WESTPAC SECURITI B DUR 42855945-0
            String tmp = e.detail.substring(PREAMBLE_LENGTH);
            if(tmp.startsWith("Westpac Securitie")) {
                // some other payment type
                result = new Payment(0, TxnType.PAYMENT, e.date,e.detail, e.debit.get(), e.balance.get(), null, null, null);
            } else {
                String[] strings = tmp.split(" ");
                Side side = Side.valueOf(strings[0]);
                String code = strings[1];
                String contractNo = strings[2].split("\\-")[0];
                result = new SecuritiesPurchase(e.date, e.detail, side, "ASX:"+code, contractNo, e.debit.get(), e.balance.get());
            }
        }
        return result;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @EqualsAndHashCode
    @Data
    @Entity
    @Table(name = "PAYMENT")
    public static class Payment {
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        private long id;
        @Column(name = "TYPE")
        private TxnType txnType;
        private LocalDate date;
        private String detail;
        private BigDecimal value;
        private BigDecimal balance;
        private Side side;
        private String code;
        @Column(name = "CONTRACTNO")
        private String contractNo;

        public Payment(Payment p) {
            this(0, p.txnType, p.date, p.detail, p.value, p.balance, p.side, p.code, p.contractNo);
        }

        public boolean isDividendPayment() {
            return false;
        }
    }

    @ToString
    @EqualsAndHashCode
    @Getter
    public static class Interest extends Payment {

        public Interest(Payment p) {
            this(p.date, p.detail, p.value, p.balance);
        }

        public Interest(LocalDate date, String detail, BigDecimal value, BigDecimal balance) {
            super(0, INT, date, detail, value, balance, null, null, null);
        }
    }

    @ToString
    @EqualsAndHashCode
    @Getter
    public static class Credit extends Payment {

        public Credit(Payment p) {
            this(p.date, p.detail, p.contractNo, p.value, p.balance);
        }

        public Credit(LocalDate date, String detail, String contractNo, BigDecimal value, BigDecimal balance) {
            super(0, TxnType.CREDIT, date, detail, value, balance, null, null, contractNo);
        }
    }

    @ToString
    @EqualsAndHashCode
    @Getter
    public static class Other extends Payment {

        public Other(Payment p) {
            this(p.date, p.detail, p.value, p.balance);
        }

        public Other(LocalDate date, String detail, BigDecimal value, BigDecimal balance) {
            super(0, TxnType.OTHER, date, detail, value, balance, null, null, null);
        }
    }

    @ToString
    @EqualsAndHashCode
    @Getter
    public static class Deposit extends Payment {

        public Deposit(Payment p) {
            this(p.date, p.detail, p.code, p.contractNo, p.value, p.balance);
        }

        public Deposit(LocalDate date, String detail, String code, String contractNo, BigDecimal value, BigDecimal balance) {
            super(0, TxnType.DEP, date, detail, value, balance, null, code, contractNo);
        }
    }

    @ToString
    @EqualsAndHashCode
    @Getter
    public static class SecuritiesPurchase extends Payment {

        public SecuritiesPurchase(Payment p) {
            this(p.date, p.detail, p.side, p.code, p.contractNo, p.value, p.balance);
        }

        public SecuritiesPurchase(LocalDate date, String detail, Side side, String code, String contractNo, BigDecimal value, BigDecimal balance) {
            super(0, TxnType.PAYMENT, date, detail, value, balance, side, code, contractNo);
        }
    }

    @ToString
    @EqualsAndHashCode
    @Getter
    public static class DividendPayment extends Payment {

        public DividendPayment(Payment p) {
            this(p.date, p.detail, p.code, p.contractNo, p.value, p.balance);
        }

        public DividendPayment(LocalDate date, String detail, String code, String contractNo, BigDecimal value, BigDecimal balance) {
            super(0, TxnType.DIV, date, detail, value, balance, null, code, contractNo);
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
