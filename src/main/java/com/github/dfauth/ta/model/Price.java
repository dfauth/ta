package com.github.dfauth.ta.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

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
public class Price implements Candlestick, Dated<PriceAction> {

    @Id
    @JsonIgnore
    private String code;
    @Id
    @JsonIgnore
    @Column(name = "date")
    private Timestamp _date;
    @JsonIgnore
    private BigDecimal open;
    @JsonIgnore
    private BigDecimal high;
    @JsonIgnore
    private BigDecimal low;
    @JsonIgnore
    private BigDecimal close;
    @JsonIgnore
    private int volume;

    public Price() {
    }

    public Price(String code, Timestamp date, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, Integer volume) {
        this.code = code;
        this._date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }

    public static Timestamp parseDate(String dateString) {
        return new Timestamp(LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME).atZone(ZoneId.of("UTC")).toInstant().toEpochMilli());
    }

    public static BigDecimal parsePrice(Object arg) {
        if(arg == null) {
            return null;
        } else if(arg instanceof Double) {
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

    @JsonIgnore
    public PriceCompositeKey getKey() {
        return new PriceCompositeKey(code, _date);
    }

    @Override
    public LocalDate getLocalDate() {
        return getDate();
    }

    @Override
    public LocalDate getDate() {
        return get_date().toLocalDateTime().toLocalDate();
    }

    @Override
    public PriceAction getPayload() {
        return this;
    }
}
