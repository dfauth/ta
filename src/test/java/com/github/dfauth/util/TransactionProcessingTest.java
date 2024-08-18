package com.github.dfauth.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
public class TransactionProcessingTest {

    @Test
    public void testIt() throws IOException {
        var tmp = CSVReader.read(new FileInputStream("src/test/resources/all.csv"));
        tmp.forEach(x ->
                log.info("read: "+ x.collect(new CSVReducer<>(new TxnEntry.Accumulator(), TxnEntry.FieldHandler.values(), TxnEntry.Accumulator::finish)))
        );
    }

    @Test
    @Disabled
    public void testIt2() throws IOException {
        var tmp = CSVReader.read(new FileInputStream("src/test/resources/Data_export_10082024.csv"));
        tmp.forEach(x ->
                log.info("read: "+ x.collect(new CSVReducer<>(new TxnEntry.Accumulator(), TxnEntry.FieldHandler.values(), TxnEntry.Accumulator::finish)))
        );
    }

}
