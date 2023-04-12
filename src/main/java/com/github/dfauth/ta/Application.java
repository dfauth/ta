package com.github.dfauth.ta;

import com.github.dfauth.ta.repo.PriceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class Application {

    @Autowired
    private PriceRepository repository;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    public void run(String... args) {

        log.info("StartApplication...");

        System.out.println("\nfindAll()");
        repository.findAll().forEach(x -> log.info("findAll: ",x));

    }
}