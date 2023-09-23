package com.github.dfauth.repo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dfauth.ta.Application;
import com.github.dfauth.ta.model.Price;
import com.github.dfauth.ta.repo.PriceRepository;
import com.github.dfauth.ta.repo.ValuationRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.github.dfauth.ta.functions.RSI.HUNDRED;
import static com.github.dfauth.ta.util.ExceptionalRunnable.tryCatch;
import static java.math.BigDecimal.ONE;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = Application.class)
//@AutoConfigureMockMvc
//@TestPropertySource(
//        locations = "classpath:application-integrationtest.properties")
public class RepoTest {

    @Autowired
    ValuationRepository valuationRepository;

    @Autowired
    PriceRepository priceRepository;

    @Test
    public void testValuation() {

        Timestamp latestPriceDate = priceRepository.latestPriceDate();
        Timestamp latestValuationDate = valuationRepository.latestValuationDate();
        List<Object[]> top50 = valuationRepository.topPotential(latestValuationDate, latestPriceDate, 50);
        top50.stream().forEach(p ->
                log.error("code: {}, target: {}, price: {}, potential: {}",p[0],p[1],p[2],potential((BigDecimal) p[1], (BigDecimal) p[2])));
    }

    @Test
    public void testActive() {

        Timestamp latestPriceDate = priceRepository.latestPriceDate();
        List<Price> active = priceRepository.activeAsAtDate(latestPriceDate);
        active.stream().forEach(p -> {
            log.error("code: {}, date: {}, close: {}, volume: {}", p.get_code(), p.get_date(), p.get_close(), p.get_volume());
        });
    }

    @Test
//    @Ignore
    public void getPrices() throws IOException, InterruptedException {

        ObjectMapper mapper = new ObjectMapper();
        List<Price> prices = priceRepository.findByCode("ASX:EMR",252);
        String result = prices.stream().map(p -> tryCatch(() -> mapper.writeValueAsString(p))).collect(Collectors.joining(",", "[", "]"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(baos);
        zip.putNextEntry(new ZipEntry("prices"));
        zip.write(result.getBytes());
        zip.close();
        String result1 = Base64.getEncoder().encodeToString(baos.toByteArray());
        log.info("result: "+result1);
    }

    private static BigDecimal potential(BigDecimal num, BigDecimal dem) {
        return num.divide(dem, RoundingMode.HALF_UP).subtract(ONE).multiply(HUNDRED).setScale(2);
    }
}
