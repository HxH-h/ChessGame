package com.matchsystem;

import com.matchsystem.Service.MatchPool;
import com.matchsystem.Service.MatchService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MatchSystemApplication {

    public static void main(String[] args) {
        MatchService.matchpool.start();
        SpringApplication.run(MatchSystemApplication.class, args);
    }

}
