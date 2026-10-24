package com.gospelee.api.service.reaction;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.reaction.ReactionMineItemDTO;
import com.gospelee.api.dto.reaction.ReactionMinePageDTO;
import com.gospelee.api.dto.reaction.ReactionSummaryDTO;
import com.gospelee.api.entity.Reaction;
import com.gospelee.api.enums.ReactionTargetType;
import com.gospelee.api.enums.ReactionType;
import com.gospelee.api.repository.jdbc.ReactionJdbcRepository;
import com.gospelee.api.repository.jpa.reaction.ReactionRepository;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReactionServiceImpl implements ReactionService {

  private static final int MAX_SUMMARY_IDS = 200;
  private static final int MAX_PAGE_SIZE = 100;

  private final ReactionRepository reactionRepository;
  private final ReactionJdbcRepository reactionJdbcRepository;
  private final ReactionCounter counter;
  private final Map<ReactionTargetType, ReactionTargetPolicy> policies =
      new EnumMap<>(ReactionTargetType.class);

  public ReactionServiceImpl(ReactionRepository reactionRepository,
      ReactionJdbcRepository reactionJdbcRepository, ReactionCounter counter,
      List<ReactionTargetPolicy> targetPolicies) {
    this.reactionRepository = reactionRepository;
    this.reactionJdbcRepository = reactionJdbcRepository;
    this.counter = counter;
    for (ReactionTargetPolicy policy : targetPolicies) {
      policies.put(policy.targetType(), policy);
    }
  }

  @Override
  public ReactionSummaryDTO set(AccountAuthDTO account, ReactionType type,
      ReactionTargetType targetType, Long targetId, boolean active) {
    if (active) {
      // 켤 때만 대상 규칙을 확인한다. 끄는 건 대상이 사라졌어도 언제나 할 수 있어야 한다
      policyOf(targetType).assertReactable(account, targetId);
    }

    // DB 가 먼저, 실제로 행이 바뀐 경우에만 카운터를 움직인다 (겹친 요청이 이중으로 세지지 않음)
    int changed = active
        ? reactionJdbcRepository.insertIgnore(type.name(), targetType.name(), targetId, account.getUid())
        : reactionRepository.deleteOne(type.name(), targetType.name(), targetId, account.getUid());
    if (changed > 0) {
      counter.applyDelta(type, targetType, targetId, active ? 1 : -1);
    }

    long count = countsOf(type, targetType, List.of(targetId)).getOrDefault(targetId, 0L);
    return new ReactionSummaryDTO(targetId, count, active);
  }

  @Override
  public List<ReactionSummaryDTO> summarize(Long accountUid, ReactionType type,
      ReactionTargetType targetType, List<Long> targetIds) {
    List<Long> ids = distinctLimited(targetIds);
    if (ids.isEmpty()) {
      return List.of();
    }
    Map<Long, Long> counts = countsOf(type, targetType, ids);
    Set<Long> mine = accountUid == null ? Set.of()
        : new HashSet<>(reactionRepository.findMyTargetIds(accountUid, type.name(),
            targetType.name(), ids));
    return ids.stream()
        .map(id -> new ReactionSummaryDTO(id, counts.getOrDefault(id, 0L), mine.contains(id)))
        .toList();
  }

  @Override
  public ReactionMinePageDTO listMine(Long accountUid, ReactionType type,
      ReactionTargetType targetType, Long cursor, int size) {
    int pageSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));
    // 한 건 더 가져와서 다음 페이지가 있는지 판단한다
    List<Reaction> rows = reactionRepository.findMine(accountUid, type.name(),
        targetType == null ? null : targetType.name(), cursor, PageRequest.of(0, pageSize + 1));
    boolean hasNext = rows.size() > pageSize;
    List<Reaction> page = hasNext ? rows.subList(0, pageSize) : rows;
    List<ReactionMineItemDTO> items = page.stream()
        .map(r -> new ReactionMineItemDTO(r.getUid(), r.getTargetType(), r.getTargetId(),
            r.getInsertTime()))
        .toList();
    return new ReactionMinePageDTO(items, hasNext ? page.get(page.size() - 1).getUid() : null);
  }

  @Override
  public void removeAllByTarget(ReactionTargetType targetType, Long targetId) {
    reactionRepository.deleteAllByTarget(targetType.name(), targetId);
    for (ReactionType type : ReactionType.values()) {
      counter.evict(type, targetType, targetId);
    }
  }

  @Override
  public void removeAllByAccount(Long accountUid) {
    // 지우기 전에 어떤 대상의 카운터를 무효화해야 하는지 모아 둔다
    List<Object[]> targets = reactionRepository.findAllTargetsByAccount(accountUid);
    reactionRepository.deleteAllByAccount(accountUid);
    for (Object[] row : targets) {
      try {
        counter.evict(ReactionType.valueOf((String) row[0]),
            ReactionTargetType.valueOf((String) row[1]), (Long) row[2]);
      } catch (IllegalArgumentException e) {
        log.warn("[REACTION] 알 수 없는 반응/대상 종류 row:{}/{}", row[0], row[1]);
      }
    }
  }

  private Map<Long, Long> countsOf(ReactionType type, ReactionTargetType targetType,
      List<Long> ids) {
    return counter.getCounts(type, targetType, ids, missing -> {
      Map<Long, Long> loaded = new HashMap<>();
      for (Object[] row : reactionRepository.countByTargets(type.name(), targetType.name(),
          missing)) {
        loaded.put((Long) row[0], (Long) row[1]);
      }
      return loaded;
    });
  }

  private ReactionTargetPolicy policyOf(ReactionTargetType targetType) {
    ReactionTargetPolicy policy = policies.get(targetType);
    if (policy == null) {
      throw new IllegalArgumentException("지원하지 않는 반응 대상입니다: " + targetType);
    }
    return policy;
  }

  private static List<Long> distinctLimited(List<Long> ids) {
    if (ids == null) {
      return List.of();
    }
    return ids.stream().filter(java.util.Objects::nonNull).distinct().limit(MAX_SUMMARY_IDS)
        .collect(Collectors.toList());
  }
}
