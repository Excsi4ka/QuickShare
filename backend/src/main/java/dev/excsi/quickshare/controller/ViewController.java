package dev.excsi.quickshare.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping({"/", "/download/**", "/register", "/login"})
    public String serveHtml() {
        return "forward:/index.html";
    }

}
