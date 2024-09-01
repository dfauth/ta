package com.github.dfauth.ta.util;

import com.github.dfauth.ta.model.txn.TxnEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;


@Slf4j
public class RestClient {

    public HttpStatusCode uploadPayments(List<TxnEntry.Payment> txns) {

        WebClient client = WebClient.builder()
                .baseUrl("http://localhost:8080/txns/sync")
                .build();
        return client.post().body(Flux.fromStream(txns
                        .stream()), TxnEntry.Payment.class)
                .exchange().block().statusCode();
    }
}
