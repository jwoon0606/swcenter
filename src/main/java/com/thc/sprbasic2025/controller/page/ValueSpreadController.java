package com.thc.sprbasic2025.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/value-spread")
@Controller
public class ValueSpreadController {
    @RequestMapping("/support_project/{id}")
    public String supportProjectDetail(@PathVariable String id){
        return "value-spread/support_project_detail";
    }

    @RequestMapping("/{page}")
    public String page(@PathVariable String page){
        return "value-spread/" + page;
    }
}
