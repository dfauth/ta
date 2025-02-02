package com.github.dfauth.ta.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
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

    public static MktDepth.MktDepthBuilder builder(String code) {
        return new MktDepthBuilder().code(code).date(new Timestamp(Instant.now().toEpochMilli()));
    }

    @Id
    private Timestamp date;
    @Id
    private String code;
    private int buyers;
    private int buyerShares;
    private int sellers;
    private int sellerShares;

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
                .build();
    }

    public Optional<Double> getSharesRatio() {
        return sellerShares == 0 ? Optional.empty() : Optional.of(buyerShares/(double)sellerShares);
    }

    public Optional<Double> trend(MktDepth mktDepth) {
        return getSharesRatio().flatMap(r -> mktDepth.getSharesRatio().map(r1 -> r/r1));
    }
}
