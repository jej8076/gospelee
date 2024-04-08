package com.gospelee.api.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.util.StringUtils;
import redis.embedded.RedisServer;

@Slf4j
@Configuration
public class EmbeddedRedisConfig {

  private final int redisPort = 6377;

  // 500MB 안됨
  private final int redisMaxMemory = 500000000;

  private RedisServer redisServer;

  @EventListener(ApplicationReadyEvent.class)
  public void startRedis() throws IOException {
    int port = isRedisRunning() ? findAvailablePort() : redisPort;

    if (isArmArchitecture()) {
      log.info("ARM Architecture");
      redisServer = new RedisServer(Objects.requireNonNull(getRedisServerExecutable()), port);
    } else {
      redisServer = RedisServer.builder()
          .port(port)
          .setting("maxmemory " + redisMaxMemory)
          .build();
    }

    redisServer.start();
  }

  @EventListener(ContextClosedEvent.class)
  public void stopRedis() {
    redisServer.stop();
  }

  public int findAvailablePort() throws IOException {
    for (int port = 10000; port <= 65535; port++) {
      Process process = executeGrepProcessCommand(port);
      if (!isRunning(process)) {
        return port;
      }
    }

    throw new RuntimeException("444");
  }

  /**
   * Embedded Redis가 현재 실행중인지 확인
   */
  private boolean isRedisRunning() throws IOException {
    return isRunning(executeGrepProcessCommand(redisPort));
  }

  /**
   * 해당 port를 사용중인 프로세스를 확인하는 sh 실행
   */
  private Process executeGrepProcessCommand(int redisPort) throws IOException {
    String command = String.format("netstat -nat | grep LISTEN | grep %d", redisPort);
    String[] shell = {"/bin/sh", "-c", command};

    return Runtime.getRuntime().exec(shell);
  }

  /**
   * 해당 Process가 현재 실행중인지 확인
   */
  private boolean isRunning(Process process) {
    String line;
    StringBuilder pidInfo = new StringBuilder();

    try (BufferedReader input = new BufferedReader(
        new InputStreamReader(process.getInputStream()))) {
      input.lines().forEach(pidInfo::append);
    } catch (Exception e) {
      throw new RuntimeException("444");
    }
    return StringUtils.hasText(pidInfo.toString());
  }

  private boolean isArmArchitecture() {
    return System.getProperty("os.arch").contains("aarch64");
  }

  private File getRedisServerExecutable() {
    try {
      return new File("gospelee-api/src/main/resources/binary/redis/redis-server-7.2.4-arm64");
    } catch (Exception e) {
      throw new RuntimeException("redis-server file not found");
    }
  }

}