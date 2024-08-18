package com.github.dfauth.ta.functional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dfauth.ta.model.PriceAction;
import com.github.dfauth.ta.util.ArrayRingBuffer;
import com.github.dfauth.ta.util.RingBuffer;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.github.dfauth.ta.functional.Collectors.oops;
import static com.github.dfauth.ta.util.BigDecimalOps.isGreaterThanZero;
import static com.github.dfauth.ta.util.BigDecimalOps.isLessThanZero;

@Slf4j
public class OnBalanceVolume {

    private int onBalanceVolume = 0;
    @JsonIgnore
    private PriceAction current = null;

    public OnBalanceVolume() {
    }

    public static Optional<OnBalanceVolume> calculateOnBalanceVolume(List<PriceAction> priceAction) {
        return calculateOnBalanceVolume(priceAction, priceAction.size());
    }

    public static Optional<OnBalanceVolume> calculateOnBalanceVolume(List<PriceAction> priceAction, int period) {
        RingBuffer<PriceAction> ringBuffer = new ArrayRingBuffer<>(new PriceAction[period]);
        List<OnBalanceVolume> obvs = priceAction.stream().map(pa -> {
            ringBuffer.write(pa);
            return ringBuffer.streamIfFull().reduce(new OnBalanceVolume(), OnBalanceVolume::next, oops());
        }).collect(java.util.stream.Collectors.toList());
        Optional<PriceAction> ema = PriceAction.EMA.apply(obvs.stream().map(obv -> obv.current).collect(java.util.stream.Collectors.toList()));
        return obvs.stream().reduce(OnBalanceVolume::difference);
    }

    private OnBalanceVolume difference(OnBalanceVolume onBalanceVolume) {
        return null;
    }

    private OnBalanceVolume next(PriceAction next) {
        current = Optional.ofNullable(current).map(prev -> {
            BigDecimal diff = next.subtract(prev).getClose();
            if(isGreaterThanZero(diff)) {
                onBalanceVolume += next.getVolume();
            } else if (isLessThanZero(diff)) {
                onBalanceVolume -= next.getVolume();
            }
            return next;
        }).orElse(next);
        return this;
    }

}
