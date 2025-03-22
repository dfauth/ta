package com.github.dfauth.ta.model.txn;

import com.github.dfauth.ta.model.Side;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.function.Predicate.not;

public enum TxnType implements UnaryOperator<BigDecimal>, Predicate<Payment> {
    PAYMENT(TxnType::parsePaymentString), // payment
    DEP(TxnType::parseDepositString),  // deposit
    CREDIT(TxnType::parseCreditString),  // credit
    INT(TxnType::parseInterestString), // interest
    DIV(TxnType::parseDividendString), // dividend
    OTHER(TxnType::parseOtherString);

    private static Pattern ASXCODE = Pattern.compile("^ASX\\:([A-Z0-9]{3})$");

    public static String lookupCode(String asxcode) {
        return asxcode;
    }

    public static boolean validateCode(String asxcode) {
        return Optional.ofNullable(asxcode).map(ASXCODE::matcher).filter(Matcher::matches).isPresent();
    }

    private static final BigDecimal MINUS_ONE = BigDecimal.valueOf(-1);
    private Function<Payment.PaymentFactory, Payment> f;

    TxnType(Function<Payment.PaymentFactory, Payment> f) {
        this.f = f;
    }

    public Payment toJson(Payment.PaymentFactory e) {
        return f.apply(e);
    }

    public boolean isDividend() {
        return this == DIV;
    }

    private static final String PREAMBLE = "PAYMENT BY AUTHORITY TO WESTPAC SECURITI ";
    private static final int PREAMBLE_LENGTH = PREAMBLE.length();
    private static Payment parseInterestString(Payment.PaymentFactory e) {
        // GROSS INT         155.74 INC BONUS           9.35 TAX 10.00%         15.00 NET INTERES
        return new Payment(0l, INT, e.date, e.detail, e.credit, e.balance, null, null, null, null);
    }

    private static Payment parseCreditString(Payment.PaymentFactory e) {
        if(e.detail.toUpperCase().startsWith("DEPOSIT ONLINE ")) {
            // DEPOSIT ONLINE 2081482 TFR Westpac Ch
            String tmp = e.detail.substring("DEPOSIT ONLINE ".length());
            String[] strings = tmp.split(" ");
            String contractNo = strings[0];
            return new Payment(0l, CREDIT, e.date, e.detail, e.credit, e.balance, null, null, contractNo, null);
        } else if(e.detail.startsWith("Deposit - Internet Online Banking")) {
            // Deposit - Internet Online Banking 2912267  Fnds Tfr 04-Dec
            return new Payment(0l, CREDIT, e.date, e.detail, e.credit, e.balance, null, null, null, null);
        } else {
            throw new IllegalArgumentException("Unknown or unsupportedf credit type: "+e.detail);
        }
    }

    private static Payment parseDepositString(Payment.PaymentFactory e) {
        // DEPOSIT WESTPAC SECURITI        S VUL 42584340-00
        String code;
        String contractNo;
        if(e.detail.toUpperCase().startsWith("DEPOSIT WESTPAC SECURITI")) {
            String tmp = e.detail.toUpperCase().substring("DEPOSIT WESTPAC SECURITI".length());
            String[] strings = Arrays.stream(tmp.trim().split(" ")).filter(not(""::equals)).toArray(String[]::new);
            Side side = Side.fromString(strings[0]);
            code = strings[1];
            contractNo = strings[strings.length-1];
            return new Payment(0l, DEP, e.date, e.detail, e.credit, e.balance, side, Optional.ofNullable(e.code).orElseGet(() -> lookupCode("ASX:"+code.toUpperCase())), contractNo, null);
        } else if(e.detail.toUpperCase().startsWith("DEPOSIT ")) {
            // DEPOSIT ALTIUM LIMITED        SOA24/0080705
            String tmp = e.detail.substring("DEPOSIT ".length());
            String[] strings = tmp.split(" ");
            code = strings[0];
            contractNo = strings[strings.length-1];
            return new Payment(0l, DEP, e.date, e.detail, e.credit, e.balance, null, Optional.ofNullable(e.code).orElseGet(() -> lookupCode("ASX:"+code.toUpperCase())), contractNo, null);
        } else {
            return new Payment(0, PAYMENT, e.date, e.detail, e.credit, e.balance, null, null, null, null);
        }
    }

    private static Payment parseOtherString(Payment.PaymentFactory e) {
        // DIRECT DEBIT DISHONOURED 012384
        return new Payment(0l, OTHER, e.date, e.detail, e.credit, e.balance, null, null, null, null);
    }

    private static Payment parseDividendString(Payment.PaymentFactory e) {
        if(e.detail.toUpperCase().startsWith("DEPOSIT DIVIDEND")) {
            // DEPOSIT DIVIDEND MQG ITM DIV 001269915175
            String tmp = e.detail.substring("DEPOSIT DIVIDEND ".length());
            String[] strings = tmp.split(" ");
            String code = strings[0];
            String contractNo = strings[strings.length-1];
            return new Payment(0l, DIV, e.date, e.detail, e.credit, e.balance, null, Optional.ofNullable(e.code).orElseGet(() -> lookupCode("ASX:"+code.toUpperCase())), contractNo, e.exDividendDate);
        } else {
            // Deposit-Debenture/Note Interest Vgb Payment  Jan15/00800426
            String[] strings = Arrays.stream(e.detail.replace("/", " ").split(" ")).filter(not(""::equals)).toArray(String[]::new);
            String code = strings[3];
            String contractNo = strings[strings.length-1];
            return new Payment(0l, DIV, e.date, e.detail, e.credit, e.balance, null, Optional.ofNullable(e.code).orElseGet(() -> lookupCode("ASX:"+code.toUpperCase())), contractNo, e.exDividendDate);
        }
    }

    public static Payment parsePaymentString(Payment.PaymentFactory e) {
        Payment result;
        // WITHDRAWAL ONLINE 1934011 TFR Westpac Cho renovation fund
        if(e.detail.startsWith("WITHDRAWAL ONLINE")) {
            result = new Payment(0, TxnType.PAYMENT, e.date,e.detail, e.debit, e.balance, null, null, null, null);
        } else {
            // PAYMENT BY AUTHORITY TO WESTPAC SECURITI B DUR 42855945-0
            String tmp = e.detail.substring(PREAMBLE_LENGTH);
            if(tmp.startsWith("Westpac Securitie")) {
                // some other payment type
                result = new Payment(0, TxnType.PAYMENT, e.date,e.detail, e.debit, e.balance, null, null, null, null);
            } else {
                String[] strings = Arrays.stream(tmp.split(" ")).filter(not(""::equals)).toArray(String[]::new);
                Side side = Side.fromString(strings[0]);
                String code = strings[1];
                String contractNo = strings[2].split("\\-")[0];
                result = new Payment(0l, PAYMENT, e.date, e.detail, e.debit, e.balance,side, Optional.ofNullable(e.code).orElseGet(() -> lookupCode("ASX:"+code.toUpperCase())), contractNo, null);
            }
        }
        return result;
    }

    public boolean isCredit() {
        return !isDebit();
    }

    public boolean isDebit() {
        return this == PAYMENT;
    }

    public boolean isPayment() {
        return this == PAYMENT;
    }

    @Override
    public BigDecimal apply(BigDecimal bd) {
        return isCredit() ? bd : bd.multiply(MINUS_ONE);
    }

    @Override
    public boolean test(Payment payment) {
        return this == payment.getTxnType();
    }
}
