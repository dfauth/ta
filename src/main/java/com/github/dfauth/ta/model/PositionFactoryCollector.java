package com.github.dfauth.ta.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.dfauth.ta.functional.Lists;
import com.github.dfauth.ta.functional.Maps;
import com.github.dfauth.ta.model.txn.Payment;
import com.github.dfauth.ta.util.BigDecimalOps;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static com.github.dfauth.ta.functional.Lists.last;
import static com.github.dfauth.ta.functional.Maps.*;
import static com.github.dfauth.ta.functional.Optionals.bothPresent;
import static com.github.dfauth.ta.functional.Optionals.reduce;
import static com.github.dfauth.ta.functions.CAGR.cagr;
import static com.github.dfauth.ta.model.Dated.dated;
import static com.github.dfauth.ta.util.BigDecimalOps.multiply;
import static com.github.dfauth.ta.util.BigDecimalOps.valueOf;
import static java.math.BigDecimal.ZERO;
import static java.time.LocalDate.now;
import static java.time.ZoneOffset.UTC;
import static java.util.function.Function.identity;

@Slf4j
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class PositionFactoryCollector implements Collector<Trade, Map<String, List<PositionFactoryCollector.PositionFactory>>, Map<String,List<PositionFactoryCollector.PositionFactory>>> {

    private final BiFunction<String, LocalDate, Price> priceLookup;
    private final BiFunction<String, LocalDate, Function<LocalDate,List<Payment>>> paymentLookup;

    @Override
    public Supplier<Map<String, List<PositionFactoryCollector.PositionFactory>>> supplier() {
        return HashMap::new;
    }

    @Override
    public BiConsumer<Map<String, List<PositionFactoryCollector.PositionFactory>>, Trade> accumulator() {
        return (m, t) -> {
            m.computeIfPresent(t.getCode(), (k,v) -> last(v).filter(PositionFactoryCollector.PositionFactory::isOpen).map(p -> {
                p.addTrade(t);
                return v;
            }).orElseGet(() -> Lists.add(v, new PositionFactory(t.getCode(), Maps.from(() -> new TreeMap<>(LocalDate::compareTo), Trade::getLocalDate, t), priceLookup, paymentLookup))));
            m.computeIfAbsent(t.getCode(), k -> List.of(new PositionFactory(t.getCode(), Maps.from(() -> new TreeMap<>(LocalDate::compareTo), Trade::getLocalDate, t), priceLookup, paymentLookup)));
        };
    }

    @Override
    public BinaryOperator<Map<String, List<PositionFactoryCollector.PositionFactory>>> combiner() {
        return Maps.merge(listMerge());
    }

    @Override
    public Function<Map<String, List<PositionFactoryCollector.PositionFactory>>, Map<String,List<PositionFactoryCollector.PositionFactory>>> finisher() {
        return identity();
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.emptySet();
    }

    @RequiredArgsConstructor
    public static abstract class AbstractPositionFactory {
        @Getter
        protected final String code;
        protected final TreeMap<LocalDate, List<Trade>> trades;

        public AbstractPositionFactory addTrade(Trade t) {
            trades.computeIfPresent(t.getLocalDate(), (k,v) -> Lists.add(v, t));
            trades.computeIfAbsent(t.getLocalDate(), k -> List.of(t));
            return this;
        }

        @JsonProperty("sz")
        public int getSize() {
            return streamTrades().mapToInt(t -> t.getSide().getMultiplier() * t.getSize()).sum();
        }

        @JsonIgnore
        public boolean isOpen() {
            return getSize() != 0;
        }

        @JsonIgnore
        public boolean isClosed() {
            return !isOpen();
        }

        protected Stream<Trade> streamTrades(Predicate<Side> p) {
            return streamTrades().filter(t -> p.test(t.getSide()));
        }

        protected Stream<Trade> streamTrades() {
            return trades.values().stream().flatMap(List::stream);
        }

        @JsonProperty("s")
        public LocalDate getStart() {
            return trades.ceilingKey(LocalDate.EPOCH);
        }

        @JsonProperty("l")
        public LocalDate getLast() {
            return trades.floorKey(now());
        }
    }

    public static class PositionFactory extends AbstractPositionFactory {
        private final BiFunction<String, LocalDate, Price> priceLookup;
        private final BiFunction<String, LocalDate, Function<LocalDate,List<Payment>>> paymentLookup;

        public PositionFactory(String code, TreeMap<LocalDate, List<Trade>> trades, BiFunction<String, LocalDate, Price> priceLookup, BiFunction<String, LocalDate, Function<LocalDate,List<Payment>>> paymentLookup) {
            super(code, trades);
            this.priceLookup = priceLookup;
            this.paymentLookup = paymentLookup;
        }

        public Optional<Position> create() {
            return create(now());
        }

        public Optional<Position> create(LocalDate asAt) {
            return Optional.of(asAt)
                    .filter(_asAt -> _asAt.isAfter(getStart()))
                    .map(_asAt -> new Position(
                            code,
                            trades.values().stream().flatMap(List::stream).filter(t -> t.getLocalDate().isBefore(asAt)).map(mapEntry(Trade::getLocalDate)).collect(mapEntries(() -> new TreeMap<>(LocalDate::compareTo), listMerge())),
                            Maps.from(() -> new TreeMap<>(LocalDate::compareTo), Payment::getDate, paymentLookup.apply(code, asAt).apply(getStart()).toArray(Payment[]::new)),
                            priceLookup.apply(code, asAt)
                        )
                    );
        }
    }

    public static class Position extends AbstractPositionFactory {

        private final TreeMap<LocalDate, List<Payment>> payments;
        @Setter
        private Price price;

        public Position(String code, TreeMap<LocalDate, List<Trade>> trades, TreeMap<LocalDate, List<Payment>> payments, Price price) {
            super(code, trades);
            this.payments = payments;
            this.price = price;
        }

        public Position(Trade trade) {
            this(trade.getCode(), Maps.from(() -> new TreeMap<>(LocalDate::compareTo), Trade::getLocalDate, trade), new TreeMap<>(LocalDate::compareTo), null);
        }

        private static Function<Trade, BigDecimal> getValue() {
            return t -> Direction.fromString(t.getSide().name()).map(d -> d.getValue(t.getCost())).orElseThrow();
        }

        @JsonProperty("pv")
        public Optional<BigDecimal> getPurchaseValue() {
            return streamTrades(Side::isBuy).map(Position.getValue()).reduce(BigDecimal::add);
        }

        @JsonProperty("c")
        public Optional<BigDecimal> getCommission() {
            return streamTrades().map(Trade::getCommission).reduce(BigDecimal::add);
        }

        @JsonProperty("sv")
        public Optional<BigDecimal> getSaleValue() {
            return streamTrades(Side::isSell).map(Position.getValue()).reduce(BigDecimal::add);
        }

        @JsonProperty("p")
        public Optional<BigDecimal> getProfit() {
            return isOpen() ? Optional.of(ZERO) : reduce(BigDecimal::add, getSaleValue(), getPurchaseValue());
        }

        @JsonProperty("r")
        public Optional<BigDecimal> getReturn() {
            return bothPresent(getProfit(), Optional.of(getMaxInv()).filter(BigDecimalOps::isGreaterThanZero), (BigDecimal l, BigDecimal r) -> BigDecimalOps.divide(l, r));
        }

        @JsonProperty("t")
        public long getTrades() {
            return streamTrades().count();
        }

        @JsonProperty("mv")
        public BigDecimal getMarketValue() {
            return getPrice().map(p -> multiply(p.getClose(), getSize())).orElse(ZERO);
        }

        @JsonProperty("duration")
        public long getDuration() {
            return isOpen() ? Duration.between(getStart().atStartOfDay(UTC), now().atStartOfDay(UTC)).toDays() : Duration.between(getStart().atStartOfDay(UTC), getLast().atStartOfDay(UTC)).toDays();
        }

        @JsonProperty("wht")
        public BigDecimal getWeightedHoldingTime() {
            var now = now().atStartOfDay(UTC);
            ToLongFunction<Trade> f = t -> t.getSize() * (t.getSide().isBuy() ?
                    Duration.between(t.getLocalDate().atStartOfDay(UTC), now).toDays() :
                    Duration.between(now, t.getLocalDate().atStartOfDay(UTC)).toDays());
            return BigDecimalOps.divide(valueOf(streamTrades().mapToLong(f).sum()), getUnitsPurchased());
        }

        @JsonProperty("cagr")
        public Optional<BigDecimal> getCagr() {
            return getReturn().map(r -> cagr(r.doubleValue(), getWeightedHoldingTime().doubleValue()/365.25)).map(d -> valueOf(d).setScale(4, RoundingMode.HALF_UP));
        }

        @JsonGetter("mi")
        public BigDecimal getMaxInv() {
            return getMaxInvestment().getMaxValue().getPayload();
        }

        @JsonIgnore
        public MaxInvestment getMaxInvestment() {
            return trades.entrySet().stream()
                    .map(e -> dated(e.getKey(), e.getValue().stream()
                            .map(Trade::getValue)
                            .reduce(BigDecimal::add).orElse(ZERO)))
                    .reduce(new MaxInvestment(),
                            MaxInvestment::apply,
                            MaxInvestment::merge);
        }

        @JsonIgnore
        public Optional<Price> getPrice() {
            return Optional.ofNullable(price);
        }

        @JsonIgnore
        public int getUnitsPurchased() {
            return streamTrades(Side::isBuy).mapToInt(Trade::getSize).sum();
        }

        @JsonProperty("d")
        public BigDecimal getDividends() {
            return payments.values().stream().flatMap(List::stream).map(Payment::getValue).reduce(BigDecimal::add).orElse(ZERO);
        }

        public void addPayments(List<Payment> payments) {
            payments.stream().forEach(p -> {
                this.payments.computeIfPresent(p.getDate(), (k,v) -> Lists.add(v, p));
                this.payments.computeIfAbsent(p.getDate(), k -> List.of(p));
            });
        }

    }
}
