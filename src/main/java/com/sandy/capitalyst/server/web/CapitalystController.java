package com.sandy.capitalyst.server.web;

import org.springframework.stereotype.Controller ;
import org.springframework.web.bind.annotation.RequestMapping ;

@Controller
public class CapitalystController {

    @RequestMapping( "/" )
    public String home() {
        return "landing/landing" ;
    }
}
