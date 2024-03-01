package com.github.dfauth.ta.controller;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Slf4j
public class ControllerUtils {

    static BigDecimal toBigDecimal(Object o) {
        if(o == null) {
            return null;
        } else if(o instanceof Integer) {
            return BigDecimal.valueOf((Integer)o);
        } else if(o instanceof Double){
            return BigDecimal.valueOf((Double) o);
        } else if(o instanceof String){
            if("".equals(o)) {
                return null;
            } else {
                return new BigDecimal((String) o);
            }
        } else {
            throw new IllegalArgumentException("Unsupported type: "+o.getClass());
        }
    }

}
