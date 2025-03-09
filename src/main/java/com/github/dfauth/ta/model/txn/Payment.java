package com.github.dfauth.ta.model.txn;

import com.github.dfauth.ta.model.Side;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

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
    }
}
