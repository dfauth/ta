package com.github.dfauth.repo;

import com.github.dfauth.ta.Application;
import com.github.dfauth.ta.model.Price;
import com.github.dfauth.ta.repo.PriceRepository;
import com.github.dfauth.ta.repo.ValuationRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.List;

import static com.github.dfauth.ta.functions.RSI.HUNDRED;
import static java.math.BigDecimal.ONE;

@Slf4j
@ExtendWith(SpringExtension.class)
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
    public PriceRepository priceRepository;

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

    private static BigDecimal potential(BigDecimal num, BigDecimal dem) {
        return num.divide(dem, RoundingMode.HALF_UP).subtract(ONE).multiply(HUNDRED).setScale(2);
    }
}
