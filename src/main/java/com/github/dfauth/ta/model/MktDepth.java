package com.github.dfauth.ta.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Optionals;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static com.github.dfauth.ta.functional.Collectors.oops;

@Slf4j
@Entity
@Getter
@Builder
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@Table(name = "MKTDEPTH")
@IdClass(CodeDateCompositeKey.class)
public class MktDepth {

    public static Comparator<MktDepth> comparator = (m1, m2) -> Optionals.mapIfAllPresent(m1.getSharesRatio(), m2.getSharesRatio(), (r1, r2) -> (int)(r1 - r2)).orElse(0);

    public static MktDepth.MktDepthBuilder builder(String code) {
        return new MktDepthBuilder().code(code).date(new Timestamp(Instant.now().toEpochMilli()));
    }

    @Id @JsonIgnore
    private Timestamp date;
    @Id
    private String code;
    @JsonProperty("b")
    private int buyers;
    @JsonProperty("bs")
    private int buyerShares;
    @JsonProperty("s")
    private int sellers;
    @JsonProperty("ss")
    private int sellerShares;
    @JsonProperty("p")
    private Double price;
    @JsonProperty("c")
    private Double change;
    @JsonProperty("v")
    private Integer volume;
    @JsonProperty("t")
    private transient Double trend;

    public MktDepth() {
    }

    public static Collector<? super MktDepth, Object, Double> trendCollector() {
        return new Collector<>() {
            @Override
            public Supplier<Object> supplier() {
                return () -> new Object();
            }

            @Override
            public BiConsumer<Object, MktDepth> accumulator() {
                return (o, md) -> {
                };
            }

            @Override
            public BinaryOperator<Object> combiner() {
                return oops();
            }

            @Override
            public Function<Object, Double> finisher() {
                return ignored -> 0.0d;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Set.of();
            }
        };
    }

    @JsonProperty("d")
    public LocalDate getLocalDate() {
        return LocalDate.from(date.toLocalDateTime());
    }

    public boolean isSameLocalDate(LocalDate localDate) {
        return getLocalDate().equals(localDate);
    }

    public MktDepth avg(MktDepth mktDepth) {
        return new MktDepthBuilder()
                .code(mktDepth.code)
                .date(new Timestamp(LocalDate.from(mktDepth.date.toLocalDateTime()).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()))
                .buyers((buyers+mktDepth.getBuyers())/2)
                .buyerShares((buyerShares+mktDepth.getBuyerShares())/2)
                .sellers((sellers+mktDepth.getSellers())/2)
                .sellerShares((sellerShares+mktDepth.getSellerShares())/2)
                .price((price+mktDepth.getPrice())/2)
                .change((change+mktDepth.getChange())/2)
                .volume((volume+mktDepth.getVolume())/2)
                .build();
    }

    @JsonProperty("r")
    public Optional<Double> getSharesRatio() {
        return sellerShares == 0 ? Optional.empty() : Optional.of(BigDecimal.valueOf(buyerShares / (double) sellerShares).setScale(3, RoundingMode.HALF_UP).doubleValue());
    }

    public MktDepth trend(MktDepth mktDepth) {
        MktDepth current;
        MktDepth prev;
        if (date.toLocalDateTime().toLocalDate().isAfter(mktDepth.getLocalDate())) {
            current = this;
            prev = mktDepth;
        } else {
            prev = this;
            current = mktDepth;
        }
        return new MktDepthBuilder()
                .code(current.code)
                .date(new Timestamp(LocalDate.from(current.date.toLocalDateTime()).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()))
                .buyers(current.buyers)
                .buyerShares(current.buyerShares)
                .sellers(current.sellers)
                .sellerShares(current.sellerShares)
                .price(current.price)
                .change(current.change)
                .volume(current.getVolume())
                .trend(((double)(current.buyerShares/ current.sellerShares)) - ((double)(prev.buyerShares/prev.sellerShares)))
                .build();
    }
}
