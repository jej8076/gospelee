package com.gospelee.socket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class GospeleeSocketApplication {

  public static void main(String[] args) {
    SpringApplication.run(GospeleeSocketApplication.class, args);
  }

}
