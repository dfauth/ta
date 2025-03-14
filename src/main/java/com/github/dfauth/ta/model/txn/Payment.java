package com.github.dfauth.ta.model.txn;

import com.github.dfauth.ta.model.Side;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import static io.github.dfauth.trycatch.ExceptionalRunnable.tryCatch;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Data
@Entity
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    public static PaymentFactory.PaymentFactoryBuilder builder() {
        return PaymentFactory.builder();
    }

    public Payment sanityCheck() {
        return tryCatch(() -> {
            assert(txnType != null);
            assert(date != null);
            assert(detail != null);
            assert(value != null);
            assert(balance != null);
            if(txnType.isPayment()) {
//                assert(side != null);
//                assert(code.length() == 7);
//                assert(contractNo != null);
            }
            return this;
        },e -> this);
    }

    public Payment validate(Payment next) {
        if(this.balance.add(next.txnType.apply(next.value)).equals(next.balance)) {
            return next;
        } else{
            throw new ArithmeticException("transaction: "+next+" does not equal expected value "+this.balance.add(next.txnType.apply(next.value))+" based on "+this);
        }
    }

    @Builder
    public static class PaymentFactory {
        public TxnType txnType;
        public LocalDate date;
        public String detail;
        public BigDecimal debit;
        public BigDecimal credit;
        public BigDecimal balance;
        public String contractNo;

        public Payment toPayment() {
            return txnType.toJson(this);
        }

        public PaymentFactory guessTxnType() {
            if(detail.startsWith("Payment")) {
                txnType = TxnType.PAYMENT;
                credit = null;
            } else if(detail.startsWith("Deposit Dividend") || detail.startsWith("Deposit-Debenture/Note")) {
                txnType = TxnType.DIV;
                debit = null;
            } else if(detail.startsWith("Deposit")) {
                txnType = TxnType.DEP;
                debit = null;
            } else if(detail.startsWith("Gross Int") || detail.startsWith("Interest Paid")) {
                txnType = TxnType.INT;
                debit = null;
            } else {
                throw new IllegalArgumentException("Unsupported payment type: "+detail);
            }
            return this;
        }
    }
}
