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
            Trend trend = calculateTrend(ALL);
            assertEquals(Trend.TrendState.BULL, trend.getState());
            assertEquals(51, trend.getDaysInThisState());
            assertTrue(trend.getPreviousState().isEmpty());
            assertEquals(0.02, trend.getCurrent().getPriceDistanceFromFastMA().get().doubleValue());
            assertFalse(trend.getTrendAcceleration().get().doubleValue() > 0.0);
            assertEquals(-0.257, trend.getCurrent().getFastVolumeDistanceFromLongMA().get().doubleValue());
            assertEquals(-0.147, trend.getCurrent().getSlowVolumeDistanceFromLongMA().get().doubleValue());
        }
        {
            Trend trend = calculateTrend(MP1);
            assertEquals(Trend.TrendState.BULL, trend.getState());
            assertEquals(51, trend.getDaysInThisState());
            assertTrue(trend.getPreviousState().isEmpty());
            assertEquals(0.031, trend.getCurrent().getPriceDistanceFromFastMA().get().doubleValue());
            assertTrue(trend.getTrendAcceleration().get().doubleValue() > 0.0);
            assertEquals(-0.273, trend.getCurrent().getFastVolumeDistanceFromLongMA().get().doubleValue());
            assertEquals(-0.168, trend.getCurrent().getSlowVolumeDistanceFromLongMA().get().doubleValue());
        }

        {
            Trend trend = calculateTrend(EMR);
            assertEquals(Trend.TrendState.BULL, trend.getState());
            assertEquals(51, trend.getDaysInThisState());
            assertTrue(trend.getPreviousState().isEmpty());
            assertEquals(0.049, trend.getCurrent().getPriceDistanceFromFastMA().get().doubleValue());
            assertFalse(trend.getTrendAcceleration().get().doubleValue() > 0.0);
            assertEquals(0.869, trend.getCurrent().getFastVolumeDistanceFromLongMA().get().doubleValue());
            assertEquals(0.372, trend.getCurrent().getSlowVolumeDistanceFromLongMA().get().doubleValue());
        }

        {
            Trend trend = calculateTrend(LBL);
            assertEquals(Trend.TrendState.BEAR, trend.getState());
            assertEquals(16, trend.getDaysInThisState());
            assertEquals(Trend.TrendState.LATE_BULL, trend.getPreviousState().get());
            assertEquals(-0.001, trend.getCurrent().getPriceDistanceFromFastMA().get().doubleValue());
            assertTrue(trend.getTrendAcceleration().get().doubleValue() > 0.0);
            assertEquals(2.289, trend.getCurrent().getFastVolumeDistanceFromLongMA().get().doubleValue());
            assertEquals(0.54, trend.getCurrent().getSlowVolumeDistanceFromLongMA().get().doubleValue());
        }

        {
            Trend trend = calculateTrend(PPL);
            assertEquals(Trend.TrendState.LATE_BEAR, trend.getState());
            assertEquals(51, trend.getDaysInThisState());
            assertTrue(trend.getPreviousState().isEmpty());
            assertEquals(0.0, trend.getCurrent().getPriceDistanceFromFastMA().get().doubleValue());
            assertFalse(trend.getTrendAcceleration().isPresent());
            assertEquals(-0.14, trend.getCurrent().getFastVolumeDistanceFromLongMA().get().doubleValue());
            assertEquals(0.33, trend.getCurrent().getSlowVolumeDistanceFromLongMA().get().doubleValue());
        }

        {
            Trend trend = calculateTrend(WGX);
            assertEquals(Trend.TrendState.BULL, trend.getState());
            assertEquals(51, trend.getDaysInThisState());
            assertTrue(trend.getPreviousState().isEmpty());
            assertEquals(-0.064, trend.getCurrent().getPriceDistanceFromFastMA().get().doubleValue());
            assertTrue(trend.getTrendAcceleration().isPresent());
            assertEquals(-0.269, trend.getCurrent().getFastVolumeDistanceFromLongMA().get().doubleValue());
            assertEquals(-0.005, trend.getCurrent().getSlowVolumeDistanceFromLongMA().get().doubleValue());
        }

        {
            Trend trend = calculateTrend(CGC);
            assertEquals(Trend.TrendState.LATE_BULL, trend.getState());
            assertEquals(17, trend.getDaysInThisState());
            assertEquals(Trend.TrendState.BULL, trend.getPreviousState().get());
            assertEquals(0.044, trend.getCurrent().getPriceDistanceFromFastMA().get().doubleValue());
            assertFalse(trend.getTrendAcceleration().get().doubleValue() > 0.0);
            assertEquals(1.537, trend.getCurrent().getFastVolumeDistanceFromLongMA().get().doubleValue());
            assertEquals(0.674, trend.getCurrent().getSlowVolumeDistanceFromLongMA().get().doubleValue());
        }

        {
            Trend trend = calculateTrend(AX1);
            assertEquals(Trend.TrendState.EARLY_BULL, trend.getState());
            assertEquals(24, trend.getDaysInThisState());
            assertEquals(Trend.TrendState.LATE_BEAR, trend.getPreviousState().get());
            assertEquals(-0.002, trend.getCurrent().getPriceDistanceFromFastMA().get().doubleValue());
            assertTrue(trend.getTrendAcceleration().get().doubleValue() > 0.0);
            assertEquals(0.014, trend.getCurrent().getFastVolumeDistanceFromLongMA().get().doubleValue());
            assertEquals(0.054, trend.getCurrent().getSlowVolumeDistanceFromLongMA().get().doubleValue());
        }
    }

    private Trend calculateTrend(List<Price> priceAction) {
        Price last = priceAction.get(priceAction.size() - 1);
        log.info(priceAction.size()+" prices for "+last.getCode()+" ending "+last.getDate());
        return Trend.calculateTrend(mapList(priceAction, PriceAction.class::cast)).get();
    }
}
