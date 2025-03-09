package com.github.dfauth.ta.model.txn;

import com.github.dfauth.ta.model.Side;

import java.util.function.Function;

public enum TxnType {
    PAYMENT(TxnType::parsePaymentString), // payment
    DEP(TxnType::parseDepositString),  // deposit
    CREDIT(TxnType::parseCreditString),  // credit
    INT(TxnType::parseInterestString), // interest
    DIV(TxnType::parseDividendString), // dividend
    OTHER(TxnType::parseOtherString);

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
        return new Payment(0l, INT, e.date, e.detail, e.credit, e.balance, null, null, null);
    }

    private static Payment parseCreditString(Payment.PaymentFactory e) {
        // DEPOSIT ONLINE 2081482 TFR Westpac Ch
        String tmp = e.detail.substring("DEPOSIT ONLINE ".length());
        String[] strings = tmp.split(" ");
        String contractNo = strings[0];
        return new Payment(0l, CREDIT, e.date, e.detail, e.credit, e.balance, null, null, contractNo);
    }

    private static Payment parseDepositString(Payment.PaymentFactory e) {
        // DEPOSIT WESTPAC SECURITI        S VUL 42584340-00
        String code;
        String contractNo;
        if(e.detail.startsWith("DEPOSIT WESTPAC SECURITI        ")) {
            String tmp = e.detail.substring("DEPOSIT WESTPAC SECURITI        ".length());
            String[] strings = tmp.split(" ");
            String side = strings[0];
            code = strings[1];
            contractNo = strings[strings.length-1];
            return new Payment(0l, DEP, e.date, e.detail, e.credit, e.balance, null, "ASX:"+code, contractNo);
        } else if(e.detail.startsWith("DEPOSIT ")) {
            // DEPOSIT ALTIUM LIMITED        SOA24/0080705
            String tmp = e.detail.substring("DEPOSIT ".length());
            String[] strings = tmp.split(" ");
            code = strings[0];
            contractNo = strings[strings.length-1];
            return new Payment(0l, DEP, e.date, e.detail, e.credit, e.balance, null, "ASX:"+code, contractNo);
        } else {
            return new Payment(0, PAYMENT, e.date, e.detail, e.credit, e.balance, null, null, null);
        }
    }

    private static Payment parseOtherString(Payment.PaymentFactory e) {
        // DIRECT DEBIT DISHONOURED 012384
        return new Payment(0l, OTHER, e.date, e.detail, e.credit, e.balance, null, null, null);
    }

    private static Payment parseDividendString(Payment.PaymentFactory e) {
        // DEPOSIT DIVIDEND MQG ITM DIV 001269915175
        String tmp = e.detail.substring("DEPOSIT DIVIDEND ".length());
        String[] strings = tmp.split(" ");
        String code = strings[0];
        String contractNo = strings[strings.length-1];
        return new Payment(0l, DIV, e.date, e.detail, e.credit, e.balance, null, "ASX:"+code, contractNo);
    }

    public static Payment parsePaymentString(Payment.PaymentFactory e) {
        Payment result;
        // WITHDRAWAL ONLINE 1934011 TFR Westpac Cho renovation fund
        if(e.detail.startsWith("WITHDRAWAL ONLINE")) {
            result = new Payment(0, TxnType.PAYMENT, e.date,e.detail, e.debit, e.balance, null, null, null);
        } else {
            // PAYMENT BY AUTHORITY TO WESTPAC SECURITI B DUR 42855945-0
            String tmp = e.detail.substring(PREAMBLE_LENGTH);
            if(tmp.startsWith("Westpac Securitie")) {
                // some other payment type
                result = new Payment(0, TxnType.PAYMENT, e.date,e.detail, e.debit, e.balance, null, null, null);
            } else {
                String[] strings = tmp.split(" ");
                Side side = Side.fromString(strings[0]);
                String code = strings[1];
                String contractNo = strings[2].split("\\-")[0];
                result = new Payment(0l, PAYMENT, e.date, e.detail, e.debit, e.balance,side, "ASX:"+code, contractNo);
            }
        }
        return result;
    }
}
