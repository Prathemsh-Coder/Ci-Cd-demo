package com.example.demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class HelloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void helloReturnsDefaultGreeting() throws Exception {
        mockMvc.perform(get("/api/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "Hello, World! This build was  by Jenkins."));
    }

    @Test
    void helloReturnsCustomName() throws Exception {
        mockMvc.perform(get("/api/hello").param("name", "Prathmesh"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "Hello, Prathmesh! This build was verified by Jenkins."));
    }

    @Test
    void versionEndpointWorks() throws Exception {
        mockMvc.perform(get("/api/version"))
                .andExpect(status().isOk())
                .andExpect(content().string("v1.0.0"));
    }

}
