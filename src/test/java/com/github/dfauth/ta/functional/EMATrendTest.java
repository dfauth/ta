package com.github.dfauth.ta.functional;


import com.github.dfauth.ta.model.Price;
import com.github.dfauth.ta.model.PriceAction;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.dfauth.ta.functional.Lists.mapList;
import static com.github.dfauth.ta.functions.TestData.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class EMATrendTest {

    @Test
    public void testIt() {
        {
            Trend trend = calculateTrend(MP1);
            assertEquals(Trend.TrendState.BULL, trend.getState());
            assertEquals(52, trend.getDaysInThisState());
            assertTrue(trend.getPreviousState().isEmpty());
            assertEquals(0.044, trend.getCurrent().getPriceDistanceFromFastMA().get().doubleValue());
            assertFalse(trend.getTrendAcceleration().get().doubleValue() > 0.0);
            assertEquals(-0.26, trend.getCurrent().getFastVolumeDistanceFromLongMA().get().doubleValue());
            assertEquals(-0.158, trend.getCurrent().getSlowVolumeDistanceFromLongMA().get().doubleValue());
        }

        {
            Trend trend = calculateTrend(EMR);
            assertEquals(Trend.TrendState.BULL, trend.getState());
            assertEquals(52, trend.getDaysInThisState());
            assertTrue(trend.getPreviousState().isEmpty());
            assertEquals(0.036, trend.getCurrent().getPriceDistanceFromFastMA().get().doubleValue());
            assertTrue(trend.getTrendAcceleration().get().doubleValue() > 0.0);
            assertEquals(0.717, trend.getCurrent().getFastVolumeDistanceFromLongMA().get().doubleValue());
            assertEquals(0.25, trend.getCurrent().getSlowVolumeDistanceFromLongMA().get().doubleValue());
        }

        {
            Trend trend = calculateTrend(LBL);
            assertEquals(Trend.TrendState.BEAR, trend.getState());
            assertEquals(18, trend.getDaysInThisState());
            assertEquals(Trend.TrendState.LATE_BULL, trend.getPreviousState().get());
            assertEquals(0.009, trend.getCurrent().getPriceDistanceFromFastMA().get().doubleValue());
            assertFalse(trend.getTrendAcceleration().get().doubleValue() > 0.0);
            assertEquals(1.154, trend.getCurrent().getFastVolumeDistanceFromLongMA().get().doubleValue());
            assertEquals(0.811, trend.getCurrent().getSlowVolumeDistanceFromLongMA().get().doubleValue());
        }

        {
            Trend trend = calculateTrend(PPL);
            assertEquals(Trend.TrendState.LATE_BEAR, trend.getState());
            assertEquals(52, trend.getDaysInThisState());
            assertTrue(trend.getPreviousState().isEmpty());
            assertEquals(0.0, trend.getCurrent().getPriceDistanceFromFastMA().get().doubleValue());
            assertFalse(trend.getTrendAcceleration().isPresent());
            assertEquals(-0.206, trend.getCurrent().getFastVolumeDistanceFromLongMA().get().doubleValue());
            assertEquals(0.066, trend.getCurrent().getSlowVolumeDistanceFromLongMA().get().doubleValue());
        }

        {
            Trend trend = calculateTrend(WGX);
            assertEquals(Trend.TrendState.LATE_BULL, trend.getState());
            assertEquals(0, trend.getDaysInThisState());
            assertFalse(trend.getPreviousState().isEmpty());
            assertEquals(-0.062, trend.getCurrent().getPriceDistanceFromFastMA().get().doubleValue());
            assertFalse(trend.getTrendAcceleration().isPresent());
            assertEquals(-0.268, trend.getCurrent().getFastVolumeDistanceFromLongMA().get().doubleValue());
            assertEquals(0.001, trend.getCurrent().getSlowVolumeDistanceFromLongMA().get().doubleValue());
        }

        {
            Trend trend = calculateTrend(CGC);
            assertEquals(Trend.TrendState.LATE_BULL, trend.getState());
            assertEquals(24, trend.getDaysInThisState());
            assertEquals(Trend.TrendState.BULL, trend.getPreviousState().get());
            assertEquals(0.041, trend.getCurrent().getPriceDistanceFromFastMA().get().doubleValue());
            assertFalse(trend.getTrendAcceleration().get().doubleValue() > 0.0);
            assertEquals(1.437, trend.getCurrent().getFastVolumeDistanceFromLongMA().get().doubleValue());
            assertEquals(0.679, trend.getCurrent().getSlowVolumeDistanceFromLongMA().get().doubleValue());
        }

        {
            Trend trend = calculateTrend(AX1);
            assertEquals(Trend.TrendState.LATE_BEAR, trend.getState());
            assertEquals(9, trend.getDaysInThisState());
            assertEquals(Trend.TrendState.EARLY_BULL, trend.getPreviousState().get());
            assertEquals(0.006, trend.getCurrent().getPriceDistanceFromFastMA().get().doubleValue());
            assertFalse(trend.getTrendAcceleration().get().doubleValue() > 0.0);
            assertEquals(-0.041, trend.getCurrent().getFastVolumeDistanceFromLongMA().get().doubleValue());
            assertEquals(0.034, trend.getCurrent().getSlowVolumeDistanceFromLongMA().get().doubleValue());
        }
    }

    private Trend calculateTrend(List<Price> priceAction) {
        Price last = priceAction.get(priceAction.size() - 1);
        log.info(priceAction.size()+" prices for "+last.getCode()+" ending "+last.getDate());
        return Trend.calculateTrend(mapList(priceAction, PriceAction.class::cast), 20, 50, 200, PriceAction.EMA).get();
    }
}
