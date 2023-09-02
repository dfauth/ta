package com.github.dfauth.ta.functional;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public abstract class Tuple implements Iterable<Object>, Serializable, Comparable<Tuple> {

    protected final Object[] valueArray;
    protected final List<Object> valueList;

    protected Tuple(final Object... values) {
        this.valueArray = values;
        this.valueList = Arrays.asList(values);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public int compareTo(final Tuple t) {

        final int tLen = this.valueArray.length;
        final Object[] oValues = t.valueArray;
        final int oLen = oValues.length;

        for (int i = 0; i < tLen && i < oLen; i++) {

            final Comparable tElement = (Comparable)this.valueArray[i];
            final Comparable oElement = (Comparable)oValues[i];

            final int comparison = tElement.compareTo(oElement);
            if (comparison != 0) {
                return comparison;
            }

        }

        return (Integer.valueOf(tLen)).compareTo(Integer.valueOf(oLen));

    }
    @Override
    public Iterator<Object> iterator() {
        return valueList.iterator();
    }
}
