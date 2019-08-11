package com.mfksoft.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WelcomeController {

    @Value("${welcome.name}")
    private String name;

    @GetMapping("/")
    public String greeting(Model model) {
        model.addAttribute("name", name);
        return "welcome";
    }

    @GetMapping("/hello")
    public String greetingTo(@RequestParam(name = "name", required = false, defaultValue = "") String name, Model model) {
        model.addAttribute("name",name);
        return "welcome";
    }

}
