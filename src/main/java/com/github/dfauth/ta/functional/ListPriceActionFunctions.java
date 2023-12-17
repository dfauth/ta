package com.github.dfauth.ta.functional;

import com.github.dfauth.ta.functions.LinearRegression;
import com.github.dfauth.ta.model.PriceAction;

import java.util.List;
import java.util.function.Function;

import static com.github.dfauth.ta.functional.Lists.mapList;
import static com.github.dfauth.ta.functional.PriceActionFunction.generateList;
import static com.github.dfauth.ta.functions.LinearRegression.lobf;

public class ListPriceActionFunctions<T> implements WithMatcher<PriceActionFunction<List<PriceAction>,T>> {

    public static ListPriceActionFunctions<LinearRegression.LineOfBestFit> LOBF() {
        return new ListPriceActionFunctions<>("LOBF", i -> generateList(pa -> lobf(mapList(pa, PriceAction::getClose)).orElseThrow()));
    }

    public static ListPriceActionFunctions<?>[] values() {
        return new ListPriceActionFunctions[]{LOBF()};
    };

    private String name;
    private final Function<Integer, PriceActionFunction<List<PriceAction>, T>> f;

    public ListPriceActionFunctions(String name, Function<Integer, PriceActionFunction<List<PriceAction>, T>> f) {
        this.name = name;
        this.f = f;
    }

    public PriceActionFunction<List<PriceAction>,T> get(int n) {
        return f.apply(n);
    }

    public String name() {
        return name;
    }

}
