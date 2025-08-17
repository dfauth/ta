package com.github.dfauth.ta.service;

import com.github.dfauth.ta.model.MktDepth;
import com.github.dfauth.ta.repo.MktDepthRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class MktDepthService {

    @Autowired
    private MktDepthRepository mktDepthRepository;

    @Transactional
    public void sync(MktDepth mktDepth) {
        mktDepthRepository.save(mktDepth);
    }

    public List<MktDepth> findById(String code) {
        return mktDepthRepository.findById(code);
    }

    public Iterable<MktDepth> findAll() {
        return mktDepthRepository.findAll();
    }

    public Iterable<MktDepth> findLatest(LocalDate date) {
        return mktDepthRepository.findLatest(date);
    }

    public Optional<MktDepth> findByIdAndDate(String code, LocalDate date) {
        return mktDepthRepository.findByIdAndDate(code, asTimestamp(date));
    }

    public List<MktDepth> findByDate(LocalDate date) {
        return mktDepthRepository.findByDate(asTimestamp(date));
    }

    public static Timestamp asTimestamp(LocalDate date) {
        return new Timestamp(date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
    }

}
