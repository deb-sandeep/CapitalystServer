package com.sandy.capitalyst.server.web;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller ;
import org.springframework.web.bind.annotation.RequestMapping ;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays ;
import java.util.List ;

@Controller
public class CapitalystController {

    @RequestMapping( "/" )
    public String home() {
        return "landing/landing" ;
    }

    @Bean
    public CorsFilter corsFilter() {
        System.out.println( "Registering CORS filter" ) ;
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials( true ) ;
        config.setAllowedOrigins( List.of( "http://localhost:4200" ) ) ;
        config.setAllowedHeaders( Arrays.asList("Origin", "Content-Type", "Accept") ) ;
        config.setAllowedMethods( Arrays.asList("GET", "POST", "PUT", "OPTIONS", "DELETE", "PATCH") ) ;

        source.registerCorsConfiguration( "/**", config ) ;
        return new CorsFilter( source ) ;
    }
}
