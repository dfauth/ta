package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.functional.Lists;
import com.github.dfauth.ta.functional.RingBufferProcessor;
import com.github.dfauth.ta.functions.Momentum;
import com.github.dfauth.ta.model.Price;
import com.github.dfauth.ta.model.PriceAction;
import com.github.dfauth.ta.util.ArrayRingBuffer;
import com.github.dfauth.ta.util.RingBuffer;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Processor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.dfauth.ta.functional.Lists.head;
import static com.github.dfauth.ta.functional.Lists.last;
import static com.github.dfauth.ta.functional.RingBufferCollector.ringBufferCollector;
import static com.github.dfauth.ta.functional.RingBufferProcessor.ringBufferProcessor;
import static com.github.dfauth.ta.functions.Momentum.processPricesMomentum;
import static com.github.dfauth.ta.model.PriceAction.EMA;
import static com.github.dfauth.ta.model.PriceAction.SMA;
import static com.github.dfauth.ta.reactive.OptionalProcessor.identity;
import static io.github.dfauth.trycatch.ExceptionalRunnable.tryCatch;
import static java.util.Optional.empty;

@RestController
@Slf4j
public class SMAController extends BaseController implements ControllerMixIn {

    @PostMapping("/sma/{period}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Controller.OHLC> sma(@RequestBody List<List<String>> codes, @PathVariable int period) {
        try {
            log.info("sma/{}/{}",codes,period);
            Map<String, Controller.OHLC> result = flatMapCode(codes, code -> sma(code, period).stream());
            return result;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/sma/{_code}/{period}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<Controller.OHLC> sma(@PathVariable String _code, @PathVariable int period) {
        log.info("sma/{}/{}",_code,period);
        return tryCatch(() -> {
            List<Price> prices = prices(_code, period);
            return Optional.of(prices.stream().collect(ringBufferCollector(new PriceAction[period], SMA))).flatMap(o -> o.map(Controller.OHLC::new));
        }, ControllerMixIn.logAndReturn(empty()));
    }

    @PostMapping("/sma/momentum/{period}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Controller.OHLC> smaMomentum(@RequestBody List<List<String>> codes, @PathVariable int period) {
        try {
            log.info("sma/momentum/{}/{}",codes,period);
            Map<String, Controller.OHLC> result = flatMapCode(codes, code -> smaMomentum(code, period).stream());
            return result;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/sma/momentum/{_code}/{period}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<Controller.OHLC> smaMomentum(@PathVariable String _code, @PathVariable int period) {
        RingBufferProcessor<PriceAction, Optional<PriceAction>> p = ringBufferProcessor(new PriceAction[period], SMA);
        log.info("smaMomentum/{}/{}",_code,period);
        return tryCatch(() -> {
            List<Price> prices = prices(_code, 2 * period);
            List<PriceAction> tmp = new ArrayList<>();
            Processor<Optional<PriceAction>, PriceAction> p1 = Flux.from(p).subscribeWith(identity());
            Flux.from(p1).subscribe(tmp::add);
            Flux.fromStream(prices.stream()).subscribe(p);
            List<PriceAction> tail = Lists.splitAt(tmp, -1 * period)._2();
            return last(tail)
                    .flatMap(_l -> head(tail)
                    .map(_h -> _l.subtract(_h)
                            .divide(period)))
                    .map(Controller.OHLC::new);
        }, ControllerMixIn.logAndReturn(empty()));
    }

    @PostMapping("/ema/momentum/{period}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Controller.OHLC> emaMomentum(@RequestBody List<List<String>> codes, @PathVariable int period) {
        try {
            log.info("ema/momentum/{}/{}",codes,period);
            return flatMapCode(codes, code -> emaMomentum(code, period).stream());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/ema/momentum/{_code}/{period}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<Controller.OHLC> emaMomentum(@PathVariable String _code, @PathVariable int period) {
        RingBuffer<PriceAction> ringBuffer = new ArrayRingBuffer<>(new PriceAction[period]);
        log.info("emaMomentum/{}/{}",_code,period);
        return tryCatch(() -> {
            List<Price> prices = prices(_code, 2 * period);
            List<PriceAction> emas = prices.stream().map(pa -> {
                ringBuffer.write(pa);
                return EMA.apply(ringBuffer.streamIfFull().collect(Collectors.toList()));
            }).flatMap(Optional::stream).collect(Collectors.toList());
            return last(emas)
                    .flatMap(_l -> head(emas)
                            .map(_h -> _l.subtract(_h)
                                    .divide(period)))
                    .map(Controller.OHLC::new);
        }, ControllerMixIn.logAndReturn(empty()));
    }

    @GetMapping("/ema/momentum/new/{_code}/{period}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<Momentum> emaMomentumNew(@PathVariable String _code, @PathVariable int period) {
        log.info("emaMomentum/{}/{}",_code,period);
        return tryCatch(() -> {
            List<Price> prices = prices(_code, 4 * period);
            return processPricesMomentum(period, prices, EMA);
        }, ControllerMixIn.logAndReturn(empty()));
    }

}
