package com.thc.sprbasic2025.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/support_project")
@Controller
public class SupportProjectController {
    @RequestMapping("/{page}")
    public String page(@PathVariable String page) {
        return "support_project/" + page;
    }

    @RequestMapping("/{page}/{id}")
    public String page(@PathVariable String page, @PathVariable String id) {
        return "support_project/" + page;
    }
}
