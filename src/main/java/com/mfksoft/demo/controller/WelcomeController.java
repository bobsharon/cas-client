package com.mfksoft.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@Controller
public class WelcomeController {

    @GetMapping("/")
    public String greeting(HttpServletRequest request, Model model) {
        model.addAttribute("name", request.getUserPrincipal().getName());
        return "welcome";
    }

}
