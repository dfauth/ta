package com.github.dfauth.ta.model.txn;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dfauth.ta.model.Side;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.github.dfauth.ta.model.txn.TxnType.validateCode;
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
    @JsonIgnore
    private String contractNo;
    private LocalDate exDividendDate;

    public Payment(Payment p) {
        this(0, p.txnType, p.date, p.detail, p.value, p.balance, p.side, p.code, p.contractNo, p.exDividendDate);
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
            if(code != null) {
                assert(validateCode(code));
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
        public String code;
        public LocalDate exDividendDate;

        public Payment toPayment() {
            return txnType.toJson(this);
        }

        public PaymentFactory guessTxnType() {
            if(detail.startsWith("Payment")) {
                txnType = TxnType.PAYMENT;
                credit = null;
            } else if(detail.startsWith("Deposit Dividend") || (detail.toUpperCase().startsWith("DEPOSIT") && detail.toUpperCase().contains("DIVIDEND")) || detail.startsWith("Deposit-Debenture/Note")
                    || detail.startsWith("Deposit G8 Education Lim")   // special cases
                    || detail.startsWith("Deposit Bigair Group Ltd")
                    || detail.startsWith("Deposit Jbh Payment")
                    || detail.startsWith("Deposit Intecq Limited")
                    || detail.startsWith("Deposit Bwx Ret Prem")
                    || detail.startsWith("Deposit Byron Bay NSW")
                    || detail.startsWith("Deposit Zenitas Healthca")
                    || detail.startsWith("Deposit Onemarket Ltd")
                    || detail.startsWith("Deposit Aurelia Metals")
                    || detail.startsWith("Deposit Freedom Ins")
                    || detail.startsWith("Deposit Org Ret Premium"))
            {
                txnType = TxnType.DIV;
                debit = null;
            } else if(detail.toUpperCase().startsWith("DEPOSIT") && detail.toUpperCase().contains("ONLINE")) {
                txnType = TxnType.CREDIT;
                debit = null;
            } else if(detail.startsWith("Deposit Westpac Securiti")
                    || detail.startsWith("Deposit Warringah Mall")
                    || detail.startsWith("Deposit Slcsoa")
                    || detail.startsWith("Deposit Westfield Corp")
            ) {
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
