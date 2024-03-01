package com.github.dfauth.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dfauth.ta.Application;
import com.github.dfauth.ta.model.Price;
import com.github.dfauth.ta.model.PriceAction;
import com.github.dfauth.ta.repo.PriceRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static com.github.dfauth.ta.functional.Lists.mapList;
import static io.github.dfauth.trycatch.ExceptionalRunnable.tryCatch;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = Application.class)
//@AutoConfigureMockMvc
//@TestPropertySource(
//        locations = "classpath:application-integrationtest.properties")
public class ZipUtils {

    @Autowired
    private PriceRepository priceRepository;

    @Test
    public void testGetPrices() throws IOException {
        String prices = getPrices("ASX:WGX");
        log.info("result: "+prices);
    }

    public String getPrices(String code) throws IOException {
        return getPrices(code, 252);
    }

    public String getPrices(String code, int limit) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        List<PriceAction> prices = mapList(priceRepository.findByCode(code,limit), PriceAction.class::cast);
        String result = prices.stream().map(p -> tryCatch(() -> mapper.writeValueAsString(p))).collect(Collectors.joining(",", "[", "]"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(baos);
        zip.putNextEntry(new ZipEntry("prices"));
        zip.write(result.getBytes());
        zip.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    public static List<Price> unzip(String s) {
        try {
            byte[] bytes = Base64.getDecoder().decode(s.getBytes());
            ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(bytes));
            log.info("reading: "+zip.getNextEntry().getName());
            byte[] buffer = new byte[1024];
            int len;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while((len = zip.read(buffer)) > 0) {
                baos.write(buffer,0,len);
            }
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(baos.toByteArray(), new TypeReference<List<Price>>() {});
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
