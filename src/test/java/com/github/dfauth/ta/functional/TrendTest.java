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
public class TrendTest {

    @Test
    public void testIt() {
        {
            Trend trend = calculateTrend(MP1);
            assertEquals(Trend.TrendState.BULL, trend.getState());
            assertEquals(52, trend.getDaysInThisState());
            assertTrue(trend.getPreviousState().isEmpty());
            assertEquals(0.041, trend.getCurrent().getPriceDistanceFromFastMA().get().doubleValue());
            assertFalse(trend.getTrendAcceleration().get().doubleValue() > 0.0);
            assertEquals(-0.262, trend.getCurrent().getFastVolumeDistanceFromLongMA().get().doubleValue());
            assertEquals(-0.127, trend.getCurrent().getSlowVolumeDistanceFromLongMA().get().doubleValue());
        }

        {
            Trend trend = calculateTrend(EMR);
            assertEquals(Trend.TrendState.BULL, trend.getState());
            assertEquals(52, trend.getDaysInThisState());
            assertTrue(trend.getPreviousState().isEmpty());
            assertEquals(0.05, trend.getCurrent().getPriceDistanceFromFastMA().get().doubleValue());
            assertTrue(trend.getTrendAcceleration().get().doubleValue() > 0.0);
            assertEquals(0.558, trend.getCurrent().getFastVolumeDistanceFromLongMA().get().doubleValue());
            assertEquals(0.297, trend.getCurrent().getSlowVolumeDistanceFromLongMA().get().doubleValue());
        }

        {
            Trend trend = calculateTrend(LBL);
            assertEquals(Trend.TrendState.BEAR, trend.getState());
            assertEquals(10, trend.getDaysInThisState());
            assertEquals(Trend.TrendState.LATE_BULL, trend.getPreviousState().get());
            assertEquals(0.003, trend.getCurrent().getPriceDistanceFromFastMA().get().doubleValue());
            assertFalse(trend.getTrendAcceleration().get().doubleValue() > 0.0);
            assertEquals(2.06, trend.getCurrent().getFastVolumeDistanceFromLongMA().get().doubleValue());
            assertEquals(0.895, trend.getCurrent().getSlowVolumeDistanceFromLongMA().get().doubleValue());
        }

        {
            Trend trend = calculateTrend(PPL);
            assertEquals(Trend.TrendState.LATE_BEAR, trend.getState());
            assertEquals(52, trend.getDaysInThisState());
            assertTrue(trend.getPreviousState().isEmpty());
            assertEquals(0.0, trend.getCurrent().getPriceDistanceFromFastMA().get().doubleValue());
            assertFalse(trend.getTrendAcceleration().isPresent());
            assertEquals(-0.246, trend.getCurrent().getFastVolumeDistanceFromLongMA().get().doubleValue());
            assertEquals(0.273, trend.getCurrent().getSlowVolumeDistanceFromLongMA().get().doubleValue());
        }

        {
            Trend trend = calculateTrend(WGX);
            assertEquals(Trend.TrendState.BULL, trend.getState());
            assertEquals(52, trend.getDaysInThisState());
            assertTrue(trend.getPreviousState().isEmpty());
            assertEquals(-0.077, trend.getCurrent().getPriceDistanceFromFastMA().get().doubleValue());
            assertFalse(trend.getTrendAcceleration().get().doubleValue() > 0.0);
            assertEquals(-0.183, trend.getCurrent().getFastVolumeDistanceFromLongMA().get().doubleValue());
            assertEquals(0.031, trend.getCurrent().getSlowVolumeDistanceFromLongMA().get().doubleValue());
        }

        {
            Trend trend = calculateTrend(CGC);
            assertEquals(Trend.TrendState.LATE_BULL, trend.getState());
            assertEquals(20, trend.getDaysInThisState());
            assertEquals(Trend.TrendState.BULL, trend.getPreviousState().get());
            assertEquals(0.053, trend.getCurrent().getPriceDistanceFromFastMA().get().doubleValue());
            assertFalse(trend.getTrendAcceleration().get().doubleValue() > 0.0);
            assertEquals(1.477, trend.getCurrent().getFastVolumeDistanceFromLongMA().get().doubleValue());
            assertEquals(0.640, trend.getCurrent().getSlowVolumeDistanceFromLongMA().get().doubleValue());
        }

        {
            Trend trend = calculateTrend(AX1);
            assertEquals(Trend.TrendState.LATE_BEAR, trend.getState());
            assertEquals(42, trend.getDaysInThisState());
            assertEquals(Trend.TrendState.BEAR, trend.getPreviousState().get());
            assertEquals(0.003, trend.getCurrent().getPriceDistanceFromFastMA().get().doubleValue());
            assertFalse(trend.getTrendAcceleration().get().doubleValue() > 0.0);
            assertEquals(0.024, trend.getCurrent().getFastVolumeDistanceFromLongMA().get().doubleValue());
            assertEquals(0.014, trend.getCurrent().getSlowVolumeDistanceFromLongMA().get().doubleValue());
        }
    }

    private Trend calculateTrend(List<Price> priceAction) {
        Price last = priceAction.get(priceAction.size() - 1);
        log.info(priceAction.size()+" prices for "+last.getCode()+" ending "+last.getDate());
        return Trend.calculateTrend(mapList(priceAction, PriceAction.class::cast)).get();
    }
}
