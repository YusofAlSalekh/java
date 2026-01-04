package com.ortecfinance.tasklist;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

@Profile("console")
@Configuration
public class ConsoleIOConfig {

    @Bean
    public BufferedReader consoleReader() {
        return new BufferedReader(new InputStreamReader(System.in));
    }

    @Bean
    public PrintWriter consoleWriter() {
        return new PrintWriter(System.out, true);
    }
}
