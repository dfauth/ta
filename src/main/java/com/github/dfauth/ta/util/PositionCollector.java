package com.github.dfauth.ta.util;

import com.github.dfauth.ta.functional.Lists;
import com.github.dfauth.ta.model.Position;
import com.github.dfauth.ta.model.Trade;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

@Slf4j
public class PositionCollector implements Collector<Trade, PositionCollector.TradeAggregator, List<Position>> {

    @Override
    public Supplier<TradeAggregator> supplier() {
        return TradeAggregator::new;
    }

    @Override
    public BiConsumer<TradeAggregator, Trade> accumulator() {
        return TradeAggregator::add;
    }

    @Override
    public BinaryOperator<TradeAggregator> combiner() {
        return TradeAggregator::add;
    }

    @Override
    public Function<TradeAggregator, List<Position>> finisher() {
        return TradeAggregator::getPositions;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.emptySet();
    }

    @AllArgsConstructor
    public static class TradeAggregator {

        private Optional<Position> openPosition;
        private List<Position> closedPositions;

        public TradeAggregator() {
            this(Optional.empty(), Collections.emptyList());
        }

        public TradeAggregator add(TradeAggregator other) {
            var tmp = Lists.add(closedPositions, other.closedPositions);
            tmp.sort(Comparator.comparing(Position::getDate));
            return new TradeAggregator(openPosition.map(p -> other.openPosition.map(p1 -> p1.merge(p))).orElse(other.openPosition), tmp);
        }

        public List<Position> getPositions() {
            return openPosition.map(p -> Lists.add(closedPositions, p)).orElse(closedPositions);
        }

        public void add(Trade t) {
            openPosition.ifPresentOrElse(
                    p -> {
                        Position newPosition = p.onTrade(t);
                        if(newPosition.isOpen()) {
                            openPosition = Optional.of(newPosition);
                        } else {
                            openPosition = Optional.empty();
                            closedPositions = Lists.add(closedPositions, newPosition);
                        }
                    },
                    () -> openPosition = Optional.of(new Position(t)));
        }
    }
}
