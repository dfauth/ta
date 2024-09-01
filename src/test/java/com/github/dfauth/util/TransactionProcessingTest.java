package com.github.dfauth.util;

import com.github.dfauth.ta.model.txn.CSVReducer;
import com.github.dfauth.ta.model.txn.TxnEntry;
import com.github.dfauth.ta.util.CSVReader;
import com.github.dfauth.ta.util.RestClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatusCode;

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
        var tmp = CSVReader.read(new FileInputStream("src/test/resources/Data_export_10082024.csv"), TxnEntry.FieldHandler.values().length);
        List<TxnEntry.Payment> txns = tmp.map(fieldStream -> fieldStream
                .collect(new CSVReducer<>(new TxnEntry.Accumulator(),
                        TxnEntry.FieldHandler.values(),
                        TxnEntry.Accumulator::instead)))
                .collect(Collectors.toList());
        txns.stream()
                        .forEach(e -> {
                            log.info("read: "+e);
                        });

        HttpStatusCode httpStatusCode = new RestClient().uploadPayments(txns);
        Assertions.assertEquals(HttpStatusCode.valueOf(200), httpStatusCode);
    }

}
