package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/api/hello")
    public String hello(@RequestParam(defaultValue = "World") String name) {
        return "Hello, " + name + "! This build was verified by Jenkins.";
    }

    @GetMapping("/api/version")
    public String version() {
        return "v1.0.0";
    }

}
