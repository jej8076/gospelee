package com.gospelee.api.service.reaction;

import com.gospelee.api.enums.ReactionTargetType;
import com.gospelee.api.enums.ReactionType;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

/**
 * 반응 수 카운터 (Redis).
 *
 * <p>원본은 DB 의 reaction 테이블이고, Redis 는 목록 조회 때마다 COUNT 쿼리를 날리지 않기 위한
 * 캐시다. 쓰기는 항상 DB 가 먼저 반영된 뒤(그 행이 실제로 바뀐 경우에만) 카운터를 ±1 한다.
 * 그래서 같은 요청이 겹쳐도 카운터가 이중으로 오르지 않는다.
 *
 * <ul>
 *   <li>키가 있을 때만 증감한다 (Lua). 없으면 다음 조회 때 DB COUNT 로 채운다.</li>
 *   <li>채울 때는 SET NX 라서 동시에 여러 요청이 채워도 먼저 쓴 값이 유지된다.</li>
 *   <li>키에는 TTL(약 6시간)을 둔다. 장애나 경합으로 어긋나도 TTL 이 지나면 DB 값으로 돌아온다.</li>
 *   <li>Redis 가 죽어도 DB COUNT 로 대신 응답한다 (느려질 뿐 틀리지 않는다).</li>
 * </ul>
 */
@Slf4j
@Component
public class ReactionCounter {

  private static final String KEY_PREFIX = "reaction:count:";
  private static final Duration BASE_TTL = Duration.ofHours(6);

  // 키가 있을 때만 증감하고, 0 아래로 내려가지 않게 한다. 키가 없으면 -1
  private static final DefaultRedisScript<Long> APPLY_DELTA = new DefaultRedisScript<>(
      "if redis.call('EXISTS', KEYS[1]) == 1 then "
          + "local v = redis.call('INCRBY', KEYS[1], ARGV[1]) "
          + "if v < 0 then redis.call('SET', KEYS[1], 0, 'KEEPTTL') return 0 end "
          + "return v "
          + "else return -1 end",
      Long.class);

  private final StringRedisTemplate redis;

  public ReactionCounter(RedisConnectionFactory connectionFactory) {
    this.redis = new StringRedisTemplate(connectionFactory);
    this.redis.afterPropertiesSet();
  }

  /**
   * 대상들의 반응 수. Redis 에 없는 대상만 {@code dbLoader} 로 조회해 캐시에 채운다.
   * 반환 맵에는 모든 대상 id 가 들어 있다 (반응이 없으면 0).
   */
  public Map<Long, Long> getCounts(ReactionType type, ReactionTargetType targetType,
      List<Long> targetIds, Function<List<Long>, Map<Long, Long>> dbLoader) {
    Map<Long, Long> result = new HashMap<>();
    if (targetIds.isEmpty()) {
      return result;
    }

    List<Long> missing = new ArrayList<>();
    try {
      List<String> keys = targetIds.stream().map(id -> key(type, targetType, id)).toList();
      List<String> cached = redis.opsForValue().multiGet(keys);
      for (int i = 0; i < targetIds.size(); i++) {
        String value = cached == null ? null : cached.get(i);
        Long parsed = parse(value);
        if (parsed == null) {
          missing.add(targetIds.get(i));
        } else {
          result.put(targetIds.get(i), parsed);
        }
      }
    } catch (Exception e) {
      log.warn("[REACTION] Redis 조회 실패, DB 로 대체 type:{} target:{} - {}", type, targetType,
          e.getMessage());
      missing = new ArrayList<>(targetIds);
      result.clear();
    }

    if (!missing.isEmpty()) {
      Map<Long, Long> loaded = dbLoader.apply(missing);
      for (Long id : missing) {
        long count = loaded.getOrDefault(id, 0L);
        result.put(id, count);
        try {
          redis.opsForValue().setIfAbsent(key(type, targetType, id), Long.toString(count), ttl());
        } catch (Exception e) {
          log.warn("[REACTION] Redis 저장 실패 id:{} - {}", id, e.getMessage());
        }
      }
    }
    return result;
  }

  /** DB 가 실제로 바뀐 뒤 호출한다. 키가 없으면 아무것도 하지 않는다 (다음 조회 때 DB 로 채움) */
  public void applyDelta(ReactionType type, ReactionTargetType targetType, Long targetId,
      long delta) {
    try {
      redis.execute(APPLY_DELTA, List.of(key(type, targetType, targetId)), Long.toString(delta));
    } catch (Exception e) {
      log.warn("[REACTION] Redis 증감 실패 type:{} target:{}:{} - {}", type, targetType, targetId,
          e.getMessage());
      evict(type, targetType, targetId);
    }
  }

  /** 카운터를 지워서 다음 조회 때 DB 값으로 다시 채우게 한다 */
  public void evict(ReactionType type, ReactionTargetType targetType, Long targetId) {
    try {
      redis.delete(key(type, targetType, targetId));
    } catch (Exception e) {
      log.warn("[REACTION] Redis 삭제 실패 type:{} target:{}:{} - {}", type, targetType, targetId,
          e.getMessage());
    }
  }

  static String key(ReactionType type, ReactionTargetType targetType, Long targetId) {
    return KEY_PREFIX + type.name() + ":" + targetType.name() + ":" + targetId;
  }

  private static Long parse(String value) {
    if (value == null) {
      return null;
    }
    try {
      return Long.parseLong(value);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  // 같은 시각에 한꺼번에 만료돼 DB 로 몰리지 않게 TTL 에 편차를 둔다
  private static Duration ttl() {
    return BASE_TTL.plusSeconds(ThreadLocalRandom.current().nextLong(0, 1800));
  }
}
