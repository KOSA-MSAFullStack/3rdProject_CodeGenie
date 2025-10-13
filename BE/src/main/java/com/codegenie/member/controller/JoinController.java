package com.codegenie.member.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codegenie.member.dto.JoinDTO;
import com.codegenie.member.service.JoinService;


@RestController
@RequestMapping("/api") 
public class JoinController {
    
    private final JoinService joinService;

    public JoinController(JoinService joinService) {
        
        this.joinService = joinService;
    }

    @PostMapping("/join")
    public String joinProcess(@RequestBody JoinDTO joinDTO) {

        System.out.println(joinDTO.getEmail());
        joinService.joinProcess(joinDTO);

        return "ok";
    }
} 