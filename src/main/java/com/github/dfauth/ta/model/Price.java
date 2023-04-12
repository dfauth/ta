package com.github.dfauth.ta.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Entity
@Data
@IdClass(PriceCompositeKey.class)
public class Price {

    @Id private String _code;
    @Id private Timestamp _date;
    private BigDecimal _open;
    private BigDecimal _high;
    private BigDecimal _low;
    private BigDecimal _close;
    private Integer _volume;

    public Price() {
    }

    public Price(String _code, Timestamp date, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, Integer volume) {
        this._code = _code;
        this._date = date;
        this._open = open;
        this._high = high;
        this._low = low;
        this._close = close;
        this._volume = volume;
    }

    public static Timestamp parseDate(String dateString) {
        return new Timestamp(LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME).atZone(ZoneId.of("UTC")).toInstant().toEpochMilli());
    }

    public static BigDecimal parsePrice(Object arg) {
        if(arg instanceof Double) {
            return BigDecimal.valueOf((Double)arg);
        } else {
            return BigDecimal.valueOf((Integer)arg);
        }
    }

    public static Integer parseVolume(Object arg) {
        return Integer.valueOf((String) arg);
    }

    public PriceCompositeKey getKey() {
        return new PriceCompositeKey(_code, _date);
    }
}
