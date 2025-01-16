package ru.seller_support.assignment.controller.view;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/panel")
public class PanelViewController {
    @GetMapping
    public String panel() {
        return "panel";
    }
}
