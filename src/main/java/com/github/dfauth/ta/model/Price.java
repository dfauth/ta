package com.github.dfauth.ta.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Entity
@Data
@IdClass(PriceCompositeKey.class)
public class Price implements Candlestick {

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
            return BigDecimal.valueOf((Double) arg).setScale(3);
        } else if(arg instanceof String) {
            return new BigDecimal((String) arg).setScale(3);
        } else {
            return BigDecimal.valueOf((Integer)arg).setScale(3);
        }
    }

    public static Integer parseVolume(Object arg) {
        return Integer.valueOf((String) arg);
    }

    public static Price parseStrings(String code, String[] fields) {
        return new Price(code,
                parseDate(fields[0]),
                parsePrice(fields[1]),
                parsePrice(fields[2]),
                parsePrice(fields[3]),
                parsePrice(fields[4]),
                parseVolume(fields[5])
        );
    }

    public PriceCompositeKey getKey() {
        return new PriceCompositeKey(_code, _date);
    }

    @Override
    public String getCode() {
        return get_code();
    }

    @Override
    public LocalDate getDate() {
        return get_date().toLocalDateTime().toLocalDate();
    }

    @Override
    public BigDecimal getOpen() {
        return get_open();
    }

    @Override
    public BigDecimal getHigh() {
        return get_high();
    }

    @Override
    public BigDecimal getLow() {
        return get_low();
    }

    @Override
    public BigDecimal getClose() {
        return get_close();
    }

    @Override
    public int getVolume() {
        return get_volume();
    }
}
