package de.kaleidox.galio.components.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping
public class GenericController {
    @GetMapping("/")
    public String index() {
        return "index";
    }
}
