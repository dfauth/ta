package com.github.dfauth.ta.functions;

import com.github.dfauth.ta.model.Price;
import com.github.dfauth.ta.model.PriceAction;

import java.util.function.BiFunction;

public interface Gap {

    BiFunction<Price, Price, Gap> isGapped = (previous, current) -> current.gapUp(previous) ? GapType.GAPUP.create(current.subtract(previous)) : current.gapDown(previous) ? GapType.GAPDOWN.create(current.subtract(previous)) : GapType.NEITHER.create(current.subtract(previous));

    default boolean isGapUp() {
        return getGapType().isGapUp();
    }

    default boolean isGapDown() {
        return getGapType().isGapDown();
    }

    GapType getGapType();

    enum GapType {
        GAPUP,
        GAPDOWN,
        NEITHER;

        public Gap create(PriceAction pa) {
            return new Gap(){
                @Override
                public GapType getGapType() {
                    return GapType.this;
                }
            };
        }

        public boolean isGapUp() {
            return gapType() == GAPUP;
        }

        private GapType gapType() {
            return this;
        }

        public boolean isGapDown() {
            return gapType() == GAPDOWN;
        }
    }
}
