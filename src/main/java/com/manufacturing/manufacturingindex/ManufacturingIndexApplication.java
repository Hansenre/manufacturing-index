package com.manufacturing.manufacturingindex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.manufacturing.manufacturingindex")
public class ManufacturingIndexApplication {

    public static void main(String[] args) {
        SpringApplication.run(ManufacturingIndexApplication.class, args);
    }
}
