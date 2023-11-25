package com.github.dfauth.ta.functional;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface WithMatcher<T> {
    String name();
    default NamePeriodMatcher matcher(String id) {
        Matcher matcher = Pattern.compile(String.format("%s\\((\\d+)\\)",name().toLowerCase())).matcher(id.toLowerCase());
        Optional<Integer> period = Optional.of(matcher).filter(Matcher::find).map(m -> m.group(1)).map(Integer::parseInt);
        return new NamePeriodMatcher() {
            @Override
            public boolean matches() {
                return period.isPresent();
            }

            @Override
            public int period() {
                return period.orElseThrow();
            }
        };
    }

    T get(int period);

    interface NamePeriodMatcher {
        boolean matches();
        int period();
    }
}
