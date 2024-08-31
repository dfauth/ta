package com.github.dfauth.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class TransactionProcessingTest {

    @Test
    @Disabled
    public void testIt() throws IOException {
        var tmp = CSVReader.read(new FileInputStream("src/test/resources/all.csv"), TxnEntry.FieldHandler.values().length+1);
        tmp.forEach(x ->
                log.info("read: "+ x.collect(new CSVReducer<>(new TxnEntry.Accumulator(), TxnEntry.FieldHandler.values(), TxnEntry.Accumulator::finish)))
        );
    }

    @Test
    public void testIt2() throws IOException {
        var tmp = CSVReader.read(new FileInputStream("src/test/resources/Data_export_10082024.csv"), TxnEntry.FieldHandler.values().length+1);
        List<TxnEntry.Payment> txns = tmp.map(fieldStream -> fieldStream.collect(new CSVReducer<>(new TxnEntry.Accumulator(), TxnEntry.FieldHandler.values(), TxnEntry.Accumulator::instead))).collect(Collectors.toList());
        log.info("total dividends: "+txns.stream()
                        .map(e -> {
                            log.info("read: "+e);
                            return e;
                        })
                .filter(TxnEntry.Payment::isDividendPayment)
                .filter(p -> p.getDate().getYear() == 2024)
                .map(TxnEntry.Payment::getValue)
                .reduce((n1,n2) -> n1.doubleValue()+n2.doubleValue()));
    }

}
