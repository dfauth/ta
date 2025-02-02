package com.github.dfauth.ta.service;

import com.github.dfauth.ta.model.MktDepth;
import com.github.dfauth.ta.repo.MktDepthRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

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
}
