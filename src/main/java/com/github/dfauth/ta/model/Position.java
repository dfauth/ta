package com.github.dfauth.ta.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.dfauth.ta.functional.Lists;
import com.github.dfauth.ta.functions.CAGR;
import com.github.dfauth.ta.model.txn.Payment;
import com.github.dfauth.ta.model.txn.TxnType;
import com.github.dfauth.ta.util.BigDecimalOps;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.OneToMany;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.github.dfauth.ta.functional.Collectors.oops;
import static com.github.dfauth.ta.functional.Optionals.bothPresent;
import static com.github.dfauth.ta.functions.CAGR.bdMapper;
import static com.github.dfauth.ta.util.Utils.thenThrow;
import static java.math.BigDecimal.ZERO;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;

@Slf4j
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@IdClass(CodeDateCompositeKey.class)
public class Position {

    public static Predicate<Double> nonNaN = d -> !Double.isNaN(d);
    public static Predicate<Double> nonZero = d -> d != 0;
    public static Optional<Double> nonZero(double d) {
        return d == 0 ? empty() : Optional.of(d);
    };
    public static final BiFunction<BigDecimal, BigDecimal, BigDecimal> profit =  (pv, sv) -> sv.subtract(pv);
    public static final BiFunction<Long, Integer, Optional<Double>> periods = (wht, up) -> nonZero(((double)wht)/(up * 365L));
    public static final BiFunction<Double, Double, Optional<Double>> cagr = (r,p) -> CAGR.cagr(r,p,bdMapper(3)).map(BigDecimal::doubleValue);

    @Id @JsonIgnore
    private Timestamp date;
    @Id
    private String code;
    @JsonIgnore
    private Timestamp last;
    @JsonIgnore
    private int unitsPurchased;
    @JsonIgnore
    private int unitsSold;
    @JsonIgnore
    private long weightedHoldingTime;
    @JsonProperty("pv")
    private BigDecimal purchaseValue = ZERO;
    @JsonProperty("sv")
    private BigDecimal saleValue = ZERO;
    @JsonProperty("c")
    private BigDecimal commission = ZERO;
    @JsonIgnore
    @OneToMany(targetEntity = Trade.class, orphanRemoval = false)
    protected List<Trade> trades;
    @JsonIgnore
    @OneToMany(targetEntity = Payment.class, orphanRemoval = false)
    protected List<Payment> payments;

    public static Long calculateWeightedHoldingTime(Instant start, int size) {
        return calculateWeightedHoldingTime(start, Instant.now(),size);
    }

    public static long calculateWeightedHoldingTime(Instant start, Instant end, int size) {
        return bothPresent(start, end, Duration::between).map(Duration::toDays).map(days -> days*size).orElse(0l);
    }

    public Position(Trade t) {
        apply(List.of(t));
    }

    @JsonProperty("s")
    public LocalDate getOpen() {
        return date.toLocalDateTime().toLocalDate();
    }

    @JsonProperty("l")
    public LocalDate getClose() {
        return last.toLocalDateTime().toLocalDate();
    }

    @JsonProperty("t")
    public int getTradeCount() {
        return Optional.ofNullable(trades).map(List::size).orElse(0);
    }

    @JsonProperty("d")
    public Optional<BigDecimal> getDividends() {
        return payments.stream().filter(payment -> payment.getTxnType().isDividend()).map(Payment::getValue).reduce(BigDecimal::add);
    }

    private Position apply(List<Trade> t) {
        List<String> tradeCodes = t.stream().map(Trade::getCode).distinct().toList();
        if(tradeCodes.size() != 1) {
            throw new IllegalArgumentException("list of trades has inconsistent codes: "+t);
        }
        var tradeCode = tradeCodes.get(0);
        if(this.code != null && !this.code.equals(tradeCode)) {
            throw new IllegalArgumentException("trade codes: "+tradeCodes+" does not match position code "+this.code);
        }
        this.code = tradeCode;
        this.date = t.stream().map(Trade::getDate).reduce(this.date, (o, _t) -> o == null ? _t : _t.toInstant().isBefore(o.toInstant()) ? _t : o, oops());
        this.last = t.stream().map(Trade::getDate).reduce(this.last, (o, _t) -> o == null ? _t : _t.toInstant().isAfter(o.toInstant()) ? _t : o, oops());
        t.stream().filter(_t -> this.last != null).forEach(_t -> this.weightedHoldingTime =+ calculateWeightedHoldingTime(this.date.toInstant(), this.last.toInstant(), Optional.ofNullable(getSize()).orElse(0)));
        this.unitsPurchased = t.stream().filter(_t -> _t.getSide().isBuy()).mapToInt(Trade::getSize).reduce(this.unitsPurchased, Integer::sum);
        this.unitsSold = t.stream().filter(_t -> _t.getSide().isSell()).mapToInt(Trade::getSize).reduce(this.unitsSold, Integer::sum);
        this.purchaseValue = t.stream().filter(_t -> _t.getSide().isBuy()).map(Trade::getCost).reduce(this.purchaseValue, (c, _t) -> c == null ? _t : c.add(_t), oops());
        this.saleValue = t.stream().filter(_t -> _t.getSide().isSell()).map(Trade::getCost).reduce(this.saleValue, (c, _t) -> c == null ? _t : c.add(_t), oops());
        this.commission = t.stream().map(Trade::getCommission).reduce(this.commission, (c, _t) -> c == null ? _t : c.add(_t), oops());
        this.trades = Lists.add(this.trades, t);
        return this;
    }

    public Position onTrade(Trade t) {
        return new Position(
                this.date,
                this.code,
                this.last,
                this.unitsPurchased,
                this.unitsSold,
                this.weightedHoldingTime,
                this.purchaseValue,
                this.saleValue,
                this.commission,
                this.trades,
                List.of()
        ).apply(List.of(t));
    }

    @JsonIgnore
    public boolean isClosed() {
        return getSize() == 0;
    }

    @JsonIgnore
    public boolean isOpen() {
        return !isClosed();
    }

    @JsonProperty("sz")
    public int getSize() {
        return unitsPurchased - unitsSold;
    }

    @JsonProperty("p")
    public Optional<BigDecimal> getProfit() {
        return bothPresent(purchaseValue, saleValue, profit);
    }

    @JsonIgnore
    public boolean isProfitable() {
        return getProfit().map(BigDecimalOps::isGreaterThanZero).orElse(false);
    }

    @JsonProperty("r")
    public Optional<Double> getReturn() {
        return getProfit().flatMap(p -> Optional.ofNullable(getPurchaseValue()).filter(pv -> pv.doubleValue() > 0).map(pv  -> p.divide(pv, RoundingMode.HALF_UP).doubleValue()));
    }

    @JsonProperty("cagr")
    public Optional<Double> getCagr() {
        Optional<Double> p = periods.apply(getWeightedHoldingTime(), getUnitsPurchased());
        return p.flatMap(_p -> getReturn()
                .flatMap(_r -> cagr.apply(_r,_p)));
    }

    public long getDuration() {
        return daysBetween(this.date.toInstant(), this.last.toInstant());
    }

    public static long daysBetween(Instant from, Instant to) {
        return Duration.between(from, to).toDays();
    }

    public boolean isOpenAt(LocalDate date) {
        return Optional.ofNullable(onLoad(trades).floorEntry(date)).map(Map.Entry::getValue).filter(Position::isOpen).isPresent();
    }

    public static TreeMap<LocalDate, Position> onLoad(List<Trade> trades) {
        return trades.stream()
                .reduce(new TreeMap<>(),
                        (m,t) -> {
                            m.compute(t.getLocalDate(),
                                    (k,v) -> Optional.ofNullable(v)
                                        .map(p -> p.onTrade(t))
                                        .orElseGet(() -> Optional.ofNullable(m.lastEntry())
                                                .map(Map.Entry::getValue)
                                                .map(p -> p.onTrade(t))
                                                .orElseGet(() -> new Position(t)))
                            );
                            return m;
                        },
                        oops());
    }

    public boolean isReconciled() {
        Map<TxnType, List<Payment>> paymentMap = payments.stream().collect(Collectors.groupingBy(Payment::getTxnType));
        Optional<BigDecimal> sumPurchasePayments = paymentMap.getOrDefault(TxnType.PAYMENT, emptyList()).stream().map(Payment::getValue).reduce(BigDecimal::add);
        Optional<BigDecimal> sumSalePayments = paymentMap.getOrDefault(TxnType.DEP, emptyList()).stream().map(Payment::getValue).reduce(BigDecimal::add);
        return sumPurchasePayments.map(spp -> getPurchaseValue().equals(spp.setScale(getPurchaseValue().scale(), RoundingMode.HALF_UP))).orElse(false) &&
                sumSalePayments.map(ssp -> getSaleValue().equals(ssp.setScale(getSaleValue().scale(), RoundingMode.HALF_UP))).orElse(false);
    }

    public Position merge(Position other) {
        return new Position(
            date.toInstant().isBefore(other.date.toInstant()) ? date : other.date,
            code.equals(other.code) ? code : thenThrow(UnsupportedOperationException::new),
            last.toInstant().isAfter(other.getLast().toInstant()) ? last : other.last,
            unitsPurchased + other.unitsPurchased,
            unitsSold + other.unitsSold,
            weightedHoldingTime + other.weightedHoldingTime,
            purchaseValue.add(other.purchaseValue),
            saleValue.add(other.saleValue),
            commission.add(other.commission),
            Lists.add(trades, other.trades),
            Lists.add(payments, other.payments)
        );
    }

    public Position positionAsAt(LocalDate asAt) {
        return trades.stream()
                .filter(t -> t.getDate().toLocalDateTime().toLocalDate().isBefore(asAt))
                .reduce(null,
                        (p, t) -> Optional.ofNullable(p).map(_p -> _p.onTrade(t)).orElseGet(() -> new Position(t)),
                        Position::merge
                );
    }
}
