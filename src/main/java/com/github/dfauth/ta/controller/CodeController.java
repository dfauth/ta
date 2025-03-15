package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.model.Code;
import com.github.dfauth.ta.repo.CodeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class CodeController {

    @Autowired
    private CodeRepository codeRepository;

    @GetMapping("/codes")
    @ResponseStatus(HttpStatus.OK)
    public Iterable<Code> codes() {
        return codeRepository.findAll();
    }

    @GetMapping("/codes/add/{code}")
    @ResponseStatus(HttpStatus.OK)
    public Code addCode(@PathVariable String code) {
        return codeRepository.save(new Code(code));
    }
}
