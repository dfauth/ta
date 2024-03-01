package com.github.dfauth.ta.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
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
public class Price implements Candlestick, Dated<PriceAction> {

    @Id
    @JsonIgnore
    private String _code;
    @Id
    @JsonIgnore
    private Timestamp _date;
    @JsonIgnore
    private BigDecimal _open;
    @JsonIgnore
    private BigDecimal _high;
    @JsonIgnore
    private BigDecimal _low;
    @JsonIgnore
    private BigDecimal _close;
    @JsonIgnore
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

    @JsonIgnore
    public PriceCompositeKey getKey() {
        return new PriceCompositeKey(_code, _date);
    }

    @Override
    public String getCode() {
        return get_code();
    }

    public void setCode(String code) {
        set_code(code);
    }


    @Override
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonSerialize(using=LocalDateSerializer.class)
    @JsonDeserialize(using=LocalDateDeserializer.class)
    public LocalDate getDate() {
        return get_date().toLocalDateTime().toLocalDate();
    }

    public void setDate(LocalDate date) {
        set_date(new Timestamp(date.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()));
    }


    @Override
    public BigDecimal getOpen() {
        return get_open();
    }

    public void setOpen(BigDecimal open) {
        set_open(open);
    }

    @Override
    public BigDecimal getHigh() {
        return get_high();
    }

    public void setLow(BigDecimal low) {
        set_low(low);
    }

    @Override
    public BigDecimal getLow() {
        return get_low();
    }

    public void setHigh(BigDecimal high) {
        set_high(high);
    }

    @Override
    public BigDecimal getClose() {
        return get_close();
    }

    public void setClose(BigDecimal close) {
        set_close(close);
    }

    @Override
    public int getVolume() {
        return get_volume();
    }
    public void setVolume(int vol) {
        set_volume(vol);
    }

    @Override
    public LocalDate getLocalDate() {
        return _date.toLocalDateTime().toLocalDate();
    }

    @Override
    public PriceAction getPayload() {
        return this;
    }
}
