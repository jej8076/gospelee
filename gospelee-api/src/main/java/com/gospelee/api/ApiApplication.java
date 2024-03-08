package com.gospelee.api;

import com.gospelee.api.dto.Account.AccountDTO;
import com.gospelee.api.entity.Account;
import com.gospelee.api.repository.AccountRepository;
import com.gospelee.api.repository.BibleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import util.FieldUtil;

@EnableJpaAuditing
@SpringBootApplication
public class ApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(ApiApplication.class, args);
  }

  @Bean
  public CommandLineRunner runner(AccountRepository accountRepository) {
    return (args) -> {
      if (accountRepository.count() == 0) {
        AccountDTO account = AccountDTO.builder()
            .name("정의진")
            .phone("01024318076")
            .rrn("9108141155812")
            .build();
        Account entity = (Account) FieldUtil.toEntity(account);
        accountRepository.save(entity);
      }
    };
  }
}


