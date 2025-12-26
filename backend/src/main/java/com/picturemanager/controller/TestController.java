// src/main/java/com/picturemanager/controller/TestController.java
package com.picturemanager.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello from TestController!";
    }

    @GetMapping("/images/recent")
    public String testImages() {
        return "Test images endpoint";
    }
}