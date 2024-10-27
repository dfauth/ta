package com.github.dfauth.ta.model.txn;

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
@Table(name = "PAYMENT")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "TYPE")
    private TxnEntry.TxnType txnType;
    private LocalDate date;
    private String detail;
    private BigDecimal value;
    private BigDecimal balance;
    private TxnEntry.Side side;
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
